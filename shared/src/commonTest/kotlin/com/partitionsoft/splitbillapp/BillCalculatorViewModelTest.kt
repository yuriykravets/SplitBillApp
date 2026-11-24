package com.partitionsoft.splitbillapp

import com.partitionsoft.splitbillapp.core.localization.AppLanguage
import com.partitionsoft.splitbillapp.core.localization.UkrainianStrings
import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.core.model.Money
import com.partitionsoft.splitbillapp.core.platform.ShareManager
import com.partitionsoft.splitbillapp.core.platform.TimeProvider
import com.partitionsoft.splitbillapp.domain.model.SavedBillSplit
import com.partitionsoft.splitbillapp.domain.model.SplitMode
import com.partitionsoft.splitbillapp.domain.model.TipOption
import com.partitionsoft.splitbillapp.domain.repository.BillHistoryRepository
import com.partitionsoft.splitbillapp.domain.repository.SettingsRepository
import com.partitionsoft.splitbillapp.domain.usecase.CalculateBillSplitUseCase
import com.partitionsoft.splitbillapp.domain.usecase.FormatShareSummaryUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ObserveBillHistoryUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ParseInputUseCase
import com.partitionsoft.splitbillapp.domain.usecase.SaveBillSplitUseCase
import com.partitionsoft.splitbillapp.presentation.calculator.BillCalculatorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BillCalculatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeBillHistoryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var fakeShareManager: FakeShareManager
    private lateinit var viewModel: BillCalculatorViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeBillHistoryRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        fakeShareManager = FakeShareManager()
        val timeProvider = object : TimeProvider {
            override fun currentTimeMillis(): Long = 1_700_000_000_000L
            override fun formatRelativeTimestamp(epochMillis: Long, language: AppLanguage): String = "Just now"
        }
        viewModel = BillCalculatorViewModel(
            parseInput = ParseInputUseCase(),
            calculateBillSplit = CalculateBillSplitUseCase(),
            formatShareSummary = FormatShareSummaryUseCase(),
            saveBillSplit = SaveBillSplitUseCase(fakeRepository, timeProvider),
            observeBillHistory = ObserveBillHistoryUseCase(fakeRepository),
            settingsRepository = fakeSettingsRepository,
            shareManager = fakeShareManager,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun liveRecalculatesWhenInputsChange() = runTest {
        viewModel.onBillAmountChanged("120")
        viewModel.onIncrementPeople() // default 2 -> 3 people
        viewModel.onTipOptionSelected(TipOption.Preset(15))

        val state = viewModel.uiState.value
        val result = assertNotNull(state.calculationResult)
        assertEquals(3, state.participantCount)
        assertEquals(Money(1800), result.tipAmount)
        assertEquals(Money(13800), result.totalAmount)
        assertEquals(Money(4600), result.baseAmountPerPerson)
    }

    @Test
    fun customTipAndAdditionalChargeUpdateCalculation() = runTest {
        viewModel.onBillAmountChanged("80")
        viewModel.onTipOptionSelected(TipOption.Custom)
        viewModel.onCustomTipChanged("12.5")
        viewModel.onToggleAdditionalCharge()
        viewModel.onAdditionalChargeChanged("10")

        val result = assertNotNull(viewModel.uiState.value.calculationResult)
        assertEquals(Money(1000), result.tipAmount) // 12.5% of 80 = 10
        assertEquals(Money(1000), result.additionalCharge)
        assertEquals(Money(10000), result.totalAmount)
        assertEquals(Money(5000), result.baseAmountPerPerson) // 2 people
    }

    @Test
    fun savesToHistoryAndReopensSplit() = runTest {
        viewModel.onBillAmountChanged("90")
        viewModel.onCurrencySelected(Currency.EUR)
        viewModel.onNoteChanged("Pizza night")
        viewModel.onIncrementPeople() // 3 people
        val firstId = viewModel.uiState.value.participants.first().id
        viewModel.onUpdateParticipantName(firstId, "Alex")

        viewModel.onSaveToHistory()
        advanceUntilIdle()

        assertEquals(1, fakeRepository.savedItems.size)
        assertTrue(viewModel.uiState.value.isSavedUpToDate)
        assertEquals(1, viewModel.uiState.value.historyCount)

        val saved = fakeRepository.savedItems.first()
        viewModel.onReset()
        assertNull(viewModel.uiState.value.calculationResult)
        assertEquals(Currency.EUR, viewModel.uiState.value.currency)

        viewModel.onReopenSavedSplit(saved)
        val reopened = viewModel.uiState.value
        assertEquals("90", reopened.billAmountText)
        assertEquals("Pizza night", reopened.note)
        assertEquals(3, reopened.participantCount)
        assertEquals("Alex", reopened.participants.first().name)
        assertNotNull(reopened.calculationResult)
    }

    @Test
    fun shareResultDispatchesFormattedSummary() = runTest {
        viewModel.onBillAmountChanged("120")
        viewModel.onIncrementPeople()
        viewModel.onShareResult()

        assertEquals(1, fakeShareManager.sharedTexts.size)
        assertTrue(fakeShareManager.sharedTexts.first().contains("Each person pays: $46"))
    }

    @Test
    fun switchesToUkrainianForShareSummaryAndPlurals() = runTest {
        fakeSettingsRepository.setLanguage(AppLanguage.UKRAINIAN)
        viewModel.onBillAmountChanged("120")
        viewModel.onIncrementPeople()
        viewModel.onShareResult()

        assertEquals(1, fakeShareManager.sharedTexts.size)
        assertTrue(fakeShareManager.sharedTexts.first().contains("Кожен сплачує: $46"))
        assertEquals("1 особа", UkrainianStrings.peopleCountLabel(1))
        assertEquals("3 особи", UkrainianStrings.peopleCountLabel(3))
        assertEquals("5 осіб", UkrainianStrings.peopleCountLabel(5))
        assertEquals("11 осіб", UkrainianStrings.peopleCountLabel(11))
        assertEquals("21 особа", UkrainianStrings.peopleCountLabel(21))
    }

    @Test
    fun itemizedSplitFlowSavesAndReopensCleanly() = runTest {
        viewModel.onSplitModeChanged(SplitMode.BY_ITEMS)
        assertTrue(viewModel.uiState.value.showParticipantNames)

        val p1 = viewModel.uiState.value.participants[0].id
        val p2 = viewModel.uiState.value.participants[1].id
        viewModel.onUpdateParticipantName(p2, "Alex")

        viewModel.onSaveItem("Tea", "4", setOf(p1))
        viewModel.onSaveItem("Spaghetti", "18", setOf(p2))
        viewModel.onSaveItem("Wine", "12", setOf(p2))

        val state = viewModel.uiState.value
        val result = assertNotNull(state.calculationResult)
        assertEquals(SplitMode.BY_ITEMS, result.splitMode)
        assertEquals(Money(3400), result.billAmount)
        assertEquals(Money(510), result.tipAmount)
        assertEquals(Money(460), result.participantShares[0].amount)
        assertEquals(Money(3450), result.participantShares[1].amount)

        viewModel.onSaveToHistory()
        advanceUntilIdle()
        val saved = fakeRepository.savedItems.first()

        viewModel.onReset()
        assertEquals(SplitMode.BY_ITEMS, viewModel.uiState.value.splitMode)
        assertTrue(viewModel.uiState.value.items.isEmpty())
        assertNull(viewModel.uiState.value.calculationResult)

        viewModel.onReopenSavedSplit(saved)
        val reopened = viewModel.uiState.value
        assertEquals(SplitMode.BY_ITEMS, reopened.splitMode)
        assertEquals(3, reopened.items.size)
        assertEquals(Money(3910), assertNotNull(reopened.calculationResult).totalAmount)
    }
}

private class FakeSettingsRepository : SettingsRepository {
    private val lang = MutableStateFlow(AppLanguage.ENGLISH)
    override fun observeLanguage(): Flow<AppLanguage> = lang.asStateFlow()
    override fun currentLanguage(): AppLanguage = lang.value
    override suspend fun setLanguage(language: AppLanguage) {
        lang.value = language
    }
}

private class FakeBillHistoryRepository : BillHistoryRepository {
    private val items = MutableStateFlow<List<SavedBillSplit>>(emptyList())
    val savedItems: List<SavedBillSplit> get() = items.value

    override fun observeHistory(): Flow<List<SavedBillSplit>> = items.asStateFlow()

    override suspend fun save(split: SavedBillSplit) {
        items.update { current ->
            listOf(split) + current.filterNot { it.id == split.id }
        }
    }

    override suspend fun deleteById(id: String) {
        items.update { current -> current.filterNot { it.id == id } }
    }

    override suspend fun clearAll() {
        items.value = emptyList()
    }
}

private class FakeShareManager : ShareManager {
    val sharedTexts = mutableListOf<String>()
    override fun shareText(text: String, title: String) {
        sharedTexts.add(text)
    }
}
