// Copyright 2007, 2008, 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

Tapestry.Palette = Class.create({

    // id: of main select element                                                                                                 ˜
    // reorder: true to enable extra controls for changing selection order
    // naturalOrder: array of values, the proper order for the elements (needed when de-selecting items)
    initialize : function(id, reorder, naturalOrder)
    {
        this.reorder = reorder;
        // The SELECT elements

        this.avail = $(id + "-avail");
        this.selected = $(id);

        this.hidden = $(id + "-values");

        // The BUTTON elements
        this.select = $(id + "-select");
        this.deselect = $(id + "-deselect");

        if (this.reorder)
        {
            this.up = $(id + "-up");
            this.down = $(id + "-down");
        }

        this.valueToOrderIndex = {};

        naturalOrder.each(function (value, i)
        {
            this.valueToOrderIndex[value] = i;
        }.bind(this));

        this.bindEvents();
    },

    bindEvents : function()
    {
        var updateButtons = this.updateButtons.bindAsEventListener(this);
        this.avail.observe("change", updateButtons);
        this.selected.observe("change", updateButtons);

        var selectClicked = this.selectClicked.bindAsEventListener(this);
        this.select.observe("click", selectClicked);
        this.avail.observe("dblclick", selectClicked);

        var deselectClicked = this.deselectClicked.bindAsEventListener(this);
        this.deselect.observe("click", deselectClicked);
        this.selected.observe("dblclick", deselectClicked);

        if (this.reorder)
        {
            this.up.observe("click", this.moveUpClicked.bindAsEventListener(this));
            this.down.observe("click", this.moveDownClicked.bindAsEventListener(this));
        }
    },

    updateButtons: function()
    {
        this.select.disabled = this.avail.selectedIndex < 0;

        var nothingSelected = this.selected.selectedIndex < 0;

        this.deselect.disabled = nothingSelected;

        if (this.reorder)
        {
            this.up.disabled = nothingSelected || this.allSelectionsAtTop();
            this.down.disabled = nothingSelected || this.allSelectionsAtBottom();
        }
    },

    indexOfLastSelection : function(select)
    {
        if (select.selectedIndex < 0) return -1;

        for (var i = select.options.length - 1; i >= select.selectedIndex; i--)
        {
            if (select.options[i].selected) return i;
        }

        return -1;
    },

    allSelectionsAtTop: function()
    {
        var last = this.indexOfLastSelection(this.selected);
        var options = $A(this.selected.options);

        return ! options.slice(0, last).any(function (o)
        {
            return ! o.selected;
        });
    },

    allSelectionsAtBottom : function()
    {
        var options = $A(this.selected.options);

        // Make sure that all elements from the (first) selectedIndex to the end are also selected.
        return options.slice(this.selected.selectedIndex).all(function(o)
        {
            return o.selected;
        });
    },

    selectClicked : function(event)
    {
        this.transferOptions(this.avail, this.selected, this.reorder);

        Event.stop(event);
    },

    deselectClicked : function(event)
    {
        this.transferOptions(this.selected, this.avail, false);

        Event.stop(event);
    },

    transferOptions : function (from, to, atEnd)
    {
        // don't bother moving the options if nothing is selected. this can happen
        // if you double-click a disabled option
        if (from.selectedIndex == -1)
            return;

        // from: SELECT to move option(s) from (those that are selected)
        // to: SELECT to add option(s) to
        // atEnd : if true, add at end, otherwise by natural sort order
        var toOptions = $A(to.options);

        toOptions.each(function(option)
        {
            option.selected = false;
        });

        var movers = this.removeSelectedOptions(from);
        this.moveOptions(movers, to, atEnd);

    },

    updateHidden : function()
    {
        // Every value in the selected list (whether enabled or not) is combined to form the value.
        var values = $A(this.selected).map(function(o)
        {
            return o.value;
        });

        this.hidden.value = values.toJSON();
    },

    moveUpClicked : function(event)
    {
        var pos = this.selected.selectedIndex - 1;
        var movers = this.removeSelectedOptions(this.selected);

        var before = pos < 0 ? this.selected.options[0] : this.selected.options[pos];

        this.reorderSelected(movers, before);

        Event.stop(event);
    },

    removeSelectedOptions : function(select)
    {
        var movers = [];
        var options = select.options;

        for (var i = select.selectedIndex; i < select.length; i++)
        {
            var option = options[i];
            if (option.selected)
            {
                select.remove(i--);
                movers.push(option);
            }
        }

        return movers;
    },

    moveOptions : function(movers, to, atEnd)
    {

        movers.each(function(option)
        {
            this.moveOption(option, to, atEnd);
        }.bind(this));

        this.updateHidden();
        this.updateButtons();
    },

    moveOption : function(option, to, atEnd)
    {
        var before = null;

        if (!atEnd)
        {
            var optionOrder = this.valueToOrderIndex[option.value];
            var candidate = $A(to.options).find(function (o)
            {
                return this.valueToOrderIndex[o.value] > optionOrder;
            }.bind(this));
            if (candidate) before = candidate;
        }
        this.addOption(to, option, before);
    },

    addOption : function(to, option, before)
    {
        try
        {
            to.add(option, before);
        }
        catch (ex)
        {
            //probably IE complaining about type mismatch for before argument;
            if (before == null)
            {
                //just add to the end...
                to.add(option);
            }
            else
            {
                //use option index property...
                to.add(option, before.index);
            }
        }

    },

    moveDownClicked : function(event)
    {
        var lastSelected = $A(this.selected.options).reverse(true).find(function (option)
        {
            return option.selected;
        });
        var lastPos = lastSelected.index;
        var before = this.selected.options[lastPos + 2];

        // TODO: needs to be "reorder options"
        this.reorderSelected(this.removeSelectedOptions(this.selected), before);

        Event.stop(event);
    },

    reorderSelected : function(movers, before)
    {
        movers.each(function(option)
        {
            this.addOption(this.selected, option, before);
        }.bind(this));

        this.updateHidden();
        this.updateButtons();
    }
});



