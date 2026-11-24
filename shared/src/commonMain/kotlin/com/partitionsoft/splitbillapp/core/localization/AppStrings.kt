package com.partitionsoft.splitbillapp.core.localization

import androidx.compose.runtime.staticCompositionLocalOf
import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.domain.model.ValidationError

data class AppStrings(
    val language: AppLanguage,
    val appTitle: String,
    // Top bar & navigation
    val changeCurrencyCd: String,
    val resetBillCd: String,
    val historyCd: String,
    val settingsCd: String,
    val backToCalculatorCd: String,
    // Hero card
    val totalToPay: String,
    val perPersonFrom: String,
    val eachPersonPays: String,
    val billLabel: String,
    val tipLabel: String,
    val taxAndExtrasLabel: String,
    val totalLabel: String,
    val totalBillLabel: String,
    val shareSplit: String,
    val save: String,
    val saved: String,
    val savedToHistoryMessage: String,
    // Bill input section
    val billAmountTitle: String,
    val clearBillAmountCd: String,
    val addTaxOrFee: String,
    val taxOrFeeAdded: String,
    val taxOrServiceChargeLabel: String,
    val notePlaceholder: String,
    // Tip section
    val tipTitle: String,
    val customTip: String,
    val customTipPercentageLabel: String,
    val customTipPlaceholder: String,
    // Participants section
    val splitBetweenTitle: String,
    val equalSplitSubtitle: String,
    val decreasePeopleCd: String,
    val increasePeopleCd: String,
    val assignNames: String,
    val hideParticipantNames: String,
    val tapToRename: String,
    val defaultSelfName: String,
    val renameDialogPlaceholder: String,
    val cancel: String,
    val quickParticipantNames: List<String>,
    // Currency picker
    val selectCurrencyTitle: String,
    val selectedCd: String,
    // History screen
    val historyTitle: String,
    val clearAll: String,
    val clearHistoryDialogTitle: String,
    val clearHistoryDialogBody: String,
    val clearConfirm: String,
    val deleteSavedSplitCd: String,
    val eachPays: String,
    val emptyHistoryTitle: String,
    val emptyHistorySubtitle: String,
    // Settings screen
    val settingsTitle: String,
    val languageSectionTitle: String,
    val languageSectionSubtitle: String,
    val aboutSectionTitle: String,
    val aboutDescription: String,
    // Share summary
    val shareRestaurantBill: String,
    val shareTip: String,
    val shareTaxAndExtras: String,
    val shareTotal: String,
    val sharePeople: String,
    val shareBreakdown: String,
    val shareEachPersonPays: String,
    // Split mode & itemized orders
    val splitModeEqual: String,
    val splitModeByItems: String,
    val itemizedSplitSubtitle: String,
    val ordersTitle: String,
    val addItem: String,
    val editItemTitle: String,
    val addItemTitle: String,
    val itemNameLabel: String,
    val itemNamePlaceholder: String,
    val itemPriceLabel: String,
    val orderedByLabel: String,
    val everyoneChip: String,
    val noItemsYetTitle: String,
    val noItemsYetSubtitle: String,
    val deleteItemCd: String,
    val sharedByAll: String,
    val noOrdersAssigned: String,
    val byItemsBadge: String,
    val quickItemPresets: List<String>,
    // Relative time
    val justNow: String,
) {
    fun peopleCountLabel(count: Int): String = when (language) {
        AppLanguage.ENGLISH -> if (count == 1) "1 person" else "$count people"
        AppLanguage.UKRAINIAN -> "$count ${ukrainianPlural(count, "особа", "особи", "осіб")}"
    }

    fun itemsCountLabel(count: Int): String = when (language) {
        AppLanguage.ENGLISH -> if (count == 1) "1 item" else "$count items"
        AppLanguage.UKRAINIAN -> "$count ${ukrainianPlural(count, "позиція", "позиції", "позицій")}"
    }

    fun splitWaysHint(count: Int, perPersonFormatted: String): String = when (language) {
        AppLanguage.ENGLISH -> "Split $count ways ($perPersonFormatted each)"
        AppLanguage.UKRAINIAN -> "Порівну на $count ($perPersonFormatted з кожного)"
    }

    fun sharesRangeLabel(minFormatted: String, maxFormatted: String): String = when (language) {
        AppLanguage.ENGLISH -> "Per person: $minFormatted – $maxFormatted"
        AppLanguage.UKRAINIAN -> "З кожного: $minFormatted – $maxFormatted"
    }

    fun remainderExplanation(remainderCount: Int, unitLabel: String): String = when (language) {
        AppLanguage.ENGLISH -> {
            val verb = if (remainderCount == 1) "person pays" else "people pay"
            "$remainderCount $verb +$unitLabel so shares match total"
        }
        AppLanguage.UKRAINIAN -> {
            val noun = ukrainianPlural(remainderCount, "особа сплачує", "особи сплачують", "осіб сплачують")
            "$remainderCount $noun +$unitLabel, щоб сума збіглася"
        }
    }

    fun defaultParticipantName(index: Int): String = when (language) {
        AppLanguage.ENGLISH -> if (index == 0) "You" else "Person ${index + 1}"
        AppLanguage.UKRAINIAN -> if (index == 0) "Ви" else "Учасник ${index + 1}"
    }

    fun renameDialogTitle(displayName: String): String = when (language) {
        AppLanguage.ENGLISH -> "Name for $displayName"
        AppLanguage.UKRAINIAN -> "Ім'я для: $displayName"
    }

    fun defaultSplitTitle(count: Int): String = when (language) {
        AppLanguage.ENGLISH -> "Split (${peopleCountLabel(count)})"
        AppLanguage.UKRAINIAN -> "Рахунок (${peopleCountLabel(count)})"
    }

    fun minutesAgo(minutes: Long): String = when (language) {
        AppLanguage.ENGLISH -> "${minutes}m ago"
        AppLanguage.UKRAINIAN -> "$minutes хв тому"
    }

    fun currencyDisplayName(currency: Currency): String = when (language) {
        AppLanguage.ENGLISH -> currency.displayName
        AppLanguage.UKRAINIAN -> when (currency) {
            Currency.USD -> "Долар США"
            Currency.EUR -> "Євро"
            Currency.GBP -> "Фунт стерлінгів"
            Currency.UAH -> "Українська гривня"
            Currency.PLN -> "Польський злотий"
            Currency.CAD -> "Канадський долар"
            Currency.AUD -> "Австралійський долар"
            Currency.CHF -> "Швейцарський франк"
            Currency.JPY -> "Японська єна"
        }
    }

    fun validationMessage(error: ValidationError, currency: Currency): String = when (language) {
        AppLanguage.ENGLISH -> when (error) {
            ValidationError.EmptyBillAmount -> "Enter a bill amount"
            ValidationError.InvalidBillAmount ->
                if (currency.fractionDigits == 0) "Enter a whole number for ${currency.code}"
                else "Enter a valid amount (up to ${currency.fractionDigits} decimals)"
            ValidationError.NonPositiveBillAmount -> "Amount must be greater than 0"
            ValidationError.AmountTooLarge -> "Amount is too large"
            ValidationError.InvalidTipPercentage -> "Enter a valid percentage"
            ValidationError.TipPercentageOutOfRange -> "Tip must be between 0% and 200%"
            ValidationError.InvalidAdditionalCharge -> "Enter a valid charge amount"
            ValidationError.NegativeAdditionalCharge -> "Additional charge cannot be negative"
            ValidationError.InvalidParticipantCount -> "Choose at least 1 person"
            ValidationError.NoItemsAdded -> "Add at least one item"
            ValidationError.UnassignedItems -> "Assign every item to at least one person"
        }

        AppLanguage.UKRAINIAN -> when (error) {
            ValidationError.EmptyBillAmount -> "Введіть суму рахунку"
            ValidationError.InvalidBillAmount ->
                if (currency.fractionDigits == 0) "Введіть ціле число для ${currency.code}"
                else "Введіть коректну суму (до ${currency.fractionDigits} знаків після коми)"
            ValidationError.NonPositiveBillAmount -> "Сума має бути більшою за 0"
            ValidationError.AmountTooLarge -> "Сума занадто велика"
            ValidationError.InvalidTipPercentage -> "Введіть коректний відсоток"
            ValidationError.TipPercentageOutOfRange -> "Чайові мають бути від 0% до 200%"
            ValidationError.InvalidAdditionalCharge -> "Введіть коректну суму доплати"
            ValidationError.NegativeAdditionalCharge -> "Доплата не може бути від'ємною"
            ValidationError.InvalidParticipantCount -> "Оберіть щонайменше 1 особу"
            ValidationError.NoItemsAdded -> "Додайте щонайменше одну позицію"
            ValidationError.UnassignedItems -> "Призначте кожну позицію хоча б одній особі"
        }
    }

    private fun ukrainianPlural(n: Int, one: String, few: String, many: String): String {
        val mod100 = n % 100
        val mod10 = n % 10
        return when {
            mod100 in 11..14 -> many
            mod10 == 1 -> one
            mod10 in 2..4 -> few
            else -> many
        }
    }
}

