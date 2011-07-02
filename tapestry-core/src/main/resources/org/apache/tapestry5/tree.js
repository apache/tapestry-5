/* Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

T5.extend(T5, {
	tree : {

		/**
		 * Approximate time per pixel for the hide and reveal animations. The
		 * idea is to have small (few children) and large (many childen)
		 * animations operate at the same visible rate, even though they will
		 * take different amounts of time.
		 */
		ANIMATION_RATE : .005,

		/**
		 * Maximum animation time, in seconds. This is necessary for very large
		 * animations, otherwise its looks visually odd to see the child tree
		 * nodes whip down the screen.
		 */
		MAX_ANIMATION_DURATION : .5,

		/** Type of Scriptaculous effect to hide/show child nodes. */
		TOGGLE_TYPE : 'blind',

		/**
		 * Name of Scriptaculous effects queue to ensure that animations do not
		 * overwrite each other.
		 */
		QUEUE_NAME : 't-tree-updates'
	}
});

T5.extendInitializer(function() {

	var cfg = T5.tree;

	function doAnimate(element) {
		var sublist = $(element).up('li').down("ul");

		var dim = sublist.getDimensions();

		var duration = Math.min(dim.height * cfg.ANIMATION_RATE,
				cfg.MAX_ANIMATION_DURATION)

		new Effect.toggle(sublist, cfg.TOGGLE_TYPE, {
			duration : duration,
			queue : {
				position : 'end',
				scope : cfg.QUEUE_NAME
			}
		});
	}

	function animateRevealChildren(element) {
		$(element).addClassName("t-tree-expanded");

		doAnimate(element);
	}

	function animateHideChildren(element) {
		$(element).removeClassName("t-tree-expanded");

		doAnimate(element);
	}

	function initializer(spec) {
		var loaded = spec.expanded;
		var expanded = spec.expanded;
        var selected = false;

		if (expanded) {
			$(spec.clientId).addClassName("t-tree-expanded")
		}
		var loading = false;

		function successHandler(reply) {
			// Remove the Ajax load indicator
			$(spec.clientId).update("");
			$(spec.clientId).removeClassName("t-empty-node");

			var response = reply.responseJSON;

			Tapestry.loadScriptsInReply(response, function() {
				var element = $(spec.clientId).up("li");
				var label = element.down("span.t-tree-label");

				label.insert({
					after : response.content
				});

				// Hide the new sublist so that we can animate revealing it.
				element.down("ul").hide();

				animateRevealChildren(spec.clientId);

				loading = false;
				loaded = true;
				expanded = true;
			});

		}

        function toggleLeafHandler(reply) {
           var response = reply.responseJSON;

                $(spec.clientId).update("");

                Tapestry.loadScriptsInReply(response, function() {
                    loading = false;
                    loaded = true;
                    expanded = true;
                    selected = !selected;
                });
        }

		function doLoad() {
			if (loading)
				return;

			loading = true;

            if(spec.expandChildrenURL)
            {
			    $(spec.clientId).addClassName("t-empty-node");
            }
            else
            {
                $(spec.clientId).next("span.t-tree-label").addClassName("t-selected-leaf-node-label");
            }
			$(spec.clientId).update("<span class='t-ajax-wait'/>");

            var requestURL = spec.expandChildrenURL? spec.expandChildrenURL:spec.toggleLeafURL;

            var handler =  spec.expandChildrenURL? successHandler: toggleLeafHandler;

			Tapestry.ajaxRequest(requestURL, handler);

		}

		$(spec.clientId).observe("click", function(event) {
			event.stop();

			if (!loaded) {

				doLoad();

				return;
			}

            if(spec.toggleLeafURL)
            {
                var label = $(spec.clientId).next("span.t-tree-label");

                if(selected)
                {
                    label.removeClassName("t-selected-leaf-node-label");
                }
                else
                {
                     label.addClassName("t-selected-leaf-node-label");
                }
                selected = !selected;

                Tapestry.ajaxRequest(spec.toggleLeafURL, {});

                return;
            }

			// Children have been loaded, just a matter of toggling
			// between showing or hiding the children.

			var f = expanded ? animateHideChildren : animateRevealChildren;

			f.call(null, spec.clientId);

			var url = expanded ? spec.markCollapsedURL : spec.markExpandedURL;

			Tapestry.ajaxRequest(url, {});

			expanded = !expanded;
		});
	}

	return {
		treeNode : initializer
	};
});
