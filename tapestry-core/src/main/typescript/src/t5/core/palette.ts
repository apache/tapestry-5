// Copyright 2012-2025 The Apache Software Foundation
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

/**
 * ## t5/core/palette
 * 
 * Support for the `core/Palette` component.
 */
import dom from "t5/core/dom";
import _ from "underscore";
import events from "t5/core/events";
import { ElementWrapper }from "t5/core/types";

const isSelected = (option: any) => option.selected;

/**
 * Palette controller class.
 */
class PaletteController {
  selected: ElementWrapper;
  container: ElementWrapper;
  available: ElementWrapper;
  hidden: ElementWrapper;
  moveUp: ElementWrapper;
  moveDown: ElementWrapper;
  deselect: ElementWrapper;
  reorder: boolean;
  valueToOrderIndex: {};

  constructor(id: string) {
    this.selected = (dom(id))!;
    this.container = this.selected.findParent(".palette")!;
    this.available = this.container.findFirst(".palette-available select")!;
    this.hidden = this.container.findFirst("input[type=hidden]")!;

    this.selected = this.container.findFirst("[data-action=select]")!;
    this.deselect = this.container.findFirst("[data-action=deselect]")!;

    this.moveUp = this.container.findFirst("[data-action=move-up]")!;
    this.moveDown = this.container.findFirst("[data-action=move-down]")!;

    // Track where reorder is allowed based on whether the buttons actually exist
    this.reorder = this.moveUp !== null;

    this.valueToOrderIndex = {};

    // @ts-ignore
    for (let i = 0; i < this.available.element.options.length; i++) {
      // @ts-ignore
      var option = this.available.element.options[i];
      // @ts-ignore
      this.valueToOrderIndex[option.value] = i;
    }

    // This occurs even when the palette is disabled, to present the
    // values correctly. Otherwise it looks like nothing is selected.
    this.initialTransfer();

    // @ts-ignore
    if (!this.selected.element.disabled) {
      this.updateButtons();
      this.bindEvents();
    }
  }

  initialTransfer() {
    // Get the values for options that should move over
    let i, option;
    // @ts-ignore
    const values = JSON.parse(this.hidden.value());
    const valueToPosition = {};

    for (i = 0; i < values.length; i++) {
      var v = values[i];
      // @ts-ignore
      valueToPosition[v] = i;
    }

    const e = this.available.element;

    const movers = [];

    // @ts-ignore
    for (i = e.options.length - 1; i >= 0; i--) {
      // @ts-ignore
      option = e.options[i];
      var {
        value
      } = option;
      // @ts-ignore
      var pos = valueToPosition[value];
      if (pos !== undefined) {
        movers[pos] = option;
        // @ts-ignore
        e.remove(i);
      }
    }

    return (() => {
      const result = [];
      for (option of Array.from(movers)) {
        // @ts-ignore
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
    // @ts-ignore
    const values = (Array.from(this.selected.element.options).map((option) => option.value));
    return this.hidden.value(JSON.stringify(values));
  }

  bindEvents() {
    this.container.on("change", "select", () => {
      this.updateButtons();
      return false;
    });

    // @ts-ignore
    this.selected.on("click", () => {
      this.doSelect();
      return false;
    });

    // @ts-ignore
    this.available.on("dblclick", () => {
      this.doSelect();
      return false;
    });

    // @ts-ignore
    this.deselect.on("click", () => {
      this.doDeselect();
      return false;
    });

    // @ts-ignore
    this.selected.on("dblclick", () => {
      this.doDeselect();
      return false;
    });

    if (this.reorder) {
      // @ts-ignore      
      this.moveUp.on("click", () => {
        this.doMoveUp();
        return false;
      });

      // @ts-ignore
      return this.moveDown.on("click", () => {
        this.doMoveDown();
        return false;
      });
    }
  }

  // Invoked whenever the selections in either list changes or after an updates; figures out which buttons
  // should be enabled and which disabled.
  updateButtons() {
    // @ts-ignore
    this.selected.element.disabled = this.available.element.selectedIndex < 0;

    // @ts-ignore
    const nothingSelected = this.selected.element.selectedIndex < 0;

    // @ts-ignore
    this.deselect.element.disabled = nothingSelected;

    if (this.reorder) {
      // @ts-ignore
      this.moveUp.element.disabled = nothingSelected || this.allSelectionsAtTop();
      // @ts-ignore
      return this.moveDown.element.disabled = nothingSelected || this.allSelectionsAtBottom();
    }
  }

  doSelect() { return this.transferOptions(this.available, this.selected, this.reorder); }

  doDeselect() { return this.transferOptions(this.selected, this.available, false); }

  doMoveUp() {
    // @ts-ignore
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
    // @ts-ignore
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
  // @ts-ignore
  reorderSelected(options) {

    return this.performUpdate(true, options, () => {

      this.deleteOptions(this.selected);

      return Array.from(options).map((o) =>
        // @ts-ignore
        this.selected.element.add(o, null));
    });
  }

  // Performs the update, which includes the willChange and didChange events.
  // @ts-ignore
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
  // @ts-ignore
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
  // @ts-ignore
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

  // @ts-ignore
  insertOption(options, option, atEnd) {

    let before;
    if (!atEnd) {
      // @ts-ignore
      const optionOrder = this.valueToOrderIndex[option.value];
      // @ts-ignore
      before = _.find(options, o => this.valueToOrderIndex[o.value] > optionOrder);
    }

    if (before) {
      const i = _.indexOf(options, before);
      return options.splice(i, 0, option);
    } else {
      return options.push(option);
    }
  }

  // @ts-ignore
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
    // @ts-ignore
    const options = _.toArray(this.selected.element.options);

    // @ts-ignore
    return _(options.slice(0, +last + 1 || undefined)).all(o => o.selected);
  }

  allSelectionsAtBottom() {
    const e = this.selected.element;
    // @ts-ignore
    const last = e.selectedIndex;
    // @ts-ignore
    const options = _.toArray(e.options);

    // @ts-ignore
    return _(options.slice(last)).all(o => o.selected);
  }
}

// Export just the initializer function
export default (id: string) => new PaletteController(id)