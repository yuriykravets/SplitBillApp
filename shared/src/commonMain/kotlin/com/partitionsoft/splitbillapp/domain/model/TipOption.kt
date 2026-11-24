package com.partitionsoft.splitbillapp.domain.model

sealed interface TipOption {
    data class Preset(val percent: Int) : TipOption
    data object Custom : TipOption

    companion object {
        val presets: List<Preset> = listOf(
            Preset(0),
            Preset(5),
            Preset(10),
            Preset(15),
            Preset(20),
        )
    }
}
