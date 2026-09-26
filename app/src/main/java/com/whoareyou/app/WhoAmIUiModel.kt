package com.whoareyou.app

import androidx.annotation.StringRes

data class WhoAmITraitRow(
    @StringRes val labelRes: Int?,
    @StringRes val certaintyRes: Int,
    val score: Int,
    val confidence: Int
)

object WhoAmIUiModel {
    fun traitRow(trait: PersonalTrait): WhoAmITraitRow = WhoAmITraitRow(
        labelRes = WhoAmICopy.traitLabelResource(trait.traitId),
        certaintyRes = when (trait.certainty) {
            PersonalCertainty.UNKNOWN,
            PersonalCertainty.EXPLORING -> R.string.who_am_i_certainty_exploring
            PersonalCertainty.LIKELY -> R.string.who_am_i_certainty_likely
            PersonalCertainty.ESTABLISHED -> R.string.who_am_i_certainty_established
        },
        score = trait.score,
        confidence = trait.confidence
    )
}
