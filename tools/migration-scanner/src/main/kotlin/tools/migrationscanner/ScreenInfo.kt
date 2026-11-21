package tools.migrationscanner

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ScreenInfo(
    val screen: String,
    val status: String,
    val xmlFiles: List<String>,
    val composeFiles: List<String>,
    val customViews: Int,

    val navigationTargets: Int,
    val migratedBy: String?,
    val migrationDate: String?,
    val testsAdded: Int,
    val complexityScore: Double,
)
