# Copyright 2013 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# ## t5/core/bootstrap
#
# Utilities for leveraging Bootstrap
define ["t5/core/bootstrap"],
  ->

    exports =
      # Generates the CSS class name for an icon.
      #
      # * name - of icon, e.g., "arrow-left"
      glyph: (name) -> """<span class="glyphicon glyphicon-#{name}"></span>"""

    return exports


