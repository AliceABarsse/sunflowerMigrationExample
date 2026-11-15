package tools.migrationscanner

data class ScreenInfo(
    val screen: String,
    val status: String,
    val xmlFiles: List<String>,
    val composeFiles: List<String>,
    val customViews: String,

    val navigationTargets: String,
    val migratedBy: String?,
    val migrationDate: String?,
    val testsAdded: String?,
    val complexityScore: Double,
)