val EnglishStrings = AppStrings(
    language = AppLanguage.ENGLISH,
    appTitle = "Split Bill",
    changeCurrencyCd = "Change currency",
    resetBillCd = "Reset bill",
    historyCd = "Calculation history",
    settingsCd = "Settings",
    backToCalculatorCd = "Back to calculator",
    totalToPay = "Total to pay",
    perPersonFrom = "Per person (from)",
    eachPersonPays = "Each person pays",
    billLabel = "Bill",
    tipLabel = "Tip",
    taxAndExtrasLabel = "Tax & extras",
    totalLabel = "Total",
    totalBillLabel = "Total bill",
    shareSplit = "Share split",
    save = "Save",
    saved = "Saved",
    savedToHistoryMessage = "Saved to history",
    billAmountTitle = "Bill amount",
    clearBillAmountCd = "Clear bill amount",
    addTaxOrFee = "+ Tax or fee",
    taxOrFeeAdded = "Tax / fee added",
    taxOrServiceChargeLabel = "Tax or service charge",
    notePlaceholder = "Add a note (e.g. Friday dinner)",
    tipTitle = "Tip",
    customTip = "Custom",
    customTipPercentageLabel = "Custom tip percentage",
    customTipPlaceholder = "e.g. 12.5",
    splitBetweenTitle = "Split between",
    equalSplitSubtitle = "Equal split per person",
    decreasePeopleCd = "Decrease people",
    increasePeopleCd = "Increase people",
    assignNames = "Assign names",
    hideParticipantNames = "Hide participant names",
    tapToRename = "Tap to rename",
    defaultSelfName = "You",
    renameDialogPlaceholder = "Enter a name",
    cancel = "Cancel",
    quickParticipantNames = listOf("You", "Alex", "Maria", "John", "Sam", "Kate"),
    selectCurrencyTitle = "Select currency",
    selectedCd = "Selected",
    historyTitle = "History",
    clearAll = "Clear all",
    clearHistoryDialogTitle = "Clear all history?",
    clearHistoryDialogBody = "All saved bill splits on this device will be permanently removed.",
    clearConfirm = "Clear",
    deleteSavedSplitCd = "Delete saved split",
    eachPays = "Each pays",
    emptyHistoryTitle = "No saved splits yet",
    emptyHistorySubtitle = "Tap Save on any calculation to keep it here and reopen it later.",
    settingsTitle = "Settings",
    languageSectionTitle = "App language",
    languageSectionSubtitle = "Choose your preferred language for the interface and shared summaries.",
    aboutSectionTitle = "About",
    aboutDescription = "Split Bill makes splitting restaurant checks, tips, and extras quick and accurate.",
    shareRestaurantBill = "Restaurant bill",
    shareTip = "Tip",
    shareTaxAndExtras = "Tax & extras",
    shareTotal = "Total",
    sharePeople = "People",
    shareBreakdown = "Breakdown",
    shareEachPersonPays = "Each person pays",
    splitModeEqual = "Equal",
    splitModeByItems = "By items",
    itemizedSplitSubtitle = "Proportional tip & extras",
    ordersTitle = "Orders",
    addItem = "Add item",
    editItemTitle = "Edit item",
    addItemTitle = "Add order item",
    itemNameLabel = "Item name",
    itemNamePlaceholder = "e.g. Spaghetti, Tea, Wine",
    itemPriceLabel = "Price",
    orderedByLabel = "Ordered by",
    everyoneChip = "Everyone",
    noItemsYetTitle = "No items added yet",
    noItemsYetSubtitle = "Add each dish or drink and pick who ordered it — shared items split evenly.",
    deleteItemCd = "Delete item",
    sharedByAll = "Shared by all",
    noOrdersAssigned = "No items yet",
    byItemsBadge = "By items",
    quickItemPresets = listOf("Tea", "Coffee", "Pasta", "Pizza", "Salad", "Wine", "Dessert"),
    justNow = "Just now",
)

