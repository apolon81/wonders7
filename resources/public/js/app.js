// websocket handle
var ws;

(function () {

    // handler function for POST requests that return the game's state
    var renderState = function(data, status) {
        $("#debug-state").html(JSON.stringify(data, undefined, 2));
    };

    // open the websocket connection for server pushes
    ws = new WebSocket("ws://localhost:8080/ws");

    // fetch the game state once on the page load
    $.post("http://localhost:8080/state", {id: "dummy"}, renderState);

    // join button handler
    $('#join-game').click(function() {
        $('#join-form').dialog("open");
    });
    $('#join-form').dialog({
        autoOpen: false,
        modal: true,
        buttons: {
            "Ok": function() {
                var name = $("#player-name").val();
                $.post("http://localhost:8080/join", {nick: name, id: "dummy"}, renderState);
                $(this).dialog("close");
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });

    // reset button handler
    $('#reset-game').click(function() {
        $.post("http://localhost:8080/reset", {id: "dummy"}, renderState);
    });

    // start game handler
    $('#start-game').click(function() {
        $.post("http://localhost:8080/start", {id: "dummy"}, renderState);
    });

    // log any websocket opening errors
    ws.onerror = function(error) {
        console.log('Error detected: ' + error);
    }

    // when the server calls us up, we need to fetch the current game state
    ws.onmessage = function (msg) {
        $.post("http://localhost:8080/state", {id: "dummy"}, renderState);
    };

})();
