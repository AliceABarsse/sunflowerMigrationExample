package tools.migrationscanner

import com.google.gson.Gson
import tools.migrationscanner.helpers.ComplexityCalculator
import tools.migrationscanner.helpers.ComposeParser
import tools.migrationscanner.helpers.GitParser
import tools.migrationscanner.helpers.NavigationParser
import tools.migrationscanner.helpers.TestParser
import tools.migrationscanner.helpers.XmlParser
import java.io.File

fun main() {
    val projectRoot = File("${System.getProperty("user.dir")}/../..")
    val scanner = MigrationScanner(projectRoot)
    val results = scanner.scan()

    val outputDir = File(projectRoot, "build/scan-results")
    outputDir.mkdirs()

    val outputFile = File(outputDir, "screens.json")
    outputFile.writeText(Gson().toJson(results))

    println("Migration scan complete.\nResults written to ${outputFile.absolutePath}")
}

class MigrationScanner(private val root: File) {

    private val xmlParser = XmlParser(root)
    private val gitParser = GitParser(root)
    private val complexity = ComplexityCalculator()

    private val composeParser = ComposeParser(root)
    private val navigationParser = NavigationParser(root)
    private val testParser = TestParser(root)

    fun scan(): List<ScreenInfo> {
        val xmlScreens = xmlParser.findXmlScreens()
        val composeScreens = composeParser.findComposeScreens()
        val navigationTargets = navigationParser.findNavigationTargets()
        val testInfo = testParser.findTests()

        val allScreens = (xmlScreens.keys + composeScreens.keys + navigationTargets.keys)
            .distinct()

        return allScreens.map { screenName ->
            val xmlFiles = xmlScreens[screenName] ?: emptyList()
            val composeFiles = composeScreens[screenName] ?: emptyList<String>()
            val navTargets = navigationTargets[screenName] ?: 0
            val tests = testInfo[screenName] ?: TestInfo(0, 0)

            val xmlLines = countLines(xmlFiles)
            val composeLines = countLines(composeFiles)

            val status = when {
                xmlFiles.isEmpty() && composeFiles.isNotEmpty() -> "compose"
                xmlFiles.isNotEmpty() && composeFiles.isNotEmpty() -> "mixed"
                else -> "xml"
            }

            val migrationMeta = gitParser.findMigrationMetadata(xmlFiles, composeFiles)

            ScreenInfo(
                screen = screenName,
                status = status,
                xmlFiles = xmlFiles,
                composeFiles = composeFiles,
                customViews = xmlParser.countCustomViews(screenName),
                navigationTargets = navTargets,
                migratedBy = migrationMeta.author,
                migrationDate = migrationMeta.date,
                testsAdded = tests.composeTests,
                complexityScore = complexity.calculate(
                    xmlFiles = xmlFiles,
                    composeFiles = emptyList(),
                    customViews = 0,
                    navigationTargets = 0,
                    totalLines = xmlLines + composeLines,
                )
            )
        }
    }

    private fun countLines(files: List<String>): Int {
        return files.sumOf { path ->
            try {
                File(path).useLines { it.count() }
            } catch (e: Exception) {
                0 // file missing or unreadable
            }
        }
    }
}