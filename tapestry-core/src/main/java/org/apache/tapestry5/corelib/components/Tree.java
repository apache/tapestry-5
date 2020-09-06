// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.internal.util.CaptureResultCallback;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.tree.*;

import java.util.List;

/**
 * A component used to render a recursive tree structure, with expandable/collapsable/selectable nodes. The data that is displayed
 * by the component is provided as a {@link TreeModel}. A secondary model, the {@link TreeExpansionModel}, is used
 * to track which nodes have been expanded. The optional {@link TreeSelectionModel} is used to track node selections (as currently
 * implemented, only leaf nodes may be selected).
 *
 * Tree is <em>not</em> a form control component; all changes made to the tree on the client
 * (expansions, collapsing, and selections) are propagated immediately back to the server.
 *
 * The Tree component uses special tricks to support recursive rendering of the Tree as necessary.
 *
 * @tapestrydoc
 * @since 5.3
 */
@SuppressWarnings(
        {"rawtypes", "unchecked", "unused"})
@Events({EventConstants.NODE_SELECTED, EventConstants.NODE_UNSELECTED})
@Import(module = "t5/core/tree")
public class Tree
{
    /**
     * The model that drives the tree, determining top level nodes and making revealing the overall structure of the
     * tree.
     */
    @Parameter(required = true, autoconnect = true)
    private TreeModel model;

    /**
     * Allows the container to specify additional CSS class names for the outer DIV element. The outer DIV
     * always has the class name "tree-container"; the additional class names are typically used to apply
     * a specific size and width to the component.
     */
    @Parameter(name = "class", defaultPrefix = BindingConstants.LITERAL)
    private String className;

    /**
     * Optional parameter used to inform the container about what TreeNode is currently rendering; this
     * is primarily used when the label parameter is bound.
     */
    @Property
    @Parameter
    private TreeNode node;

    /**
     * Used to control the Tree's expansion model. By default, a persistent field inside the Tree
     * component stores a {@link DefaultTreeExpansionModel}. This parameter may be bound when more
     * control over the implementation of the expansion model, or how it is stored, is
     * required.
     */
    @Parameter(allowNull = false, value = "defaultTreeExpansionModel")
    private TreeExpansionModel expansionModel;

    /**
     * Used to control the Tree's selections. When this parameter is bound, then the client-side Tree
     * will track what is selected or not selected, and communicate this (via Ajax requests) up to
     * the server, where it will be recorded into the model. On the client-side, the Tree component will
     * add or remove the {@code selected-leaf-node-label} CSS class from {@code span.tree-label}
     * for the node.
     */
    @Parameter
    private TreeSelectionModel selectionModel;

    /**
     * Optional parameter used to inform the container about the value of the currently rendering TreeNode; this
     * is often preferable to the TreeNode, and like the node parameter, is primarily used when the label parameter
     * is bound.
     */
    @Parameter
    private Object value;

    /**
     * A renderable (usually a {@link Block}) that can render the label for a tree node.
     * This will be invoked after the {@link #value} parameter has been updated.
     */
    @Property
    @Parameter(value = "block:defaultRenderTreeNodeLabel")
    private RenderCommand label;

    @Environmental
    private JavaScriptSupport jss;

    @Inject
    private ComponentResources resources;

    @Persist
    private TreeExpansionModel defaultTreeExpansionModel;

    private static RenderCommand RENDER_CLOSE_TAG = new RenderCommand()
    {
        public void render(MarkupWriter writer, RenderQueue queue)
        {
            writer.end();
        }
    };

    private static RenderCommand RENDER_LABEL_SPAN = new RenderCommand()
    {
        public void render(MarkupWriter writer, RenderQueue queue)
        {
            writer.element("span", "class", "tree-label");
        }
    };

    private static RenderCommand MARK_SELECTED = new RenderCommand()
    {
        public void render(MarkupWriter writer, RenderQueue queue)
        {
            writer.getElement().attribute("class", "selected-leaf-node");
        }
    };

    @Environmental
    private Heartbeat heartbeat;

