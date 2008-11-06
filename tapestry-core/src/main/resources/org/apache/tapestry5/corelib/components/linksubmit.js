//  Copyright 2008 The Apache Software Foundation
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

Tapestry.LinkSubmit = Class.create({

    initialize: function(formId, clientId)
    {
        this.form = $(formId);
        this.element = $(clientId);

        this.element.observe("click", this.onClick.bindAsEventListener(this));
    },

    createHidden : function()
    {
        var hidden = new Element("input", { "type":"hidden",
            "name": this.element.id + ":hidden",
            "value": this.element.id});

        this.element.insert({after:hidden});
    },

    onClick : function(event)
    {
        // Tapestry.debug("LinkSubmit #{id} clicked.", this.element);

        Event.stop(event);

        var onsubmit = this.form.onsubmit;

        if (onsubmit == undefined || onsubmit.call(window.document, event))
        {
            this.createHidden();
            this.form.submit();
        }

        return false;
    }
});

Tapestry.Initializer.linkSubmit = function(formId, clientId)
{
    new Tapestry.LinkSubmit(formId, clientId);
}