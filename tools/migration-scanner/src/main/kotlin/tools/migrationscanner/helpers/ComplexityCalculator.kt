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

/**
 * Produces a normalized complexity score (0.0–1.0) for each screen.
 *
 * The score is based on:
 *  - XML count
 *  - Compose count
 *  - Custom views
 *  - Navigation targets
 *  - File size / line count
 *
 * The goal is not perfect accuracy — just a stable, comparable metric
 * that helps you prioritize migration work.
 */
class ComplexityCalculator {

    fun calculate(
        xmlFiles: List<String>,
        composeFiles: List<String>,
        customViews: Int,
        navigationTargets: Int,
        totalLines: Int
    ): Double {

        // Raw weighted score
        val xmlWeight = xmlFiles.size * 0.25
        val composeWeight = composeFiles.size * 0.10
        val customViewWeight = customViews * 0.30
        val navWeight = navigationTargets * 0.15

        // Line count is capped to avoid runaway values
        val lineWeight = normalizeLines(totalLines) * 0.20

        val rawScore = xmlWeight + composeWeight + customViewWeight + navWeight + lineWeight

        // Normalize to 0.0–1.0
        return rawScore.coerceIn(0.0, 1.0)
    }

    /**
     * Convert line count into a 0–1 scale.
     *  - < 200 lines → low complexity
     *  - 200–800 lines → medium
     *  - > 800 lines → high
     */
    private fun normalizeLines(lines: Int): Double {
        return when {
            lines <= 200 -> 0.1
            lines <= 400 -> 0.3
            lines <= 800 -> 0.6
            lines <= 1200 -> 0.8
            else -> 1.0
        }
    }
}