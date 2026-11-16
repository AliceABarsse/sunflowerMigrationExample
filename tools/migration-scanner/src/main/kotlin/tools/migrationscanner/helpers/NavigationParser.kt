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

import java.io.File

class NavigationParser(private val root: File) {

    fun findNavigationTargets(): Map<String, Int> {
        val navDir = File(root, "app/src/main/res/navigation")
        if (!navDir.exists()) return emptyMap()

        val result = mutableMapOf<String, Int>()

        navDir.walk().filter { it.extension == "xml" }.forEach { xml ->
            val text = xml.readText()
            Regex("""android:name="([^"]+)"""")
                .findAll(text)
                .forEach { match ->
                    val className = match.groupValues[1].substringAfterLast(".")
                    result[className] = result.getOrDefault(className, 0) + 1
                }
        }

        return result
    }
}