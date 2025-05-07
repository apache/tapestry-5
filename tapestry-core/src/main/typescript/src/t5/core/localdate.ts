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
 * ## t5/core/localdate
 * 
 * Used with the LocalDate component to present a Date in a particular format, in the
 * browser's time zone.
 */
import dom from "t5/core/dom"
import moment from "t5/core/moment";

const ATTR = "data-localdate-format";

dom.scanner(`[${ATTR}]`, function(el) {
  const format = el.attr(ATTR) as string;

  const isoString = el.text();

  const m = moment(isoString);

  el.update(m.format(format));

  // A good scanner callback always removes itself from future scans.
  el.attr(ATTR, null);

});
