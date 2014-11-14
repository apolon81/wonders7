$(function () {

    // websocket handle
    var ws;

    var uuid = function(){
        return localStorage.getItem("uuid");
    };

    var plrno = function(){
        return localStorage.getItem("my_player_no");
    };

    // handler function for POST requests that return the game's state
    var renderState = function(data, status) {
        $("#debug-state").html(JSON.stringify(data, undefined, 2));
    };

    // dummy handler for POSTs returning simple 200 OK with no body
    var dummyHandler = function(data, status) {};

    // handler for successful game join action - we need to grab our player no
    var joinHandler = function(data, status) {
        var content = JSON.parse(data);
        content.your_player_number
        localStorage.setItem("my_player_no", content.your_player_number);
    }

    // open the websocket connection for server pushes
    ws = new WebSocket("ws://apolons1-mobl1:8080/ws");

    // fetch the game state once on the page load
    $.post("http://apolons1-mobl1:8080/state", {id: uuid()}, renderState);

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
                $.post("http://apolons1-mobl1:8080/join", {nick: name, id: uuid()}, joinHandler);
                $(this).dialog("close");
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });

    // reset button handler
    $('#reset-game').click(function() {
        $.post("http://apolons1-mobl1:8080/reset", {id: uuid()}, dummyHandler);
    });

    // start game handler
    $('#start-game').click(function() {
        $.post("http://apolons1-mobl1:8080/start", {id: uuid()}, dummyHandler);
    });

    // pick a card handler
    $('#pick-card').click(function() {
        $('#card-selector').html("");
        var state = JSON.parse($('#debug-state').html());
        var cards = Object.keys(state.players[plrno()].hand);
        cards.forEach(function(card) {
            $('#card-selector').html($('#card-selector').html() + "<option value=\"" + card + "\">" + card + "</option>");
        });
        $('#card-picker').dialog({
            autoOpen: true,
            modal: true,
            buttons: {
                "Ok": function() {
                    console.log($("#card-picker option:selected").text());
                    //$.post("http://apolons1-mobl1:8080/pick",
                    //        {plrno: plrno(),
                    //         card: $("#card-picker option:selected").text()},
                    //         id: uuid(),
                    //         dummyHandler);
                    $(this).dialog("close");
                },
                "Cancel": function() {
                    $(this).dialog("close");
                }
            }
        });
    });

    // introduce yourself to the server
    ws.onopen = function() {
        ws.send(JSON.stringify({message: 'introduce', uuid: uuid()}));
    }

    // log any websocket opening errors
    ws.onerror = function(error) {
        console.log('ws.onerror: ' + error);
    }

    // when the server calls us up, its one of two reasons
    // 1. either we need to fetch the current game state
    // 2. or handle the client uuid registration
    ws.onmessage = function (msg) {
        var content = JSON.parse(msg.data);
        if (content.command == "refresh") $.post("http://apolons1-mobl1:8080/state", {id: uuid()}, renderState);
        if (content.command == "introduce") {
            localStorage.setItem("uuid", content.uuid);
        }
    };

});
