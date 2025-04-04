/*
 * decaffeinate suggestions:
 * DS101: Remove unnecessary use of Array.from
 * DS102: Remove unnecessary code created because of implicit returns
 * DS205: Consider reworking code to avoid use of IIFEs
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
// Copyright 2012-2024 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// ## t5/core/palette
//
// Support for the `core/Palette` component.
define(["t5/core/dom", "underscore", "t5/core/events"],
  function(dom, _, events) {

    const isSelected = option => option.selected;

    class PaletteController {

      constructor(id) {
        this.selected = (dom(id));
        this.container = this.selected.findParent(".palette");
        this.available = this.container.findFirst(".palette-available select");
        this.hidden = this.container.findFirst("input[type=hidden]");

        this.select = this.container.findFirst("[data-action=select]");
        this.deselect = this.container.findFirst("[data-action=deselect]");

        this.moveUp = this.container.findFirst("[data-action=move-up]");
        this.moveDown = this.container.findFirst("[data-action=move-down]");

        // Track where reorder is allowed based on whether the buttons actually exist
        this.reorder = this.moveUp !== null;

        this.valueToOrderIndex = {};

        for (let i = 0; i < this.available.element.options.length; i++) {
          var option = this.available.element.options[i];
          this.valueToOrderIndex[option.value] = i;
        }

        // This occurs even when the palette is disabled, to present the
        // values correctly. Otherwise it looks like nothing is selected.
        this.initialTransfer();

        if (!this.selected.element.disabled) {
          this.updateButtons();
          this.bindEvents();
        }
      }

      initialTransfer() {
        // Get the values for options that should move over
        let i, option;
        const values = JSON.parse(this.hidden.value());
        const valueToPosition = {};

        for (i = 0; i < values.length; i++) {
          var v = values[i];
          valueToPosition[v] = i;
        }

        const e = this.available.element;

        const movers = [];

        for (i = e.options.length - 1; i >= 0; i--) {
          option = e.options[i];
          var {
            value
          } = option;
          var pos = valueToPosition[value];
          if (pos !== undefined) {
            movers[pos] = option;
            e.remove(i);
          }
        }

        return (() => {
          const result = [];
          for (option of Array.from(movers)) {
            result.push(this.selected.element.add(option));
          }
          return result;
        })();
      }

      // Invoked after any change to the selections list to update the hidden field as well as the
      // buttons' state.
      updateAfterChange() {
        this.updateHidden();
        return this.updateButtons();
      }

      updateHidden() {
        const values = (Array.from(this.selected.element.options).map((option) => option.value));
        return this.hidden.value(JSON.stringify(values));
      }

      bindEvents() {
        this.container.on("change", "select", () => {
          this.updateButtons();
          return false;
        });

        this.select.on("click", () => {
          this.doSelect();
          return false;
        });

        this.available.on("dblclick", () => {
          this.doSelect();
          return false;
        });

        this.deselect.on("click", () => {
          this.doDeselect();
          return false;
        });

        this.selected.on("dblclick", () => {
          this.doDeselect();
          return false;
        });

        if (this.reorder) {
          this.moveUp.on("click", () => {
            this.doMoveUp();
            return false;
          });

          return this.moveDown.on("click", () => {
            this.doMoveDown();
            return false;
          });
        }
      }

      // Invoked whenever the selections in either list changes or after an updates; figures out which buttons
      // should be enabled and which disabled.
      updateButtons() {
        this.select.element.disabled = this.available.element.selectedIndex < 0;

        const nothingSelected = this.selected.element.selectedIndex < 0;

        this.deselect.element.disabled = nothingSelected;

        if (this.reorder) {
          this.moveUp.element.disabled = nothingSelected || this.allSelectionsAtTop();
          return this.moveDown.element.disabled = nothingSelected || this.allSelectionsAtBottom();
        }
      }

      doSelect() { return this.transferOptions(this.available, this.selected, this.reorder); }

      doDeselect() { return this.transferOptions(this.selected, this.available, false); }

      doMoveUp() {
        let options = _.toArray(this.selected.element.options);

        const groups = _.partition(options, isSelected);

        const movers = groups[0];

        // The element before the first selected element is the pivot; all the selected elements will
        // move before the pivot. If there is no pivot, the elements are shifted to the front of the list.
        const firstMoverIndex = _.first(movers).index;
        const pivot = options[firstMoverIndex - 1];

        options = groups[1];

        const splicePos = pivot ? _.indexOf(options, pivot) : 0;

        movers.reverse();

        for (var o of Array.from(movers)) {
          options.splice(splicePos, 0, o);
        }

        return this.reorderSelected(options);
      }


      doMoveDown() {
        let options = _.toArray(this.selected.element.options);

        const groups = _.partition(options, isSelected);

        const movers = groups[0];

        // The element after the last selected element is the pivot; all the selected elements will
        // move after the pivot. If there is no pivot, the elements are shifted to the end of the list.
        const lastMoverIndex = movers.slice(-1)[0].index;
        const pivot = options[lastMoverIndex + 1];

        options = groups[1];

        const splicePos = pivot ? _.indexOf(options, pivot) + 1 : options.length;

        movers.reverse();

        for (var o of Array.from(movers)) {
          options.splice(splicePos, 0, o);
        }

        return this.reorderSelected(options);
      }

      // Reorders the selected options to the provided list of options; handles triggering the willUpdate and
      // didUpdate events.
      reorderSelected(options) {

        return this.performUpdate(true, options, () => {

          this.deleteOptions(this.selected);

          return Array.from(options).map((o) =>
            this.selected.element.add(o, null));
        });
      }

      // Performs the update, which includes the willChange and didChange events.
      performUpdate(reorder, selectedOptions, updateCallback) {

        let canceled = false;

        const doUpdate = () => {
          updateCallback();

          this.selected.trigger(events.palette.didChange, { selectedOptions, reorder });

          return this.updateAfterChange();
        };

        const memo = {
          selectedOptions,
          reorder,
          cancel() { return canceled = true; },
          defer() {
            canceled = true;
            return doUpdate;
          }
        };

        this.selected.trigger(events.palette.willChange, memo);

        if (!canceled) { return doUpdate(); }
      }

      // Deletes all options from a select (an ElementWrapper), prior to new options being populated in.
      deleteOptions(select) {

        const e = select.element;

        return (() => {
          const result = [];
          for (let i = e.length - 1; i >= 0; i--) {
            result.push(e.remove(i));
          }
          return result;
        })();
      }

      // Moves options between the available and selected lists, including event notifiations before and after.
      transferOptions(from, to, atEnd) {

        let o;
        if (from.element.selectedIndex === -1) {
          return;
        }

        // This could be done in a single pass, but:
        const movers = _.filter(from.element.options, isSelected);
        const fromOptions = _.reject(from.element.options, isSelected);

        const toOptions = _.toArray(to.element.options);

        for (o of Array.from(movers)) {
          this.insertOption(toOptions, o, atEnd);
        }

        const isSelectedSelect = to === this.selected;
        const selectedOptions = isSelectedSelect ? toOptions : fromOptions;

        return this.performUpdate(false, selectedOptions, () => {
          let i;
          for (i = from.element.length - 1; i >= 0; i--) {
            if (from.element.options[i].selected) {
              from.element.remove(i);
            }
          }

          // A bit ugly: update the to select by removing all, then adding back in.

          for (i = to.element.length - 1; i >= 0; i--) {
            to.element.options[i].selected = false;
            to.element.remove(i);
          }

          return (() => {
            const result = [];
            for (o of Array.from(toOptions)) {
              var groupIdx = o.getAttribute('data-optgroup-idx');
              if (isSelectedSelect || !groupIdx  || (groupIdx === '')) {
                result.push(to.element.add(o, null));
              } else {
                var group = to.element.children[parseInt(groupIdx)];
                result.push(group.appendChild(o));
              }
            }
            return result;
          })();
        });
      }


      insertOption(options, option, atEnd) {

        let before;
        if (!atEnd) {
          const optionOrder = this.valueToOrderIndex[option.value];
          before = _.find(options, o => this.valueToOrderIndex[o.value] > optionOrder);
        }

        if (before) {
          const i = _.indexOf(options, before);
          return options.splice(i, 0, option);
        } else {
          return options.push(option);
        }
      }


      indexOfLastSelection(select) {
        const e = select.element;
        if (e.selectedIndex < 0) {
          return -1;
        }

        for (let i = e.options.length - 1, end = e.selectedIndex; i >= end; i--) {
          if (e.options[i].selected) {
            return i;
          }
        }

        return -1;
      }

      allSelectionsAtTop() {
        const last = this.indexOfLastSelection(this.selected);
        const options = _.toArray(this.selected.element.options);

        return _(options.slice(0, +last + 1 || undefined)).all(o => o.selected);
      }

      allSelectionsAtBottom() {
        const e = this.selected.element;
        const last = e.selectedIndex;
        const options = _.toArray(e.options);

        return _(options.slice(last)).all(o => o.selected);
      }
    }


    // Export just the initializer function
    return id => new PaletteController(id);
});