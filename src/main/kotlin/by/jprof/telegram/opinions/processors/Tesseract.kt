package by.jprof.telegram.opinions.processors

import org.bytedeco.leptonica.global.lept.pixRead
import org.bytedeco.tesseract.TessBaseAPI
import org.bytedeco.tesseract.global.tesseract
import org.bytedeco.tesseract.global.tesseract.TessBaseAPIGetIterator
import org.bytedeco.tesseract.global.tesseract.TessBaseAPIInit2
import org.bytedeco.tesseract.global.tesseract.TessBaseAPIRecognize
import org.bytedeco.tesseract.global.tesseract.TessBaseAPISetImage2
import org.bytedeco.tesseract.global.tesseract.TessResultIteratorGetUTF8Text
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

enum class Lang(val label: String) {
    RUS("rus"), ENG("eng")
}

class Tesseract(private val tessdatasrc: String = "tessdata") {
    companion object {
        private fun copyTessdata(tessdatapath: String, destdir: File) {
            val cl = this::class.java.classLoader
            Lang.values().forEach { lang ->
                val name = "${lang.label}.traineddata"
                val from = "${tessdatapath}/$name"
                val to = File(destdir, name)
                cl.getResource(from)?.openStream()?.use {
                    it.copyTo(FileOutputStream(to))
                }
            }
        }
    }

    private lateinit var tessdata: File

    fun ocr(imgFile: File, lang: Lang): String {
        ensureTessdata()
        return TessBaseAPI().use { api ->
            TessBaseAPIInit2(api, tessdata.toString(), lang.label, tesseract.OEM_DEFAULT)
            pixRead(imgFile.absolutePath).use { img ->
                TessBaseAPISetImage2(api, img)
                TessBaseAPIRecognize(api, null/*monitor*/)
                TessBaseAPIGetIterator(api).use { resultIter ->
                    TessResultIteratorGetUTF8Text(resultIter, tesseract.RIL_BLOCK).use { bytes ->
                        String(bytes.stringBytes, StandardCharsets.UTF_8)
                    }
                }
            }
        }
    }

    private fun ensureTessdata() {
        if (this::tessdata.isInitialized) {
            check(tessdata)
            return
        }

        // if tess data already prepared - use it
        System.getenv("OPINIONS_LAYER_PATH")?.let {
            tessdata = File(it)
            check(tessdata)
            return
        }

        // otherwise try to find them among resources
        tessdata = Files.createTempDirectory("jprof-ocr").toFile()
        tessdata.deleteOnExit()
        copyTessdata(tessdatasrc, tessdata)
        check(tessdata)
    }

    private fun check(tessdata: File) {
        if (!tessdata.isDirectory) {
            throw IOException("'tessdata' exists but is not a directory")
        }
        Lang.values().forEach {
            if (!File(tessdata, "${it.label}.traineddata").isFile) {
                throw IOException("'{tessdata}/${it.label}.traineddata' not found")
            }
        }
    }
}