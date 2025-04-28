// src/main/resources/static/js/theme-toggle.js
document.addEventListener("DOMContentLoaded", function () {
    const toggleBtn = document.getElementById("toggle-theme");

    // Appliquer le thème sauvegardé dès le chargement
    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark-mode");
        if (toggleBtn) toggleBtn.textContent = "☀️ Mode Clair";
    }

    // Gestion du clic sur le bouton de bascule de thème
    if (toggleBtn) {
        toggleBtn.addEventListener("click", function () {
            document.body.classList.toggle("dark-mode");
            const isDark = document.body.classList.contains("dark-mode");
            this.textContent = isDark ? "☀️ Mode Clair" : "🌓 Mode Sombre";
            localStorage.setItem("theme", isDark ? "dark" : "light");
        });
    }
});
