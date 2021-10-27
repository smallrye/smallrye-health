/* 
 * Javascript file for health UI
 */
var refreshTimer;

function processSettings() {

    // Title
    var title = localStorage.getItem("title");
    if (title === null || title === '')
        title = "Health UI";    
    encodedtitle = htmlEncode(title);
    $("#navbar_title").html(encodedtitle);
    $("#settings_form_title").val(title);
    document.title = encodedtitle;

    // Poll
    var poll = localStorage.getItem("poll");
    $("#settings_form_poll").val(poll);
    if (poll === null || poll === '' || poll === 'off') {
        clearInterval(refreshTimer);
    } else {
        var interval = getInterval(poll);
        clearInterval(refreshTimer);
        if (interval > 0) {
            refreshTimer = setInterval(function () {
                loadHealthData();
            }, interval);
        }
    }

}

function loadHealthData() {
    var xmlhttp = new XMLHttpRequest();
    var url = getUrl();
    xmlhttp.onreadystatechange = function () {
        if (xmlhttp.readyState === 4) {
            if (xmlhttp.status === 200 || xmlhttp.status === 503) {
                var healthprobes = JSON.parse(this.responseText);
                processOk(healthprobes);
            } else {
                processError(xmlhttp);
            }
        }
    };
    xmlhttp.open("GET", url, true);
    xmlhttp.send();
}

function processOk(healthprobes) {
    var updown = healthprobes.status;
    
    if (updown === "DOWN") {
        $('#state').html("<h3><span class='badge badge-danger'><img src='refresh.png'/> Down</span></h3>");
    } else {
        $('#state').html("<h3><span class='badge badge-success'><img src='refresh.png'/> Up</span></h3>");
    }
    processData(healthprobes);
}

function processData(healthprobes) {

    var checks = healthprobes.checks;

    checks.sort(compare);
    
    var text = "";
    var i;
    for (i = 0; i < checks.length; i++) {
        var check = checks[i];
        var name = check.name;
        var updown = check.status;
        
        var headingclass = "text-success";
        var borderclass = "border-success";
        if (updown === "DOWN"){
            headingclass = "text-danger";
            borderclass = "border-danger";
        }
        text += "<div class='card " + borderclass + " shadow-sm'>";
        text += "<div class='card-body " + headingclass + "'>";
        text += "<h5 class='card-title'>" + name + "</h5>";
        text += "<table class='table'>"
                "<tbody>";

        var meta = check.data;

        for (var k in meta) {
            var v = meta[k];

            text += "<tr>" +
                    "<td>" + k + "</td>" +
                    "<td>" + v + "</td>" +
                    "</tr>";
        }

        text += "</tbody>" +
                "</table>" +
                "</div>" +
                "</div>";

    }


    $('#grid').html(text);
}

function compare(a, b) {
    // Use toUpperCase() to ignore character casing
    const checkA = a.name.toUpperCase();
    const checkB = b.name.toUpperCase();

    let comparison = 0;
    if (checkA > checkB) {
        comparison = 1;
    } else if (checkA < checkB) {
        comparison = -1;
    }
    return comparison;
}

function processError(xmlhttp) {
    $('#state').html("<h3><span class='badge badge-warning'><img src='refresh.png'/> Error fetching data</span></h3>");
    $('#grid').html("<blockquote class='blockquote text-center'>" +
            "<p class='mb-0'> Error while fetching data from [" + htmlEncode(getUrl()) + "]</p>" +
            "<p class='mb-0'>" + xmlhttp.responseText + "</p>" +
            "</blockquote>");

}

function getUrl() {
    var url = localStorage.getItem("url");
    if (url === null || url === '')
        url = "/health";
    return url;
}

function changeSettings() {
    // Title
    var title = $('#settings_form_title').val();
    localStorage.setItem("title", title);

    // URL (If the url changed we need to reload)
    var newurl = $('#settings_form_url').val();
    var oldurl = localStorage.getItem("url");
    if (newurl !== oldurl) {
        localStorage.setItem("url", newurl);
        loadHealthData();
    }

    // Poll
    var poll = $('#settings_form_poll').val();
    localStorage.setItem("poll", poll);

    $('#settingsModal').modal('hide');

    processSettings();
}

function getInterval(text) {

    if (text === "off") {
        return 0;
    } else if (text === "every 5 seconds") {
        return 5 * 1000;
    } else if (text === "every 10 seconds") {
        return 10 * 1000;
    } else if (text === "every 30 seconds") {
        return 30 * 1000;
    } else if (text === "every minute") {
        return 60 * 1000;
    } else if (text === "every 5 minutes") {
        return 60 * 5 * 1000;
    } else if (text === "every 10 minutes") {
        return 60 * 10 * 1000;
    } else {
        return 0;
    }

}

function htmlEncode(str){
  return String(str).replace(/[^\w. ]/gi, function(c){
     return '&#'+c.charCodeAt(0)+';';
  });
}

(function () {
    processSettings();
    loadHealthData();
})();