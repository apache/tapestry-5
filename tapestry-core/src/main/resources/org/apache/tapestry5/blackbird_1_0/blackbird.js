/*
 Blackbird - Open Source JavaScript Logging Utility
 Author: G Scott Olson
 Web: http://blackbirdjs.googlecode.com/
 http://www.gscottolson.com/blackbirdjs/
 Version: 1.0

 The MIT License - Copyright (c) 2008 Blackbird Project

 Heavily modified for Tapestry to rename namespace, make use of Prototype: March 2009
 */
( function()
{
    var IE6_POSITION_FIXED = true; // enable IE6 {position:fixed}

    var bbird, checkbox, filters, controls, size;
    var outputList;
    var cache = [];

    var state = getState();
    var classes = {};
    var profiler = {};

    var messageTypes = { //order of these properties imply render order of filter controls
        debug: true,  // May be set to false based on Tapestry.DEBUG_ENABLED
        info: true,
        warn: true,
        error: true,
        profile: true
    };

    function constructUI()
    {
        bbird = new Element("div", { 'id': 't-console', 'title': 'F2 toggles / Shift-F2 moves' }).hide();
        var header = new Element("div", { 'class': 't-header' });
        var left = new Element("div", { 'class' : 't-left' });

        left.insert(filters = new Element("div", { 'class': 't-filters' }));

        for (var type in messageTypes)
        {
            var className = messageTypes[type] ? type : type + "Disabled";

            filters.insert(new Element("span", {'class': className, 'title': "filter by " + type, 'type': type }));
        }

        var right = new Element("div", { 'class': 't-right'});

        right.insert(controls = new Element("div", { 'class': 't-controls'}));

        controls.insert(size = new Element("span", { 'title': 'contract', 'op': 'resize' }));

        controls.insert(new Element("span", { 'class': 't-clear', 'title': 'clear', 'op': 'clear' }));
        controls.insert(new Element("span", { 'class': 't-close', 'title': 'close', 'op': 'close' }));

        header.insert(left);
        header.insert(right);

        bbird.insert(header);

        var main = new Element("div", { 'class': 't-main' });
        main.insert(new Element("div", {'class': 't-left'}));

        var mainBody = new Element("div", { 'class': 't-mainBody' });

        mainBody.insert(outputList = new Element("ol"));

        $A(cache).each(function(element)
        {
            outputList.insert(element);
        });

        cache = undefined;

        main.insert(mainBody);
        main.insert(new Element("div", { 'class': 't-right' }));

        bbird.insert(main);

        var footer = new Element("div", { 'class': 't-footer'});

        footer.insert(left = new Element("div", { 'class': 't-left' }));

        left.insert();

        var label = new Element("label")
        label.insert(checkbox = new Element("input", { 'type': 'checkbox' }));
        label.insert("Visible on page load");

        left.insert(label);

        footer.insert(new Element("div", { 'class': 't-right' }));

        bbird.insert(footer);

        $(document.body).insert(bbird);
    }

    function backgroundImage()
    { //(IE6 only) change <BODY> tag's background to resolve {position:fixed} support
        var bodyTag = $(document.body);

        if (bodyTag.currentStyle && IE6_POSITION_FIXED)
        {
            if (bodyTag.currentStyle.backgroundImage == 'none')
            {
                bodyTag.addClassName('t-fix-ie6-background');
            }
            if (bodyTag.currentStyle.backgroundAttachment == 'scroll')
            {
                bodyTag.style.backgroundAttachment = 'fixed';
            }
        }
    }

    function addMessage(type, content)
    { //adds a message to the output list

        content = ( content.constructor == Array ) ? content.join('') : content;

        var newMsg = new Element("li", { 'class': type});

        newMsg.insert(new Element("span", { 'class': 'icon'}));
        newMsg.insert(content);

        if (outputList)
        {
            outputList.insert(newMsg);

            // If the added message is not being filtered out, then
            // make sure it is visible to the user.
            if (messageTypes[type] && !isVisible())
            {
                scrollToBottom();
                show();
            }
        }
        else
        {
            cache.push(newMsg);
        }

    }

    function clear()
    { //clear list output
        outputList.update();
    }

    function clickControl(evt)
    {
        var el = evt.element();

        if (el.tagName == 'SPAN')
        {
            switch (el.getAttributeNode('op').nodeValue)
                    {
                case 'resize': resize(); break;
                case 'clear':  clear();  break;
                case 'close':  hide();   break;
            }
        }
    }

    function clickFilter(evt)
    { //show/hide a specific message type
        var span = evt.element();

        if (span && span.tagName == 'SPAN')
        {
            var type = span.readAttribute('type');

            if (evt.altKey)
            {
                var active = 0;
                for (var entry in messageTypes)
                {
                    if (messageTypes[ entry ]) active++;
                }

                var oneActiveFilter = ( active == 1 && messageTypes[ type ] );

                filters.childElements().each(function (child)
                {
                    var childType = child.readAttribute('type');

                    var enabled = oneActiveFilter || (childType == type);

                    messageTypes[ childType ] = enabled;

                    child.className = enabled ? childType : childType + 'Disabled';
                });
            }
            else
            {
                messageTypes[ type ] = ! messageTypes[ type ];
                span.className = ( messageTypes[ type ] ) ? type : type + 'Disabled';
            }

            rebuildOutputListClassName();

            scrollToBottom();
        }
    }

    function rebuildOutputListClassName()
    {
        //build outputList's class from messageTypes object
        var disabledTypes = [];
        for (type in messageTypes)
        {
            if (! messageTypes[ type ]) disabledTypes.push(type);
        }
        disabledTypes.push('');
        outputList.className = disabledTypes.join('Hidden ');
    }

    function clickVis(evt)
    {
        var el = evt.element();

        state.load = el.checked;
        saveState();
    }


    function scrollToBottom()
    { //scroll list output to the bottom
        outputList.scrollTop = outputList.scrollHeight;
    }

    function isVisible()
    {
        return bbird.visible();
    }

    function hide()
    {
        bbird.style.display = 'none';
    }

    function show()
    {
        var body = $(document.body);

        body.removeChild(bbird);
        body.appendChild(bbird);

        bbird.style.display = 'block';
    }

    //sets the position
    function reposition(position)
    {
        if (position === undefined || position == null)
        {
            position = ( state && state.pos === null ) ? 1 : ( state.pos + 1 ) % 4; //set to initial position ('topRight') or move to next position
        }

        switch (position)
                {
            case 0: classes[ 0 ] = 'bbTopLeft'; break;
            case 1: classes[ 0 ] = 'bbTopRight'; break;
            case 2: classes[ 0 ] = 'bbBottomLeft'; break;
            case 3: classes[ 0 ] = 'bbBottomRight'; break;
        }
        state.pos = position;
        saveState();
    }

    function resize(big)
    {
        if (big === undefined || big === null)
        {
            big = ( state && state.size == null ) ? 0 : ( state.size + 1 ) % 2;
        }

        classes[ 1 ] = ( big === 0 ) ? 'bbSmall' : 'bbLarge'

        size.title = ( big === 1 ) ? 'small' : 'large';
        size.className = "t-" + size.title;

        state.size = big;

        saveState();
        scrollToBottom();
    }

    function saveState()
    {
        var expiration = new Date();
        expiration.setDate(expiration.getDate() + 14);
        document.cookie =
        [ 'blackbird=', Object.toJSON(state), '; expires=', expiration.toUTCString() ,';' ].join('');

        var newClass = [];
        for (word in classes)
        {
            newClass.push(classes[ word ]);
        }
        bbird.className = newClass.join(' ');
    }

    function getState()
    {
        var re = new RegExp(/blackbird=({[^;]+})(;|\b|$)/);
        var match = re.exec(document.cookie);
        return ( match && match[ 1 ] ) ? eval('(' + match[ 1 ] + ')') : { pos:null, size:null, load:null };
    }

    //event handler for 'keyup' event for window
    function readKey(evt)
    {
        var code = 113; //F2 key

        if (evt && evt.keyCode == code)
        {

            var visible = isVisible();

            if (visible && evt.shiftKey && evt.altKey) clear();
            else if (visible && evt.shiftKey) reposition();
            else if (!evt.shiftKey && !evt.altKey)
                {
                    ( visible ) ? hide() : show();
                }
        }
    }


    Tapestry.Logging = {
        toggle:
                function()
                {
                    ( isVisible() ) ? hide() : show();
                },
        hide:
                function()
                {
                    hide();
                },
        resize:
                function()
                {
                    resize();
                },
        clear:
                function()
                {
                    clear();
                },
        move:
                function()
                {
                    reposition();
                },
        debug:
                function(msg)
                {
                    addMessage('debug', msg);
                },
        warn:
                function(msg)
                {
                    addMessage('warn', msg);
                },
        info:
                function(msg)
                {
                    addMessage('info', msg);
                },
        error:
                function(msg)
                {
                    addMessage('error', msg);
                },
        profile:
                function(label)
                {
                    var currentTime = new Date(); //record the current time when profile() is executed

                    if (label == undefined || label == '')
                    {
                        addMessage('error', '<b>ERROR:</b> Please specify a label for your profile statement');
                    }
                    else if (profiler[ label ])
                    {
                        addMessage('profile', [ label, ': ', currentTime - profiler[ label ],    'ms' ].join(''));
                        delete profiler[ label ];
                    }
                    else
                    {
                        profiler[ label ] = currentTime;
                        addMessage('profile', label);
                    }
                    return currentTime;
                }
    }

    Tapestry.onDOMLoaded(
        /* initialize Blackbird when the page loads */
            function()
            {
                messageTypes.debug = Tapestry.DEBUG_ENABLED;

                constructUI();

                rebuildOutputListClassName();

                backgroundImage();

                checkbox.observe("click", clickVis.bindAsEventListener());

                filters.observe("click", clickFilter.bindAsEventListener());
                controls.observe("click", clickControl.bindAsEventListener());

                document.observe("keyup", readKey.bindAsEventListener());

                resize(state.size);
                reposition(state.pos);

                if (state.load)
                {
                    show();
                    $(checkbox).checked = true;
                }

                scrollToBottom();

                // The original Blackbird code would unregister the events, but I believe that's not
                // necessary due to Prototype.
            }.bind(this));
})();
