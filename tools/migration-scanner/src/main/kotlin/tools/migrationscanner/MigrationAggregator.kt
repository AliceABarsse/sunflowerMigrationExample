package tools.migrationscanner
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Summary (
    @SerialName("generated_at") val generatedAt: String,
    @SerialName("total_screens")val totalScreens: Int,
    @SerialName("compose_screens")val totalComposeScreens: Int,
    @SerialName("xml_screens")val totalXmlScreens: Int,
    @SerialName("mixed_screens")val totalMixedScreens: Int,
)

@Serializable
data class FullData(
    val summary: Summary,
    val screens: List<AggregatedScreen>,
)

@Serializable
data class AggregatedScreen(
    val screen: String,
    val status: String,
    val xmlFiles: List<String>,
    val composeFiles: List<String>,
    val customViews: Int,
    val navigationTargets: String?,
    val complexityScore: Double,
    val migratedBy: String?,
    val migrationDate: String?,
    val testsAdded: Int,
)

class MigrationAggregator {

    private val json = Json { prettyPrint = true }

    fun aggregate(
                  root: File,
    ) {

        val scanDir = File(root, "build/scan-results")
        val outputFile = File(scanDir, "screens_aggregated.json")

        if (!scanDir.exists()) {
            println("No scan-results directory found at: ${scanDir.absolutePath}")
            return
        }
        val inputFile = scanDir.listFiles { f -> f.name == "screens.json" }?.firstOrNull()

        val rawScreens = json.decodeFromString<List<ScreenInfo>>(inputFile!!.readText())
        var totalScreens = 0
        var totalXmlScreens = 0
        var totalComposeScreens = 0
        var totalMixedScreens = 0

        val aggregated = rawScreens.associate { raw ->

            val xmlCount = raw.xmlFiles.size
            val composeFiles = raw.composeFiles.size
            totalScreens += 1 // or more?

            when {
                raw.xmlFiles.isEmpty() -> totalComposeScreens += composeFiles
                raw.composeFiles.isEmpty() -> totalXmlScreens += raw.xmlFiles.size
                else -> totalMixedScreens += 1 // or more?
            }

            val migrationStatus =
                when {
                    xmlCount > 0 && composeFiles > 0 -> "mixed"
                    xmlCount > 0 -> "xml"
                    composeFiles > 0 -> "compose"
                    else -> "unknown"
                }


            raw.screen to AggregatedScreen(
                screen = raw.screen,
                status = migrationStatus,
                xmlFiles = raw.xmlFiles,
                composeFiles = raw.composeFiles,
                customViews = raw.customViews,
                navigationTargets = null, // TODO fix mapping
                complexityScore = raw.complexityScore,
                migratedBy = raw.migratedBy,
                migrationDate = raw.migrationDate,
                testsAdded = raw.testsAdded,
            )
        }
        val finalData = FullData(
            summary = Summary(
                generatedAt = "the date here",
                totalScreens = totalScreens,
                totalComposeScreens = totalComposeScreens,
                totalXmlScreens = totalXmlScreens,
                totalMixedScreens = totalMixedScreens,
            ),
            screens = aggregated.values.toList()
        )

        outputFile.writeText(json.encodeToString(finalData))
    }
}
fun main() {
    val root = File("${System.getProperty("user.dir")}/../..")
    MigrationAggregator().aggregate(root)
}