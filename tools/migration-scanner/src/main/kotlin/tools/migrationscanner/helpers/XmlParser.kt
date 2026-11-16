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
import java.util.Locale

class XmlParser(private val root: File) {

    fun findXmlScreens(): Map<String, List<String>> {
        val layoutDir = File(root, "app/src/main/res/layout")
        if (!layoutDir.exists()) {
            println("---> No layout directory.")
            return emptyMap()
        }

        val result = mutableMapOf<String, List<String>>()

        layoutDir.walk().filter { it.extension == "xml" }.forEach { xml ->
            val screenName = layoutNameToClassName(xml.nameWithoutExtension)
            println("screen: ${xml.name} -> $screenName")
            result[screenName] = result.getOrDefault(screenName, emptyList()) + xml.absolutePath
            println("result: $result[screenName]")
        }

        return result
    }

    fun countCustomViews(screenName: String): Int {
        val viewDir = File(root, "app/src/main/java")
        return viewDir.walk()
            .filter { it.extension == "kt" }
            .map { it.readText() }
            .count { it.contains("class") && it.contains(": View") }
    }

    private fun layoutNameToClassName(fileName: String): String {
        val knownPrefixes = setOf("activity", "fragment", "dialog", "view", "bottom_sheet")
        val parts = fileName.split('_').filter { it.isNotEmpty() }

        val reorderedParts = if (parts.size > 1 && parts.first().lowercase(Locale.ENGLISH) in knownPrefixes) {
            parts.drop(1) + parts.first()
        } else {
            parts
        }

        return reorderedParts.joinToString("") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
        }
    }
}