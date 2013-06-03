

// Add the scripts
$(function() {

});

var playGraph = {
    host: "http://localhost:9000/",
    consumerKey: "",
    consumerSecret: "",

    /*init: function(callback) {
        var base = playGraph.host + "assets/javascripts/";
        var scripts = ["sha1.js", "oauth.js", "jquery.oauth.js"];

        var addScript = function addScript(index) {
            if(index < scripts.length)
                $.getScript(base + scripts[index], function() {
                    addScript(index + 1);
                });
            else {
                playGraph.inited = true;
                callback();
            }
        };

        addScript(0);
    },                     */

    player: {
        contentInterpreter: null,
        currentSessionId: 0,
        finishCallback: function() {
            alert("Done");
        },

        createContentInterpreter: function() {
            $("body").append("<div id='playGraph_container'></div>");
            playGraph.player.contentInterpreter = function(data) {
                $("#playGraph_container").html(data);
            }
        },

        render: function() {
            $.oauth({
                url: playGraph.host + "api/v1/player/content/" + playGraph.player.currentSessionId,
                type: "get",
                consumerKey: playGraph.consumerKey,
                consumerSecret: playGraph.consumerSecret,
                dataType: "text",
                cache: true,
                success: function(data) {
                    playGraph.player.contentInterpreter(data);
                },
                error: function(data) {
                    console.log("Error getting content: " + JSON.stringify(data));
                }
            });
        },

        start: function(graphId, contentInterpreter, finishCallback) {
            // Setup the stuffs
            if(contentInterpreter)
                playGraph.player.contentInterpreter = contentInterpreter;
            else
                playGraph.player.createContentInterpreter();
            if(finishCallback)
                playGraph.player.finishCallback = finishCallback;

            // Start the graph a-playing
            $.oauth({
                url: playGraph.host + "api/v1/player/start",
                type: "post",
                consumerKey: playGraph.consumerKey,
                consumerSecret: playGraph.consumerSecret,
                data: {
                    graph: graphId
                },
                dataType: "json",
                success: function(data) {
                    playGraph.player.currentSessionId = data.sessionId;
                    playGraph.player.render();
                },
                error: function(data) {
                    console.log("Error starting session: " + JSON.stringify(data));
                }
            });
        },

        update: function(data) {
            // Update the graph
            data = data || {};
            data.sessionId = playGraph.player.currentSessionId;
            $.oauth({
                url: playGraph.host + "api/v1/player/update",
                type: "post",
                consumerKey: playGraph.consumerKey,
                consumerSecret: playGraph.consumerSecret,
                data: data,
                dataType: "json",
                success: function(data) {
                    if(data.status === "continue")
                        playGraph.player.render();
                    else
                        playGraph.player.finishCallback();
                },
                error: function(data) {
                    console.log("Error starting session: " + JSON.stringify(data));
                }
            });
        }
    },

    setAuth: function(key, secret) {
        playGraph.consumerKey = key;
        playGraph.consumerSecret = secret;
    }
};