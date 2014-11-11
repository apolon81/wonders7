var conn;                      // global

(function () {

    conn = new WebSocket("ws://localhost:8080/ws");

    $('#join-form').dialog({
        autoOpen: false,
        modal: true,
        buttons: {
            "Ok": function() {
                var name = $("#player-name").val();
                //conn.send(JSON.stringify({command: 'join', name: name.val()}));
                $.get("http://localhost:8080/join/" + name,
                function(data,status){
                    $("#debug-state").html(JSON.stringify(JSON.parse(data), undefined, 2));
                });
                $(this).dialog("close");
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });


    $('#join-game').click(function() {
        $('#join-form').dialog("open");
    });

    conn.onopen = function (e) {
        alert("connected!");
    };

    conn.onerror = function () {
        alert("error");
        console.log(arguments);
    };

    conn.onmessage = function (e) {
        alert(e.data);
      console.log(e.data);
    };

})();