    /**
     * Renders a single node (which may be the last within its containing node).
     * This is a mix of immediate rendering, and queuing up various Blocks and Render commands
     * to do the rest. May recursively render child nodes of the active node. The label part
     * of the node is rendered inside a {@linkplain org.apache.tapestry5.services.Heartbeat heartbeat}.
     *
     * @param node
     *         to render
     * @param isLast
     *         if true, add "last" attribute to the LI element
     * @return command to render the node
     */
    private RenderCommand toRenderCommand(final TreeNode node, final boolean isLast)
    {
        return new RenderCommand()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                // Inform the component's container about what value is being rendered
                // (this may be necessary to generate the correct label for the node).
                Tree.this.node = node;

                value = node.getValue();

                boolean isLeaf = node.isLeaf();

                writer.element("li");

                if (isLast)
                {
                    writer.attributes("class", "last");
                }

                if (isLeaf)
                {
                    writer.getElement().attribute("class", "leaf-node");
                }

                Element e = writer.element("span", "class", "tree-icon");

                if (!isLeaf && !node.getHasChildren())
                {
                    e.addClassName("empty-node");
                }

                boolean hasChildren = !isLeaf && node.getHasChildren();
                boolean expanded = hasChildren && expansionModel.isExpanded(node);

                writer.attributes("data-node-id", node.getId());

                if (expanded)
                {
                    // Inform the client side, so it doesn't try to fetch it a second time.
                    e.addClassName("tree-expanded");
                }

                writer.end(); // span.tree-icon

                // From here on in, we're pushing things onto the queue. Remember that
                // execution order is reversed from order commands are pushed.

                queue.push(RENDER_CLOSE_TAG); // li

                if (expanded)
                {
                    queue.push(new RenderNodes(node.getChildren()));
                }

                queue.push(RENDER_CLOSE_TAG);
                final RenderCommand startHeartbeat = new RenderCommand() {

                    @Override
                    public void render(MarkupWriter writer, RenderQueue queue) {
                        heartbeat.begin();
                    }
                };

                final RenderCommand endHeartbeat = new RenderCommand() {

                    @Override
                    public void render(MarkupWriter writer, RenderQueue queue) {
                        heartbeat.end();
                    }
                };

                queue.push(endHeartbeat);

                queue.push(label);

                queue.push(startHeartbeat);

                if (isLeaf && selectionModel != null && selectionModel.isSelected(node))
                {
                    queue.push(MARK_SELECTED);
                }

                queue.push(RENDER_LABEL_SPAN);

            }
        };
    }

    /**
     * Renders an &lt;ul&gt; element and renders each node recursively inside the element.
     */
    private class RenderNodes implements RenderCommand
    {
        private final Flow<TreeNode> nodes;

        public RenderNodes(List<TreeNode> nodes)
        {
            assert !nodes.isEmpty();

            this.nodes = F.flow(nodes).reverse();
        }

        public void render(MarkupWriter writer, final RenderQueue queue)
        {
            writer.element("ul");
            queue.push(RENDER_CLOSE_TAG);

            queue.push(toRenderCommand(nodes.first(), true));

            nodes.rest().each(new Worker<TreeNode>()
            {
                public void work(TreeNode element)
                {
                    queue.push(toRenderCommand(element, false));
                }
            });
        }

    }

    public String getContainerClass()
    {
        return className == null ? "tree-container" : "tree-container " + className;
    }

    public Link getTreeActionLink()
    {
        return resources.createEventLink("treeAction");
    }

    Object onTreeAction(@RequestParameter("t:nodeid") String nodeId,
                        @RequestParameter("t:action") String action)
    {
        if (action.equalsIgnoreCase("expand"))
        {
            return doExpandChildren(nodeId);
        }

        if (action.equalsIgnoreCase("markExpanded"))
        {
            return doMarkExpanded(nodeId);
        }

        if (action.equalsIgnoreCase("markCollapsed"))
        {
            return doMarkCollapsed(nodeId);
        }

        if (action.equalsIgnoreCase("select"))
        {
            return doUpdateSelected(nodeId, true);
        }

        if (action.equalsIgnoreCase("deselect"))
        {
            return doUpdateSelected(nodeId, false);
        }

        throw new IllegalArgumentException(String.format("Unexpected action: '%s' for Tree component.", action));
    }

    Object doExpandChildren(String nodeId)
    {
        TreeNode container = model.getById(nodeId);

        expansionModel.markExpanded(container);

        return new RenderNodes(container.getChildren());
    }

    Object doMarkExpanded(String nodeId)
    {
        expansionModel.markExpanded(model.getById(nodeId));

        return new JSONObject();
    }


    Object doMarkCollapsed(String nodeId)
    {
        expansionModel.markCollapsed(model.getById(nodeId));

        return new JSONObject();
    }

    Object doUpdateSelected(String nodeId, boolean selected)
    {
        TreeNode node = model.getById(nodeId);

        String event;

        if (selected)
        {
            selectionModel.select(node);

            event = EventConstants.NODE_SELECTED;
        } else
        {
            selectionModel.unselect(node);

            event = EventConstants.NODE_UNSELECTED;
        }

        CaptureResultCallback<Object> callback = CaptureResultCallback.create();

        resources.triggerEvent(event, new Object[]{nodeId}, callback);

        final Object result = callback.getResult();

        if (result != null)
        {
            return result;
        }

        return new JSONObject();
    }

    public TreeExpansionModel getDefaultTreeExpansionModel()
    {
        if (defaultTreeExpansionModel == null)
        {
            defaultTreeExpansionModel = new DefaultTreeExpansionModel();
        }

        return defaultTreeExpansionModel;
    }

    /**
     * Returns the actual {@link TreeExpansionModel} in use for this Tree component,
     * as per the expansionModel parameter. This is often, but not always, the same
     * as {@link #getDefaultTreeExpansionModel()}.
     */
    public TreeExpansionModel getExpansionModel()
    {
        return expansionModel;
    }

    /**
     * Returns the actual {@link TreeSelectionModel} in use for this Tree component,
     * as per the {@link #selectionModel} parameter.
     */
    public TreeSelectionModel getSelectionModel()
    {
        return selectionModel;
    }

    public Object getRenderRootNodes()
    {
        return new RenderNodes(model.getRootNodes());
    }

    /**
     * Clears the tree's {@link TreeExpansionModel}.
     */
    public void clearExpansions()
    {
        expansionModel.clear();
    }

    public Boolean getSelectionEnabled()
    {
        return selectionModel != null ? true : null;
    }
}
