// websocket handle
var ws, uuid;

(function () {

    // handler function for POST requests that return the game's state
    var renderState = function(data, status) {
        $("#debug-state").html(JSON.stringify(data, undefined, 2));
    };

    // open the websocket connection for server pushes
    ws = new WebSocket("ws://apolons1-mobl1:8080/ws");

    uuid = localStorage.getItem("uuid");

    // fetch the game state once on the page load
    $.post("http://apolons1-mobl1:8080/state", {id: uuid}, renderState);

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
                $.post("http://apolons1-mobl1:8080/join", {nick: name, id: uuid}, renderState);
                $(this).dialog("close");
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });

    // reset button handler
    $('#reset-game').click(function() {
        $.post("http://apolons1-mobl1:8080/reset", {id: uuid}, renderState);
    });

    // start game handler
    $('#start-game').click(function() {
        $.post("http://apolons1-mobl1:8080/start", {id: uuid}, renderState);
    });

    // introduce yourself to the server
    ws.onopen = function() {
        ws.send(JSON.stringify({message: 'introduce', uuid: uuid}));
    }

    // log any websocket opening errors
    ws.onerror = function(error) {
        console.log('Error detected: ' + error);
    }

    // when the server calls us up, we need to fetch the current game state
    ws.onmessage = function (msg) {
        console.log(msg);
        var content = JSON.parse(msg.data);
        console.log(content);
        if (content.command == "refresh") $.post("http://apolons1-mobl1:8080/state", {id: uuid}, renderState);
        if (content.command == "introduce") {
            localStorage.setItem("uuid", content.uuid);
            uuid = localStorage.getItem("uuid");
        }
    };

})();
