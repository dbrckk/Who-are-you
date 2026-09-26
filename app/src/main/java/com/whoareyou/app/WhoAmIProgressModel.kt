package com.whoareyou.app

import androidx.annotation.StringRes

object WhoAmIProgressModel {
    @StringRes
    fun domainLabel(domain: TraitDomain): Int = when (domain) {
        TraitDomain.SOCIAL -> R.string.who_am_i_domain_social
        TraitDomain.EMOTIONAL -> R.string.who_am_i_domain_emotional
        TraitDomain.THINKING -> R.string.who_am_i_domain_thinking
        TraitDomain.GROWTH -> R.string.who_am_i_domain_growth
        TraitDomain.SELF_MANAGEMENT -> R.string.who_am_i_domain_self_management
    }

    @StringRes
    fun trendLabel(trend: PersonalTrend): Int? = when (trend) {
        PersonalTrend.RISING -> R.string.who_am_i_trend_rising
        PersonalTrend.FALLING -> R.string.who_am_i_trend_falling
        PersonalTrend.VARIABLE -> R.string.who_am_i_trend_variable
        PersonalTrend.UNKNOWN,
        PersonalTrend.STABLE -> null
    }
}
