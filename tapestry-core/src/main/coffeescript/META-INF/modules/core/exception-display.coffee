# Copyright 2012 The Apache Software Foundation
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

# ## core/exception-report
#
# Provides dynamic behavior for the core/ExceptionDisplay component; specifically,
# filtering the stack trace.
define ["core/spi"],
  (spi) ->

    spi.onDocument "click", "[data-behavior=stack-trace-filter-toggle]", ->
      checked = this.element.checked

      for traceList in spi.body().find "ul.t-stack-trace"
        traceList[if checked then "addClass" else "removeClass"] "t-filtered"

      return

    return null
