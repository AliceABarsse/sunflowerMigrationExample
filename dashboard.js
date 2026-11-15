async function loadData() {
    const response = await fetch("build/scan-results/screens_aggregated.json");
    return await response.json();
}

function complexityClass(score) {
    if (score < 0.33) return "complexity-low";
    if (score < 0.66) return "complexity-medium";
    return "complexity-high";
}
loadData().then(data => {
    document.getElementById("generatedAt").innerText =
        "Generated at: " + data.generated_at;

    // Summary
    document.getElementById("totalScreens").innerText = data.summary.total_screens;
    document.getElementById("composeScreens").innerText = data.summary.compose_screens;
    document.getElementById("xmlScreens").innerText = data.summary.xml_screens;
    document.getElementById("mixedScreens").innerText = data.summary.mixed_screens;

    // Progress chart
    const ctx = document.getElementById("progressChart");
    if (typeof Chart !== "undefined" && ctx) {
        new Chart(ctx, {
            type: "doughnut",
            data: {
                labels: ["Compose", "XML", "Mixed"],
                datasets: [{
                    data: [
                        data.summary.compose_screens,
                        data.summary.xml_screens,
                        data.summary.mixed_screens
                    ],
                    backgroundColor: ["#4caf50", "#f44336", "#ff9800"],
                    radius: 50
                }]
            }
        });
    }

        // Screens table
            const tbody = document.querySelector("#screensTable tbody");
            data.screens.forEach(screen => {
                const tr = document.createElement("tr");

                tr.innerHTML = `
                    <td>${screen.screen}</td>
                    <td>${screen.status}</td>
                    <td>${screen.xmlFiles.length}</td>
                    <td>${screen.composeFiles.length}</td>
                    <td>${screen.customViews}</td>
                    <td>${screen.navigationTargets}</td>
                    <td class="${complexityClass(screen.complexityScore)}">
                        ${screen.complexityScore.toFixed(2)}
                    </td>
                    <td>${screen.migratedBy || "-"}</td>
                    <td>${screen.migrationDate || "-"}</td>
                    <td>${screen.testsAdded}</td>
                `;

                tbody.appendChild(tr);
            });

            // Developer leaderboard
                const leaderboard = {};
                data.screens.forEach(s => {
                    if (!s.migratedBy) return;
                    leaderboard[s.migratedBy] = (leaderboard[s.migratedBy] || 0) + 1;
                });

                const leaderboardList = document.getElementById("leaderboard");
                Object.entries(leaderboard)
                    .sort((a, b) => b[1] - a[1])
                    .forEach(([dev, count]) => {
                        const li = document.createElement("li");
                        li.innerText = `${dev}: ${count} screens migrated`;
                        leaderboardList.appendChild(li);
                    });
            }).catch(error => {
                console.error("Error loading or rendering dashboard data:", error);
            });
