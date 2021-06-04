# Copyright 2012-2013 The Apache Software Foundation
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

# ## t5/core/exception-display
#
# Provides dynamic behavior for the t5/core/ExceptionDisplay component; specifically,
# filtering the stack trace.
define ["t5/core/dom"],
  (dom) ->

    dom.onDocument "click", "[data-behavior=stack-trace-filter-toggle]", ->
      checked = @element.checked

      for traceList in dom.body.find ".stack-trace"
        traceList[if checked then "addClass" else "removeClass"] "filtered"

      return

    return null
