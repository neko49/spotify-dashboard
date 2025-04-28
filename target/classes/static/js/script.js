$(document).ready(function () {
    // Filtrage local du tableau
    $("#tableSearch").on("keyup", function () {
        var value = $(this).val().toLowerCase();
        $("#myTable tr").filter(function () {
            $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1);
        });
    });

    // Autocomplétion
    const $input = $("#artistName");
    const $suggestions = $("#suggestions");

    $input.on("input", function () {
        const query = $(this).val();
        if (query.length < 2) {
            $suggestions.empty();
            return;
        }

        $.get("/autocomplete", { query: query }, function (data) {
            $suggestions.hide().empty();

            if (data.length === 0) return;

            data.forEach(name => {
                $suggestions.append(`<li class="list-group-item list-group-item-action">${name}</li>`);
            });

            $(".list-group-item").on("click", function () {
                $input.val($(this).text());
                $suggestions.fadeOut(150);
            });
            $suggestions.fadeIn(200);
        });
    });

    // Clic en dehors : cacher la suggestion
    $(document).on("click", function (e) {
        if (!$(e.target).closest("#artistName").length) {
            $suggestions.fadeOut(150);
        }
    });

    $(window).on("load", function () {
        $("#loader").fadeOut(300, function () {
            $("#main-content").fadeIn(300);
        });
    });

});
