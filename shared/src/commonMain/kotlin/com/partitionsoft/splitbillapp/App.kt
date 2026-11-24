package com.partitionsoft.splitbillapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partitionsoft.splitbillapp.core.localization.LocalAppStrings
import com.partitionsoft.splitbillapp.core.localization.stringsFor
import com.partitionsoft.splitbillapp.di.appModules
import com.partitionsoft.splitbillapp.presentation.calculator.BillCalculatorScreen
import com.partitionsoft.splitbillapp.presentation.calculator.BillCalculatorViewModel
import com.partitionsoft.splitbillapp.presentation.history.HistoryScreen
import com.partitionsoft.splitbillapp.presentation.history.HistoryViewModel
import com.partitionsoft.splitbillapp.presentation.settings.SettingsScreen
import com.partitionsoft.splitbillapp.presentation.settings.SettingsViewModel
import com.partitionsoft.splitbillapp.presentation.theme.SplitBillTheme
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

private enum class AppDestination {
    Calculator,
    History,
    Settings,
}

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration {
            modules(appModules())
        },
    ) {
        SplitBillTheme {
            SplitBillRoot()
        }
    }
}

@Composable
private fun SplitBillRoot() {
    var destination by rememberSaveable { mutableStateOf(AppDestination.Calculator) }
    val calculatorViewModel: BillCalculatorViewModel = koinViewModel()
    val historyViewModel: HistoryViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val language by settingsViewModel.language.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalAppStrings provides stringsFor(language)) {
        AnimatedContent(
            targetState = destination,
            transitionSpec = {
                if (targetState != AppDestination.Calculator) {
                    (slideInHorizontally(tween(240)) { it / 4 } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(220)) { -it / 6 } + fadeOut(tween(180)))
                } else {
                    (slideInHorizontally(tween(240)) { -it / 4 } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(220)) { it / 6 } + fadeOut(tween(180)))
                }
            },
            label = "appDestination",
        ) { screen ->
            when (screen) {
                AppDestination.Calculator -> {
                    BillCalculatorScreen(
                        viewModel = calculatorViewModel,
                        onOpenHistory = { destination = AppDestination.History },
                        onOpenSettings = { destination = AppDestination.Settings },
                    )
                }

                AppDestination.History -> {
                    HistoryScreen(
                        viewModel = historyViewModel,
                        onBack = { destination = AppDestination.Calculator },
                        onSelectSplit = { saved ->
                            calculatorViewModel.onReopenSavedSplit(saved)
                            destination = AppDestination.Calculator
                        },
                    )
                }

                AppDestination.Settings -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = { destination = AppDestination.Calculator },
                    )
                }
            }
        }
    }
}
