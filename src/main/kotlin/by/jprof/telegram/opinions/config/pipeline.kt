package by.jprof.telegram.opinions.config

import by.jprof.telegram.opinions.processors.CustomVotesProcessor
import by.jprof.telegram.opinions.processors.JEPLinksProcessor
import by.jprof.telegram.opinions.processors.KotlinMentionsProcessor
import by.jprof.telegram.opinions.processors.KotlinStatsCommandProcessor
import by.jprof.telegram.opinions.processors.Tesseract
import by.jprof.telegram.opinions.processors.UpdateProcessingPipeline
import by.jprof.telegram.opinions.processors.UpdateProcessor
import by.jprof.telegram.opinions.processors.YoutubeLinksProcessor
import org.koin.core.qualifier.named
import org.koin.dsl.module

val pipelineModule = module {
    single {
        UpdateProcessingPipeline(getAll())
    }

    single<UpdateProcessor>(named("JEPLinksProcessor")) {
        JEPLinksProcessor(get(), get())
    }

    single<UpdateProcessor>(named("YoutubeLinksProcessor")) {
        YoutubeLinksProcessor(get(), get(), get(), get())
    }

    single<UpdateProcessor>(named("KotlinMentionsProcessor")) {
        KotlinMentionsProcessor(get(), get(), get(), get())
    }

    single<UpdateProcessor>(named("KotlinStatsCommandProcessor")) {
        KotlinStatsCommandProcessor(get(), get())
    }

    single<UpdateProcessor>(named("CustomVotesProcessor")) {
        CustomVotesProcessor(get(), get(), get())
    }

    single {
        Tesseract()
    }
}
