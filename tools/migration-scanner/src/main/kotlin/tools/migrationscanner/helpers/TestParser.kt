/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.migrationscanner.helpers

import tools.migrationscanner.TestInfo
import java.io.File

class TestParser(private val root: File) {

    fun findTests(): Map<String, TestInfo> {
        val testDir = File(root, "app/src/androidTest/java")
        if (!testDir.exists()) return emptyMap()

        val result = mutableMapOf<String, TestInfo>()

        testDir.walk().filter { it.extension == "kt" }.forEach { file ->
            val text = file.readText()
            val screenName = file.nameWithoutExtension

            val espresso = listOf("onView(", "withId(", "perform(")
                .count { text.contains(it) }

            val compose = listOf("composeTestRule", "onNode(", "assertExists")
                .count { text.contains(it) }

            result[screenName] = TestInfo(espresso, compose)
        }

        return result
    }
}