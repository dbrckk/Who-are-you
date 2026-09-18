This file is a merged representation of a subset of the codebase, containing specifically included files and files not matching ignore patterns, combined into a single document by Repomix.
The content has been processed where content has been compressed (code blocks are separated by ⋮---- delimiter).

# File Summary

## Purpose
This file contains a packed representation of a subset of the repository's contents that is considered the most important context.
It is designed to be easily consumable by AI systems for analysis, code review,
or other automated processes.

## File Format
The content is organized as follows:
1. This summary section
2. Repository information
3. Directory structure
4. Repository files (if enabled)
5. Multiple file entries, each consisting of:
  a. A header with the file path (## File: path/to/file)
  b. The full contents of the file in a code block

## Usage Guidelines
- This file should be treated as read-only. Any changes should be made to the
  original repository files, not this packed version.
- When processing this file, use the file path to distinguish
  between different files in the repository.
- Be aware that this file may contain sensitive information. Handle it with
  the same level of security as you would the original repository.

## Notes
- Some files may have been excluded based on .gitignore rules and Repomix's configuration
- Binary files are not included in this packed representation. Please refer to the Repository Structure section for a complete list of file paths, including binary files
- Only files matching these patterns are included: **/*.{py,js,mjs,cjs,ts,tsx,jsx,java,kt,kts,gd,groovy,gradle,toml,json,yaml,yml,sql,sh}
- Files matching these patterns are excluded: .ai/**, **/node_modules/**, **/.gradle/**, **/build/**, **/dist/**, **/.venv/**, **/__pycache__/**, **/.pytest_cache/**, **/.git/**, **/coverage/**, **/*.lock, **/*.min.js, **/*.map, assets/**, art/**, art_sources/**, marketing/**, colab/**, kaggle/**, discovery-cache.json, health-snapshot.json, history.json
- Files matching patterns in .gitignore are excluded
- Files matching default ignore patterns are excluded
- Content has been compressed - code blocks are separated by ⋮---- delimiter
- Files are sorted by Git change count (files with more changes are at the bottom)

# Directory Structure
```
src/
  main/
    java/
      com/
        whoareyou/
          app/
            baselineprofile/
              BaselineProfileGenerator.kt
              RenderingBenchmark.kt
              StartupBenchmark.kt
build.gradle.kts
```

# Files

## File: src/main/java/com/whoareyou/app/baselineprofile/BaselineProfileGenerator.kt
```kotlin
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
```

## File: src/main/java/com/whoareyou/app/baselineprofile/RenderingBenchmark.kt
```kotlin
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
```

## File: src/main/java/com/whoareyou/app/baselineprofile/StartupBenchmark.kt
```kotlin
package com.whoareyou.app.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartupWithBaselineProfile() = startup(StartupMode.COLD)

    @Test
    fun warmStartupWithBaselineProfile() = startup(StartupMode.WARM)

    private fun startup(mode: StartupMode) = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require
        ),
        startupMode = mode,
        iterations = 10,
        setupBlock = {
            pressHome()
        }
    ) {
        startActivityAndWait()
    }

    private companion object {
        const val PACKAGE_NAME = "com.whoareyou.app"
    }
}
```

## File: build.gradle.kts
```kotlin
plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.whoareyou.app.baselineprofile"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    buildTypes {
        create("benchmark") {
            matchingFallbacks += listOf("release")
        }
    }
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation("androidx.test.ext:junit:1.3.0")
    implementation("androidx.test:runner:1.7.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.5.0")
    implementation("androidx.test.uiautomator:uiautomator:2.4.0")
}
```
