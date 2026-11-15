package tools.migrationscanner

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
