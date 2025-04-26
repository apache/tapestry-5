// Copyright 2013, 2025 The Apache Software Foundation
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
 * ## t5/core/time-interval
 *
 * Used with the Interval component to express the interval between two timestamps,
 * or the dynamic difference between now and an end point in the past or future.
 * @packageDocumentation
 */

import dom from "t5/core/dom";
import moment from "t5/core/moment";

const ATTR = "data-timeinterval";

const DEFAULT_FORMAT = 'YYYY-MM-DDTHH:mm:ss.SSSZ';

// @ts-ignore
const toMoment = function(s) { if (s) { return (moment(s, DEFAULT_FORMAT)); } else { return moment(); } };

// @ts-ignore
const updateElement = function(el) {
  const start = toMoment(el.attr("data-timeinterval-start"));
  const end = toMoment(el.attr("data-timeinterval-end"));
  const plain = el.attr("data-timeinterval-plain");

  el.update(end.from(start, plain));
};

const updateDynamics = function() {
  for (var el of Array.from(dom.body.find(`[${ATTR}=dynamic]`))) {
    updateElement(el);
  }
};

// Update any dynamic intervals (the ones without a specific start date) about once a second
setInterval(updateDynamics, 1000);

dom.scanner(`[${ATTR}=true]`, function(el) {

  updateElement(el);

  if ((el.attr("data-timeinterval-start")) && (el.attr("data-timeinterval-end"))) {
    el.attr(ATTR, null);
  } else {
    el.attr(ATTR, "dynamic");
  }

});

// no exports
