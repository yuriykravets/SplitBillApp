package com.partitionsoft.splitbillapp.di

import com.partitionsoft.splitbillapp.core.platform.DatabaseDriverFactory
import com.partitionsoft.splitbillapp.data.mapper.BillHistoryMapper
import com.partitionsoft.splitbillapp.data.repository.LocalSettingsRepository
import com.partitionsoft.splitbillapp.data.repository.SqlDelightBillHistoryRepository
import com.partitionsoft.splitbillapp.db.SplitBillDatabase
import com.partitionsoft.splitbillapp.domain.repository.BillHistoryRepository
import com.partitionsoft.splitbillapp.domain.repository.SettingsRepository
import com.partitionsoft.splitbillapp.domain.usecase.CalculateBillSplitUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ClearBillHistoryUseCase
import com.partitionsoft.splitbillapp.domain.usecase.DeleteBillSplitUseCase
import com.partitionsoft.splitbillapp.domain.usecase.FormatShareSummaryUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ObserveAppLanguageUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ObserveBillHistoryUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ParseInputUseCase
import com.partitionsoft.splitbillapp.domain.usecase.SaveBillSplitUseCase
import com.partitionsoft.splitbillapp.domain.usecase.SetAppLanguageUseCase
import com.partitionsoft.splitbillapp.presentation.calculator.BillCalculatorViewModel
import com.partitionsoft.splitbillapp.presentation.history.HistoryViewModel
import com.partitionsoft.splitbillapp.presentation.settings.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect fun platformModule(): Module

val domainModule = module {
    factoryOf(::ParseInputUseCase)
    factoryOf(::CalculateBillSplitUseCase)
    factoryOf(::FormatShareSummaryUseCase)
    factoryOf(::ObserveBillHistoryUseCase)
    factoryOf(::SaveBillSplitUseCase)
    factoryOf(::DeleteBillSplitUseCase)
    factoryOf(::ClearBillHistoryUseCase)
    factoryOf(::ObserveAppLanguageUseCase)
    factoryOf(::SetAppLanguageUseCase)
}

val dataModule = module {
    single { BillHistoryMapper(calculateBillSplit = get()) }
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        SplitBillDatabase(driver)
    }
    single<BillHistoryRepository> {
        SqlDelightBillHistoryRepository(
            database = get(),
            mapper = get(),
        )
    }
    single<SettingsRepository> {
        LocalSettingsRepository(storage = get())
    }
}

val presentationModule = module {
    viewModelOf(::BillCalculatorViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SettingsViewModel)
}

fun appModules(): List<Module> = listOf(
    platformModule(),
    domainModule,
    dataModule,
    presentationModule,
)
