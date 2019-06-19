package com.carles.mediquomobiletest

import com.carles.mediquomobiletest.model.WordsRepository
import com.carles.mediquomobiletest.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.concurrent.Executors

val appModule = module {

    single { AppExecutors(Executors.newSingleThreadExecutor(), MainThreadExecutor()) }
    single { WordsRepository(get()) }
    viewModel { MainViewModel(androidApplication(), get()) }

}