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

## core/translator
#
# Support for Tapestry's built-in set of translators and validators.
#
define ["core/spi", "core/events", "core/utils", "core/fields"],
  (spi, events, utils) ->

    spi.onDocument events.field.optional, "[data-optionality=required]", (event, memo) ->

      if utils.isBlank memo.value
        message = (this.attribute "data-required-message") || "REQUIRED"
        this.trigger events.field.showValidationError, { message }
        memo.error = true
        return false

    configureDecimals: ->