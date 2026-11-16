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

data class MigrationMeta(val author: String?, val date: String?)

class GitParser(private val root: File) {

    /**
     * Determine migration metadata by looking at the most recent commit
     * touching either the Compose or XML files for a screen.
     */
    fun findMigrationMetadata(xmlFiles: List<String>, composeFiles: List<String>): MigrationMeta {
        val composeMeta = composeFiles.firstOrNull()?.let { gitInfo(it) }
        val xmlMeta = xmlFiles.firstOrNull()?.let { gitInfo(it) }

        // Prefer Compose metadata if available (migration happened there)
        return MigrationMeta(
            author = composeMeta?.author ?: xmlMeta?.author,
            date = composeMeta?.date ?: xmlMeta?.date
        )
    }

    /**
     * Runs `git log` to extract the most recent commit author and date
     * for a given file path.
     */
    private fun gitInfo(path: String): MigrationMeta? {
        return try {
            val process = ProcessBuilder(
                "git",
                "log",
                "-1",
                "--pretty=format:%an|%ad",
                path
            )
                .directory(root)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText().trim()
            if (!output.contains("|")) {
                return null
            }

            val (author, date) = output.split("|", limit = 2)
            MigrationMeta(author, date)

        } catch (e: Exception) {
            // If git is unavailable (e.g., CI shallow clone), return null metadata
            null
        }
    }
}