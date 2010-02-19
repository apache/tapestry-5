//  Copyright 2008, 2010 The Apache Software Foundation
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

    initialize: function(spec)
    {
        this.form = $(spec.form);
        this.element = $(spec.clientId);

        this.element.observe("click", this.onClick.bindAsEventListener(this));
    },

    createHidden : function()
    {
        var hidden = new Element("input", { "type":"hidden",
            "id": this.element.id + "-hidden",
            "name": this.element.id + "-hidden",
            "value": this.element.id});

        if (this.form.select("input#" + this.element.id + "-hidden").length == 0)
            this.element.insert({after:hidden});
    },

    onClick : function(event)
    {
        // Tapestry.debug("LinkSubmit #{id} clicked.", this.element);

        Event.stop(event);

        var onsubmit = this.form.onsubmit;
        
        this.createHidden();
        
        if (onsubmit == undefined || onsubmit.call(window.document, event))
        {    
            this.form.submit();
        }

        return false;
    }
});

Tapestry.Initializer.linkSubmit = function(spec)
{
    new Tapestry.LinkSubmit(spec);
}