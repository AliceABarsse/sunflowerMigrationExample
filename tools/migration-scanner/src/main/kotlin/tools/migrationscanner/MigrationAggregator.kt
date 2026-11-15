package tools.migrationscanner
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class RawScreen(
    val screen: String,
    val status: String,
    val xmlFiles: List<String> = emptyList(),
    val composeFiles: List<String> = emptyList()
)

@Serializable
data class ScreenSummary(
    val totalScreens:Int,
    val xmlScreens: Int,
    val composeScreens: Int,
    val migrationStatus: String
)


@Serializable
data class AggregatedScreen(
    val screen: String,
    val status: String,
    val xmlFiles: List<String>,
    val jetpackFiles: List<String>,
    val summary: ScreenSummary
)

/*
class MigrationAggregator {

    private val gson = Gson()

    fun aggregate(root: File) {
        val scanDir = File(root, "build/scan-results")
        if (!scanDir.exists()) {
            println("No scan-results directory found at: ${scanDir.absolutePath}")
            return
        }
        val jsonFiles = scanDir.listFiles { f -> f.extension == "json" }?.toList() ?: emptyList()

        println("Found ${jsonFiles.size} scan result files to aggregate")

        val allScreens = mutableListOf<ScreenInfo>()
        val listType = object : TypeToken<List<ScreenInfo>>() {}.type

        jsonFiles.forEach { file ->
            try {
                val content = file.readText()
                val screens: List<ScreenInfo> = gson.fromJson(content, listType)
                allScreens.addAll(screens)
            } catch (e: Exception) {
                println("Failed to parse ${file.name}: ${e.message}")
            }
        }

        val outputFile = File(scanDir, "screens_aggregated.json")
        outputFile.writeText(gson.toJson(allScreens))

        println("Aggregation complete. Output written to: ${outputFile.absolutePath}")
    }

}
*/

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

        val rawScreens = json.decodeFromString<List<RawScreen>>(inputFile!!.readText())

        val aggregated = rawScreens.associate { raw ->
            val xmlCount = raw.xmlFiles.size
            val jetpackCount = raw.composeFiles.size

            val migrationStatus =
                when {
                    xmlCount > 0 && jetpackCount > 0 -> "partial"
                    xmlCount > 0 -> "xml-only"
                    jetpackCount > 0 -> "jetpack-only"
                    else -> "unknown"
                }

            val summary = ScreenSummary(
                totalScreens = xmlCount + jetpackCount,
                xmlScreens = xmlCount,
                composeScreens = jetpackCount,
                migrationStatus = migrationStatus
            )

            raw.screen to AggregatedScreen(
                screen = raw.screen,
                status = raw.status,
                xmlFiles = raw.xmlFiles,
                jetpackFiles = raw.composeFiles,
                summary = summary
            )
        }

        outputFile.writeText(json.encodeToString(aggregated))
    }
}
fun main() {
    val root = File("${System.getProperty("user.dir")}/../..")
    MigrationAggregator().aggregate(root)
}