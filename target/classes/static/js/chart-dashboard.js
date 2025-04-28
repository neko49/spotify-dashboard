document.addEventListener("DOMContentLoaded", function () {
    const rows = document.querySelectorAll("#myTable tr");

    const titles = [];
    const popularity = [];
    const releaseYears = {};
    const durationsMs = [];
    const top5 = [];

    rows.forEach(row => {
        const cells = row.querySelectorAll("td");
        if (cells.length < 4) return;

        const title = cells[0].innerText;
        const pop = parseInt(cells[1].innerText);
        const duration = cells[2].innerText;
        const release = cells[3].innerText;

        // Popularité
        titles.push(title);
        popularity.push(pop);

        // Donut (top 5)
        top5.push({ title, pop });

        // Durée moyenne (convertir en ms)
        const [min, sec] = duration.replace("m", "").replace("s", "").split(" ").map(Number);
        const totalMs = min * 60 * 1000 + sec * 1000;
        durationsMs.push(totalMs);

        // Sortie
        const year = release.split("-")[0];
        releaseYears[year] = (releaseYears[year] || 0) + 1;
    });

    // 1. Popularité par titre
    new Chart(document.getElementById("popularityChart"), {
        type: "bar",
        data: {
            labels: titles,
            datasets: [{
                label: "Popularité",
                data: popularity,
                backgroundColor: "rgba(54, 162, 235, 0.6)"
            }]
        }
    });

    // 2. Nombre de titres par année de sortie
    new Chart(document.getElementById("releaseChart"), {
        type: "bar",
        data: {
            labels: Object.keys(releaseYears),
            datasets: [{
                label: "Sorties par année",
                data: Object.values(releaseYears),
                backgroundColor: "rgba(255, 99, 132, 0.6)"
            }]
        }
    });

    // 3. Durée moyenne
    const avgMs = durationsMs.reduce((a, b) => a + b, 0) / durationsMs.length;
    const avgMin = Math.floor(avgMs / 60000);
    const avgSec = Math.floor((avgMs % 60000) / 1000);
    document.getElementById("averageDuration").innerText = `${avgMin}m ${avgSec}s`;

    // 4. Donut top 5
    const top5Sorted = top5.sort((a, b) => b.pop - a.pop).slice(0, 5);
    new Chart(document.getElementById("topTracksChart"), {
        type: "doughnut",
        data: {
            labels: top5Sorted.map(t => t.title),
            datasets: [{
                label: "Top 5 Popularité",
                data: top5Sorted.map(t => t.pop),
                backgroundColor: [
                    "#ff6384", "#36a2eb", "#cc65fe", "#ffce56", "#4bc0c0"
                ]
            }]
        }
    });

});