val UkrainianStrings = AppStrings(
    language = AppLanguage.UKRAINIAN,
    appTitle = "Split Bill",
    changeCurrencyCd = "Змінити валюту",
    resetBillCd = "Скинути рахунок",
    historyCd = "Історія розрахунків",
    settingsCd = "Налаштування",
    backToCalculatorCd = "Повернутися до калькулятора",
    totalToPay = "До сплати",
    perPersonFrom = "З кожного (від)",
    eachPersonPays = "Кожен сплачує",
    billLabel = "Рахунок",
    tipLabel = "Чайові",
    taxAndExtrasLabel = "Податок / збір",
    totalLabel = "Разом",
    totalBillLabel = "Загальна сума",
    shareSplit = "Поділитися",
    save = "Зберегти",
    saved = "Збережено",
    savedToHistoryMessage = "Збережено в історію",
    billAmountTitle = "Сума рахунку",
    clearBillAmountCd = "Очистити суму",
    addTaxOrFee = "+ Податок або збір",
    taxOrFeeAdded = "Податок / збір додано",
    taxOrServiceChargeLabel = "Податок або сервісний збір",
    notePlaceholder = "Додайте нотатку (напр. Вечеря в п'ятницю)",
    tipTitle = "Чайові",
    customTip = "Інший %",
    customTipPercentageLabel = "Власний відсоток чайових",
    customTipPlaceholder = "напр. 12.5",
    splitBetweenTitle = "Поділити між",
    equalSplitSubtitle = "Порівну на кожного",
    decreasePeopleCd = "Зменшити кількість людей",
    increasePeopleCd = "Збільшити кількість людей",
    assignNames = "Додати імена",
    hideParticipantNames = "Сховати імена",
    tapToRename = "Натисніть, щоб змінити",
    defaultSelfName = "Ви",
    renameDialogPlaceholder = "Введіть ім'я",
    cancel = "Скасувати",
    quickParticipantNames = listOf("Ви", "Олексій", "Марія", "Андрій", "Анна", "Дмитро"),
    selectCurrencyTitle = "Оберіть валюту",
    selectedCd = "Обрано",
    historyTitle = "Історія",
    clearAll = "Очистити все",
    clearHistoryDialogTitle = "Очистити всю історію?",
    clearHistoryDialogBody = "Усі збережені розрахунки на цьому пристрої будуть видалені назавжди.",
    clearConfirm = "Очистити",
    deleteSavedSplitCd = "Видалити запис",
    eachPays = "З кожного",
    emptyHistoryTitle = "Поки що немає збережених рахунків",
    emptyHistorySubtitle = "Натисніть «Зберегти» на будь-якому розрахунку, щоб відкривати його пізніше.",
    settingsTitle = "Налаштування",
    languageSectionTitle = "Мова додатку",
    languageSectionSubtitle = "Оберіть зручну мову інтерфейсу та тексту для надсилання друзям.",
    aboutSectionTitle = "Про додаток",
    aboutDescription = "Split Bill допомагає швидко й точно ділити ресторанні рахунки та чайові між друзями.",
    shareRestaurantBill = "Рахунок у ресторані",
    shareTip = "Чайові",
    shareTaxAndExtras = "Податок / збір",
    shareTotal = "Разом",
    sharePeople = "Людей",
    shareBreakdown = "Розподіл",
    shareEachPersonPays = "Кожен сплачує",
    splitModeEqual = "Порівну",
    splitModeByItems = "За позиціями",
    itemizedSplitSubtitle = "Чайові й збори пропорційно",
    ordersTitle = "Замовлення",
    addItem = "Додати позицію",
    editItemTitle = "Редагувати позицію",
    addItemTitle = "Додати позицію",
    itemNameLabel = "Назва страви або напою",
    itemNamePlaceholder = "напр. Паста, Чай, Вино",
    itemPriceLabel = "Ціна",
    orderedByLabel = "Хто замовляв",
    everyoneChip = "Усі разом",
    noItemsYetTitle = "Позицій ще немає",
    noItemsYetSubtitle = "Додайте страви чи напої та оберіть, хто їх замовляв — спільні діляться порівну.",
    deleteItemCd = "Видалити позицію",
    sharedByAll = "Спільне для всіх",
    noOrdersAssigned = "Немає позицій",
    byItemsBadge = "За позиціями",
    quickItemPresets = listOf("Чай", "Кава", "Паста", "Піца", "Салат", "Вино", "Десерт"),
    justNow = "Щойно",
)

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.UKRAINIAN -> UkrainianStrings
}

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }
