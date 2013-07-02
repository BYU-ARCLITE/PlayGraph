/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 6/4/13
 * Time: 12:07 PM
 * To change this template use File | Settings | File Templates.
 */
var PlayGraph = (function () {

    var host = "http://playgraph.byu.edu/";
    var key;
    var secret;

    var sessionId = 0;
    var status = "not started";
    var playbackData = {};

    function setAPICredentials(apiHost, publicKey, sharedSecret) {
        host = apiHost;
        key = publicKey;
        secret = sharedSecret;
    }

    function sign(method, url, parameters) {
        var accessor = {
            consumerSecret: secret,
            tokenSecret: ""
        };
        var message = {
            method: method,
            action: url,
            parameters: parameters
        };
        message.parameters.push(["oauth_version", "1.0"]);
        message.parameters.push(["oauth_consumer_key", key]);
//        message.parameters.push(["oauth_token", ""]);
        message.parameters.push(["oauth_timestamp", OAuth.timestamp()]);
        message.parameters.push(["oauth_nonce", OAuth.nonce(11)]);
        message.parameters.push(["oauth_signature_method", "HMAC-SHA1"]);

        // Sign it
        OAuth.SignatureMethod.sign(message, accessor);
        return OAuth.getAuthorizationHeader("http://playgraph.byu.edu/", message.parameters);
    }

    function oauthAjax(url, args) {
        args.type = args.type.toUpperCase();
        args.data = args.data || {};
        var parameters = Object.keys(args.data).map(function(key) {return [key, args.data[key]];});
        var auth = sign(args.type, url, parameters);
        $.ajax(url, {
            type: args.type,
            data: args.data,
            dataType: args.dataType || "json",
            success: args.success,
            headers: {
                "Authorization": auth
            },
            error: function (data) {
                alert("ERROR: " + JSON.stringify(data));
            }
        });
    }

    // Create the classes
    function Graph(data, callback) {
        var _this = this;
        this.id = 0;
        this.startNode = 0;

        function save(data, callback) {
            _this.startNode = data.startNode;
            if (_this.id === 0) {
                oauthAjax(host + "api/v1/author/graph", {
                    type: "post",
                    data: { startNode: data.startNode },
                    success: function(data) {
                        _this.id = data.graph.id;
                        callback(_this);
                    }
                });
            } else {
                oauthAjax(host + "api/v1/author/graph/" + _this.id, {
                    type: "post",
                    data: { startNode: data.startNode },
                    success: function() {
                        callback(_this);
                    }
                });
            }
        }

        function load(id, callback) {
            _this.id = id;
            oauthAjax(host + "api/v1/author/graph/" + _this.id, {
                type: "get",
                success: function(data) {
                    _this.startNode = data.startNode;
                    callback(_this);
                }
            });
        }

        if (data && callback) {
            if (typeof data === "object")
                save(data, callback);
            if (typeof data === "number")
                load(data, callback);
        }

        Object.defineProperties(this, {
            save: {
                value: function(data, callback) {
                    save(data, callback);
                }
            },
            load: {
                value: function(id, callback) {
                    load(id, callback);
                }
            }
        });
    }

    function Node(data, callback) {
        var _this = this;
        this.id = 0;
        this.contentType = "none";
        this.contentId = 0;
        this.content = "";
        this.transitions = [];
        this.settings = "";

        function saveContent(contentData, callback) {
            if (contentData.contentType === "data") {
                _this.content = contentData.content;

                // Are we creating new data?
                if (_this.contentType === "data" && _this.contentId !== 0) {
                    // No. Just update
                    oauthAjax(host + "api/v1/author/nodecontent/" + _this.contentId, {
                        type: "post",
                        data: {content: contentData.content},
                        success: function() {
                            callback(_this.contentId);
                        }
                    });
                } else {
                    // Yes
                    oauthAjax(host + "api/v1/author/nodecontent", {
                        type: "post",
                        data: {content: contentData.content},
                        success: function(data) {
                            _this.contentType = "data";
                            _this.contentId = data.nodeContent.id;
                            callback(_this.contentId);
                        }
                    });
                }
            } else if (contentData.contentType === "graph") {

                // Graph node. We never create a new graph or edit one. We just link.
                _this.contentType = "graph";
                _this.contentId = contentData.contentId;
                callback(_this.contentId);
            } else {

                // Are we creating a new node pool?
                if (_this.contentType === "nodePool" && _this.contentId !== 0) {
                    // No. Just update
                    oauthAjax(host + "api/v1/author/nodepool/" + _this.contentId, {
                        type: "post",
                        data: {
                            nodes: contentData.nodes.join(","),
                            script: contentData.script
                        },
                        success: function() {
                            callback(_this.contentId);
                        }
                    });
                } else {
                    // Yes
                    oauthAjax(host + "api/v1/author/nodepool", {
                        type: "post",
                        data: {
                            nodes: contentData.nodes.join(","),
                            script: contentData.script
                        },
                        success: function(data) {
                            _this.contentType = "nodePool";
                            _this.contentId = data.nodePool.id;
                            callback(_this.contentId);
                        }
                    });
                }
            }
        }

        function save(data, callback) {
            _this.transitions = data.transitions || _this.transitions;
            _this.settings = data.settings || _this.settings;
            saveContent(data, function (contentId) {
                if (_this.id === 0) {
                    oauthAjax(host + "api/v1/author/node", {
                        type: "post",
                        data: {
                            contentId: contentId,
                            contentType: data.contentType,
                            transitions: JSON.stringify(_this.transitions),
                            settings: _this.settings
                        },
                        success: function(data) {
                            _this.id = data.node.id;
                            callback(_this);
                        }
                    });
                } else {
                    oauthAjax(host + "api/v1/author/node/" + _this.id, {
                        type: "post",
                        data: {
                            contentId: contentId,
                            contentType: data.contentType,
                            transitions: JSON.stringify(_this.transitions),
                            settings: _this.settings
                        },
                        success: function() {
                            callback(_this);
                        }
                    });
                }
            });
        }

        function load(id, callback) {
            _this.id = id;
            oauthAjax(host + "api/v1/author/node/" + _this.id, {
                type: "get",
                success: function(data) {
                    _this.contentType = data.contentType;
                    _this.contentId = data.contentId;
                    _this.transitions = data.transitions.map(function (t) {
                        return new Transition({targetId: t.targetId, rule: t.rule});
                    });
                    _this.settings = data.settings;

                    if (_this.contentType === "data") {
                        oauthAjax(host + "api/v1/author/nodecontent/" + _this.contentId, {
                            type: "get",
                            success: function(data) {
                                _this.content = data.content;
                                callback(_this);
                            }
                        });
                    } else {
                        // TODO: Load other types
                        callback(_this);
                    }
                }
            });
        }

        if (data && callback) {
            if (typeof data === "object")
                save(data, callback);
            if (typeof data === "number")
                load(data, callback);
        }

        Object.defineProperties(this, {
            save: {
                value: function(data, callback) {
                    data = data || {};
                    data.contentId = data.contentId || this.contentId;
                    data.contentType = data.contentType || this.contentType;
                    data.content = data.content || this.content;
                    callback = callback || function(){};
                    save(data, callback);
                }
            },
            load: {
                value: function(id, callback) {
                    load(id, callback);
                }
            }
        });
    }

//    function NodePool(args) {
//        this.nodes = args.nodes || [];
//        this.script = args.script || "[];";
//    }

    function Transition(args) {
        this.targetId = + args.targetId || 0;
        this.rule = args.rule || "true;";
    }

    // Create the default classes
    var DataEditor = (function () {
        var template = '<textarea style="width: 500px; height: 300px;"></textarea>';

        function DataEditor(args) {
            this.$element = $(template);
            args.$holder.html(this.$element);

            this.$element[0].addEventListener("change", function (event) {
                event.stopPropagation();
                var newEvent = document.createEvent("HTMLEvents");
                newEvent.initEvent("update", true, true);
                this.dispatchEvent(newEvent);
            });
        }

        DataEditor.prototype.addEventListener = function(event, callback) {
            this.$element[0].addEventListener(event, callback);
        };

        DataEditor.prototype.getValue = function() {
            return this.$element.val();
        };

        DataEditor.prototype.setValue = function(value) {
            this.$element.val(value);
        };

        return DataEditor;
    }());

    var GraphRenderer = (function () {
        var template =
            '<div style="width: auto; height: 100%; margin-right: 6px;">' +
                '<canvas style="width: 100%; height: 100%;"></canvas>' +
            '</div>';
        var graph;
        var nodes;

        function GraphRenderer(args) {
            var _this = this;
            graph = args.graph;
            nodes = args.nodes;

            this.$element = $(template);
            args.$holder.html(this.$element);

            function render() {
                var $canvas = _this.$element.children("canvas");
                var canvas = $canvas[0];
                canvas.width = $canvas.width();
                canvas.height = $canvas.height();
                var context = canvas.getContext("2d");
                var nodesToRender = [
                    {
                        node: nodes[graph.startNode],
                        level: 1
                    }
                ];
                var nodesRendered = [];
                var sizeCache = {};
                var positionCache = {};

                // Fill with white
                context.fillStyle = "#ffffff";
                context.fillRect(0, 0, canvas.width, canvas.height);

                // Draw the graph name
                context.font = "16px Georgia";
                context.fillStyle = "#666666";
                context.fillText("Graph #" + graph.id, 10, 20);

                // Begin the graph rendering algorithm

                var height = 75;
                var radius = 20;

                function getSize(level) {
                    if (!sizeCache[level])
                        sizeCache[level] = nodesToRender
                            .map(function(data){return data.level;})
                            .filter(function(l){return l === level;})
                            .length + 1;
                    return sizeCache[level];
                }

                function getPosition(level) {
                    if (positionCache[level])
                        positionCache[level]++;
                    else
                        positionCache[level] = 1;
                    return positionCache[level];
                }

                function computeLocation(nodeData) {
                    var size = getSize(nodeData.level);
                    var position = getPosition(nodeData.level);
                    nodeData.node.y = nodeData.level * height;
                    nodeData.node.x = (canvas.width / (size + 1)) * position;
                    nodeData.node.processed = true;
                }

                function processNode(nodeData) {
                    computeLocation(nodeData);
                    nodesRendered.push(nodeData.node);

                    // Now process the transitions
                    nodeData.node.transitions.forEach(function (transition) {
                        var node = args.nodes[transition.targetId];
                        var toRender = nodesToRender.map(function(data){return data.node;});
                        if (toRender.indexOf(node) === -1 && nodesRendered.indexOf(node) === -1) {
                            nodesToRender.push({
                                node: node,
                                level: nodeData.level + 1
                            });
                        }
                    });
                }

                function drawTransitions() {
                    nodesRendered.forEach(function (node) {
                        node.transitions.forEach(function (transition) {
                            var targetNode = args.nodes[transition.targetId];
                            context.beginPath();
                            context.strokeStyle = "#0000ff";
                            context.moveTo(node.x, node.y);
                            context.lineTo(targetNode.x, targetNode.y);
                            context.stroke();
                        });
                    });
                }

                function drawCircles() {
                    nodesRendered.forEach(function (node) {
                        context.beginPath();
                        context.arc(node.x, node.y, radius, 0, 2 * Math.PI, false);
                        if (node.active)
                            context.fillStyle = "#ffff00";
                        else
                            context.fillStyle = "#ffffff";
                        context.fill();
                        context.strokeStyle = "#000000";
                        context.stroke();

                        // Draw the id
                        context.font = "14px Arial";
                        context.fillStyle = "#000000";
                        var name = "" + node.id;
                        var metrics = context.measureText(name);
                        context.fillText(name, node.x - (metrics.width / 2), node.y + 4);
                    });
                }

                (function () {
                    // Mark all nodes as unprocessed
                    Object.keys(nodes).forEach(function(id){nodes[id].processed = false;});

                    while (nodesToRender.length) {
                        nodesToRender = nodesToRender.sort(function(a, b) {
                            return a.level - b.level;
                        });
                        var current = nodesToRender.splice(0,1)[0];
                        processNode(current);
                    }

                    // Process unconnected nodes
                    var unprocessed = Object.keys(nodes)
                        .map(function(id){return nodes[id];})
                        .filter(function(node){return !node.processed;});
                    var level = Math.max.apply(null, Object.keys(sizeCache).map(function(n){return n;})) + 1;
                    unprocessed.forEach(function(node) {
                        processNode({
                            node: node,
                            level: level++
                        });
                    });

                    // Now do the actual rendering
                    drawTransitions();
                    drawCircles();

                    // Set up clicking
                    $canvas.unbind("click").click(function (event) {
                        var x = event.offsetX;
                        var y = event.offsetY;

                        // Check each node to see if we clicked in it
                        nodesRendered.forEach(function (node) {
                            var distance = Math.sqrt(Math.pow((x - node.x), 2) + Math.pow(y - node.y, 2));
                            if (distance <= radius) {
                                // Got it, now send an event
                                var event = document.createEvent("HTMLEvents");
                                event.initEvent("nodeSelect", true, true);
                                event.nodeId = node.id;
                                _this.$element[0].dispatchEvent(event);
                            }
                        });
                    });
                })();
            }

            render();

            Object.defineProperties(this, {
                update: {
                    value: function() {
                        render();
                    }
                }
            });
        }

        GraphRenderer.prototype.addEventListener = function(event, callback) {
            this.$element[0].addEventListener(event, callback);
        };

        return GraphRenderer;
    }());

    var NodeSelector = (function () {
        var template = '<select></select>';
        var optionTemplate = '<option value="{{id}}">{{name}}</option>';

        function NodeSelector(args) {
            var _this = this;

            // Create the element
            this.$element = $(template);
            Object.keys(args.nodes).forEach(function (id) {
                var node = args.nodes[id];
                var name = args.nameRenderer(node);
                _this.$element.append(optionTemplate.replace("{{id}}", id).replace("{{name}}", name));
            });
            this.$element.val(args.node.id);
            args.$holder.html(this.$element);

            // Add the update event
            this.$element.change(function(event) {
                event.stopPropagation();
                var newEvent = document.createEvent("HTMLEvents");
                newEvent.initEvent("update", true, true);
                newEvent.targetId = + $(this).val();
                _this.$element[0].dispatchEvent(newEvent);
            });
        }

        NodeSelector.prototype.addEventListener = function(event, callback) {
            this.$element[0].addEventListener(event, callback);
        };

        return NodeSelector;
    }());

    var TransitionRuleEditor = (function () {
        var template = '<textarea>{{rule}}</textarea>';

        function TransitionRuleEditor(args) {
            var _this = this;
            this.$element = $(template.replace("{{rule}}", args.rule));
            args.$holder.html(this.$element);

            this.$element[0].addEventListener("change", function (event) {
                event.stopPropagation();
                var newEvent = document.createEvent("HTMLEvents");
                newEvent.initEvent("update", true, true);
                newEvent.rule = _this.$element.val();
                this.dispatchEvent(newEvent);
            });
        }

        TransitionRuleEditor.prototype.addEventListener = function(event, callback) {
            this.$element[0].addEventListener(event, callback);
        };

        return TransitionRuleEditor;
    }());

    /*
     *   This part isn't actually provided outside of this script. It deals with the rendering of the transitions
     * ============================================================================================================
     *  ____  ____   __   __ _  ____  __  ____  __  __   __ _    ____  ____  __  ____  __  ____
     * (_  _)(  _ \ / _\ (  ( \/ ___)(  )(_  _)(  )/  \ (  ( \  (  __)(    \(  )(_  _)/  \(  _ \
     *   )(   )   //    \/    /\___ \ )(   )(   )((  O )/    /   ) _)  ) D ( )(   )( (  O ))   /
     *  (__) (__\_)\_/\_/\_)__)(____/(__) (__) (__)\__/ \_)__)  (____)(____/(__) (__) \__/(__\_)
     */

    var TransitionEditor = (function () {

        var template =
            '<div>' +
                '<table class="table table-bordered">' +
                    '<thead>' +
                        '<tr>' +
                            '<th>Target Node</th>' +
                            '<th>Rule</th>' +
                            '<th>Delete</th>' +
                        '</tr>' +
                    '</thead>' +
                    '<tbody></tbody>' +
                '</table>' +
                '<button class="btn"><i class="icon-plus-sign"></i> Add Transition</button> ' +
            '</div>';

        var rowTemplate =
            '<tr>' +
                '<td></td>' +
                '<td></td>' +
                '<td><button class="btn btn-danger"><i class="icon-trash"></i></button></td>' +
            '</tr>';

        function TransitionEditor(args) {
            var _this = this;
            var transitions = [];

            this.$element = $(template);
            args.$holder.html(this.$element);

            // Set up the add button
            this.$element.children("button").click(function () {
                // Make the new transition to any node
                var transition = new Transition({
                    targetId: Object.keys(args.nodes)[0]
                });
                transitions.push(transition);

                var newEvent = document.createEvent("HTMLEvents");
                newEvent.initEvent("update", true, true);
                _this.$element[0].dispatchEvent(newEvent);

                render();
            });

            function render() {
                var $tbody = _this.$element.find("tbody").html("");

                transitions.forEach(function (transition) {
                    var node = args.nodes[transition.targetId];
                    var $row = $(rowTemplate);

                    // Create the node selector
                    var nodeSelector = new args.NodeSelector({
                        node: node,
                        nodes: args.nodes,
                        nameRenderer: args.nameRenderer,
                        $holder: $row.find("td:first-child")
                    });
                    nodeSelector.addEventListener("update", function(event) {
                        event.stopPropagation();
                        transition.targetId = event.targetId;
                        var newEvent = document.createEvent("HTMLEvents");
                        newEvent.initEvent("update", true, true);
                        _this.$element[0].dispatchEvent(newEvent);
                    });

                    // Create the rule editor
                    var ruleEditor = new args.TransitionRuleEditor({
                        rule: transition.rule,
                        $holder: $row.find("td:nth-child(2)")
                    });
                    ruleEditor.addEventListener("update", function(event) {
                        event.stopPropagation();
                        transition.rule = event.rule;
                        var newEvent = document.createEvent("HTMLEvents");
                        newEvent.initEvent("update", true, true);
                        _this.$element[0].dispatchEvent(newEvent);
                    });

                    // Set up the delete button
                    $row.find("button").click(function() {
                        transitions.splice(transitions.indexOf(transition), 1);

                        var newEvent = document.createEvent("HTMLEvents");
                        newEvent.initEvent("update", true, true);
                        _this.$element[0].dispatchEvent(newEvent);

                        render();
                    });

                    $tbody.append($row);
                });
            }

            Object.defineProperties(this, {
                transitions: {
                    get: function() {
                        return transitions;
                    },
                    set: function(value) {
                        transitions = value;
                        render();
                    }
                },
                update: {
                    value: function() {
                        render();
                    }
                }
            })
        }

        TransitionEditor.prototype.addEventListener = function(event, callback) {
            this.$element[0].addEventListener(event, callback);
        };

        return TransitionEditor;
    }());



    var SettingsEditor = (function() {
        function SettingsEditor(args) {
            args.$holder.html("There are no settings available.");
        }
        SettingsEditor.prototype.setNode = function(node) {};
        SettingsEditor.prototype.addEventListener = function(event, callback) {};

        return SettingsEditor;
    })();

    /*
     *   This is the main part which connects everything together
     * ============================================================
     *    __   _  _  ____  _  _   __  ____    ____  __    __   __
     *   / _\ / )( \(_  _)/ )( \ /  \(  _ \  (_  _)/  \  /  \ (  )
     *  /    \) \/ (  )(  ) __ ((  O ))   /    )( (  O )(  O )/ (_/\
     *  \_/\_/\____/ (__) \_)(_/ \__/(__\_)   (__) \__/  \__/ \____/
     */

    var AuthorTool = (function () {

        var graph;
        var nodes = {};
        var currentNode;

        /**
         * This either creates a new graph or loads the provided graph.
         * @param graphId
         * @param callback
         */
        function loadGraph(graphId, callback) {
            if (!graphId) {
                new Node({contentType:"data",content:""}, function (node) {
                    nodes[node.id] = node;
                    new Graph({startNode:node.id}, function (_graph) {
                        graph = _graph;
                        callback();
                    });
                });
            } else {
                // Load a graph
                var loadCount = 0;
                var loaded = 0;
                var calledBack = false;

                function loadNode(nodeId, callback) {
                    if (!nodes[nodeId]) {
                        loadCount++;
                        new Node(+nodeId, function (node) {
                            nodes[node.id] = node;
                            node.transitions.forEach(function(t) {
                                loadNode(t.targetId, callback);
                            });
                            loaded++;
                            if (loaded === loadCount && !calledBack) {
                                calledBack = true;
                                callback();
                            }
                        });
                    }
                }

                new Graph(+graphId, function (_graph) {
                    graph = _graph;
                    loadNode(graph.startNode, function() {
                        callback();
                    });
                });
            }
        }

        function AuthorTool(args) {
            var _this = this;

            key = args.publicKey;
            secret = args.sharedSecret;
            host = args.host || host;

            function selectNode(node) {
                if (currentNode)
                    currentNode.active = false;
                currentNode = node;
                currentNode.active = true;
                _this.dataEditor.setValue(node.content);
                _this.transitionEditor.transitions = currentNode.transitions;
                _this.graphRenderer.update();
                _this.settingsEditor.setNode(node);
            }

            loadGraph(args.graphId, function() {

                // Run the external initializer
                args.initializer(graph, nodes, function() {
                    // Create the components
                    // Create the data editor
                    _this.dataEditor = new args.DataEditor({
                        $holder: args.$data
                    });

                    _this.dataEditor.addEventListener("update", function() {
                        currentNode.content = _this.dataEditor.getValue();
                        currentNode.save();
                        _this.transitionEditor.update();
                    });

                    // Create the graph renderer
                    _this.graphRenderer = new args.GraphRenderer({
                        $holder: args.$graphHolder,
                        graph: graph,
                        nodes: nodes
                    });

                    _this.graphRenderer.addEventListener("nodeSelect", function(event) {
                        selectNode(nodes[event.nodeId])
                    });

                    // Create the transition editor
                    _this.transitionEditor = new TransitionEditor({
                        $holder: args.$transitions,
                        TransitionRuleEditor: args.TransitionRuleEditor,
                        nameRenderer: args.nameRenderer,
                        NodeSelector: args.NodeSelector,
                        nodes: nodes
                    });
                    _this.transitionEditor.addEventListener("update", function() {
                        currentNode.transitions = _this.transitionEditor.transitions;
                        currentNode.save();
                        _this.graphRenderer.update();
                    });

                    // Create the settings editor
                    _this.settingsEditor = new args.SettingsEditor({
                        $holder: args.$settings
                    });
                    _this.settingsEditor.addEventListener("update", function() {
                        currentNode.settings = _this.settingsEditor.settings;
                        currentNode.save();
                    });

                    selectNode(nodes[graph.startNode]);
                });
            });

            Object.defineProperties(this, {
                delete: {
                    value: function() {
                        if (currentNode.id === graph.startNode) {
                            alert("You cannot delete the root node.");
                            return;
                        }

                        Object.keys(nodes).forEach(function (id) {
                            var toRemove = nodes[id].transitions.filter(function(t){return +t.targetId === currentNode.id;});
                            toRemove.forEach(function(t) {
                                var trans = nodes[id].transitions;
                                trans.splice(trans.indexOf(t), 1);
                            });
                        });

                        delete nodes[currentNode.id];
                        // TODO: Delete via API
                        currentNode = nodes[Object.keys(nodes)[0]];
                        selectNode(currentNode);
                    }
                },
                graphId: {
                    get: function() {
                        return graph.id;
                    }
                }
            })
        }

        AuthorTool.prototype.newNode = function() {
            var _this = this;
            new Node({contentType: "data",content:""}, function (node) {
                nodes[node.id] = node;
                _this.transitionEditor.update();
                _this.graphRenderer.update();
            });
        };

        return AuthorTool;
    }());

    function startPlayback(graphId, callback) {
        oauthAjax(host + "api/v1/player/start", {
            type: "post",
            data: { graph: + graphId },
            success: function(data) {
                sessionId = data.sessionId;
                status = "continue";
                callback();
            }
        });
    }

    function updatePlayback(data, callback) {
        if (status === "continue") {
            data = data || playbackData;
            data.sessionId = sessionId;
            oauthAjax(host + "api/v1/player/update", {
                type: "post",
                data: data,
                success: function(data) {
                    playbackData = {};
                    status = data.status;
                    callback(status);
                }
            });
        } else {
            callback(status);
        }
    }

    function getPlaybackContent(callback) {
        if (status === "continue") {
            oauthAjax(host + "api/v1/player/content/" + sessionId, {
                type: "get",
                dataType: "text",
                success: function(data) {
                    callback(data);
                }
            });
        } else {
            callback("This PlayGraph is finished.");
        }
    }

    function getPlaybackSettings(callback) {
        if (status === "continue") {
            oauthAjax(host + "api/v1/player/settings/" + sessionId, {
                type: "get",
                dataType: "text",
                success: function(data) {
                    callback(data);
                }
            });
        } else {
            callback("This PlayGraph is finished.");
        }
    }

    return {
        classes: {
            Graph: Graph,
            Node: Node,
//            NodePool: NodePool,
            Transition: Transition
        },
        defaultObjects: {
            DataEditor: DataEditor,
            GraphRenderer: GraphRenderer,
            nameRenderer: function(node) {
                return "Node #" + node.id;
            },
            NodeSelector: NodeSelector,
            SettingsEditor: SettingsEditor,
            TransitionRuleEditor: TransitionRuleEditor
        },
        registeredObjects: {},
        AuthorTool: AuthorTool,
        player: {
            start: startPlayback,
            update: updatePlayback,
            content: getPlaybackContent,
            settings: getPlaybackSettings,
            data: playbackData
        },
        oauthAjax: oauthAjax,
        setAPICredentials: setAPICredentials
    };
}());