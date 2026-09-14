package com.whoareyou.app.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class RenderingBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun discoverScrollFrames() = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require
        ),
        iterations = 7,
        setupBlock = {
            device.executeShellCommand("am force-stop $PACKAGE_NAME")
            startActivityAndWait()
            device.findObject(By.res("onboarding_start"))?.click()
            check(device.wait(Until.hasObject(By.res("discover_list")), 5_000)) {
                "Discover list was not ready"
            }
        }
    ) {
        val list = device.findObject(By.res("discover_list"))
        list.setGestureMargin(device.displayWidth / 5)
        list.fling(Direction.DOWN)
        device.waitForIdle()
        list.fling(Direction.UP)
        device.waitForIdle()
    }

    @Test
    fun quizToResultFrames() = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require
        ),
        iterations = 7,
        setupBlock = {
            device.executeShellCommand("am force-stop $PACKAGE_NAME")
            startActivityAndWait()
            device.findObject(By.res("onboarding_start"))?.click()
            check(device.wait(Until.hasObject(By.res("discover_next_quiz")), 5_000)) {
                "Next quiz card was not ready"
            }
            device.findObject(By.res("discover_next_quiz")).click()
            check(device.wait(Until.hasObject(By.res("quiz_answer_0")), 5_000)) {
                "Quiz answers were not ready"
            }
        }
    ) {
        repeat(MAX_ANSWER_TAPS) {
            if (device.hasObject(By.res("result_score"))) return@measureRepeated
            val answer = device.findObject(By.res("quiz_answer_0")) ?: return@repeat
            answer.click()
            device.waitForIdle()
        }
        check(device.wait(Until.hasObject(By.res("result_score")), 5_000)) {
            "Result screen did not become ready"
        }
    }

    @Test
    fun resultScrollFrames() = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require
        ),
        iterations = 7,
        setupBlock = {
            device.executeShellCommand("am force-stop $PACKAGE_NAME")
            startActivityAndWait()
            device.findObject(By.res("onboarding_start"))?.click()
            check(device.wait(Until.hasObject(By.res("discover_next_quiz")), 5_000)) {
                "Next quiz card was not ready"
            }
            device.findObject(By.res("discover_next_quiz")).click()
            check(device.wait(Until.hasObject(By.res("quiz_answer_0")), 5_000)) {
                "Quiz answers were not ready"
            }
            repeat(MAX_ANSWER_TAPS) {
                if (device.hasObject(By.res("result_score"))) return@repeat
                device.findObject(By.res("quiz_answer_0"))?.click()
                device.waitForIdle()
            }
            check(device.wait(Until.hasObject(By.res("result_score")), 5_000)) {
                "Result screen did not become ready"
            }
        }
    ) {
        device.swipe(
            device.displayWidth / 2,
            device.displayHeight * 4 / 5,
            device.displayWidth / 2,
            device.displayHeight / 5,
            24
        )
        device.waitForIdle()
        device.swipe(
            device.displayWidth / 2,
            device.displayHeight / 5,
            device.displayWidth / 2,
            device.displayHeight * 4 / 5,
            24
        )
        device.waitForIdle()
    }

    private companion object {
        const val PACKAGE_NAME = "com.whoareyou.app"
        const val MAX_ANSWER_TAPS = 24
    }
}
