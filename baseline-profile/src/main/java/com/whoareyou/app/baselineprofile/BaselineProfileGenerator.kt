package com.whoareyou.app.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startupAndDiscover() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME
    ) {
        pressHome()
        startActivityAndWait()
        device.findObject(By.res("onboarding_start"))?.click()
        check(device.wait(Until.hasObject(By.res("app_screen_discover")), 5_000)) {
            "Discover screen did not become ready during Baseline Profile collection"
        }
    }

    private companion object {
        const val PACKAGE_NAME = "com.whoareyou.app"
    }
}
