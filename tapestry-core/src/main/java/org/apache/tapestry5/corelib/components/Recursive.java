// Licensed to the Apache License, Version 2.0 (the "License");
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

package org.apache.tapestry5.corelib.components;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.commons.RecursiveValue;
import org.apache.tapestry5.commons.RecursiveValueProvider;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Node;
import org.apache.tapestry5.internal.RecursiveContext;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * {@link Loop}-like component that renders its templates recursively based on {@link Recursive} parent-child relationships.
 * The objects should have one or more corresponding {@link RecursiveValueProvider}
 * implementations to convert them to {@link Recursive} instances.
 * The insertion point for rendering children is defined by the 
 * {@link RecursiveBody} component, which can only be used once inside
 * a <code>Recursive</code> instance. 
 * </p>
 * <p>
 * This was contributed by <a href="https://www.pubfactory.com">KGL PubFactory</a>.
 * </p>
 * @since 5.9.0
 */
public class Recursive implements RecursiveContext.Provider
{
    
    private static final Logger logger = LoggerFactory.getLogger(Recursive.class);

    static final String RECURSIVE_INSERTION_POINT_ELEMENT_NAME = "recursiveInsertionPoint";
    
    static final String ITERATION_WRAPPER_ELEMENT_NAME = "iterationWrapper";
    
    static final String PLACEHOLDER_PREFIX = "placeholder-";

    private static final int ZERO = 0;

    /**
     * A list containing the objects instances to be rendered. It can be just the root
     * elements (the ones without a parent) or all of them: this component takes care of both scenarios.
     */
    @Parameter(required = true, allowNull = false)
    private Iterable<?> source;
    
    /**
     * The max depth to render.  A value of null or <= 0 will result in rendering the entire tree.
     */
    @Parameter(required = false, allowNull = true)
    private Integer depth;
    
   /**
     * The desired client id, which defaults to the component's id.  If ever using nested Recursive
     * components, it is critical that each use a unique clientId.  This value is the root used to
     * build placeholder identifiers elements that are used to in the cleanupRender phase to restructure
     * the DOM into the necessary recursive tree structure.  If a nested Recursive component is using
     * the same clientId, it can end up mixing up the nodes of the two recursive trees.
     */
    @Parameter(value = "prop:resources.id", defaultPrefix = BindingConstants.LITERAL)
    private String clientId;
    
    /**
     * The current depth of the recursion.
     */
    @Parameter
    private int currentDepth;
    
    /**
     * Current value being rendered.
     */
    @Parameter
    private Object value;
    
    private Iterator<RecursiveValue<?>> iterator;
    
    @Inject
    @Property
    private ComponentResources resources;
    
    private RecursiveValue<?> recursiveValue;
    
    @Inject
    private Environment environment;
    
    @Inject
    private RecursiveValueProvider recursiveValueProvider;
    
    @Inject
    private JavaScriptSupport javaScriptSupport;
    
    private Map<String, String> childToParentMap;
    
    private Map<String, RecursiveValue<?>> idToRecursiveValueMap;
    
    private Map<String, Integer> idToDepthMap;
    
    private Element wrapper;
    
    private List<Element> iterationWrappers;
    
    private List<Element> placeholders;
    
    private Set<String> ids;
    
    void setupRender(MarkupWriter writer) {
        
        idToRecursiveValueMap = new HashMap<>();
        idToDepthMap = new HashMap<>();
        ids = new HashSet<>();
        iterationWrappers = new ArrayList<Element>();
        placeholders = new ArrayList<Element>();
        wrapper = writer.element("wrapper");
        childToParentMap = new HashMap<String, String>();
        environment.push(RecursiveContext.class, new RecursiveContext(this));
        
        // Visit values in tree order
        List<RecursiveValue<?>> toStack = new ArrayList<RecursiveValue<?>>();
        Iterator<?> sourceIterator = source.iterator();
        int i = 1;
        while (sourceIterator.hasNext()) {
            Object valueFromSource = sourceIterator.next();
            RecursiveValue<?> value;
            if (valueFromSource instanceof RecursiveValue) {
                value = (RecursiveValue<?>) valueFromSource;
            }
            else {
                value = recursiveValueProvider.get(valueFromSource);
            }
            if (value == null) {
                throw new RuntimeException("No RecursiveValue object provided for " + value + ". You may need to write a RecursiveValueProvider.");
            }
            addToStack(value, toStack, getClientId(String.valueOf(i)));
            i++;
        }
            
        iterator = toStack.iterator();
        recursiveValue = iterator.hasNext() ? iterator.next() : null;
        
    }

    private void addToStack(RecursiveValue<?> value, List<RecursiveValue<?>> stack, String id) {
        
        String parentId = StringUtils.substringAfter(childToParentMap.get(id), PLACEHOLDER_PREFIX);
        Integer itemDepth = parentId == null ? ZERO : idToDepthMap.get(parentId) + 1;
        
        if (depth == null || depth <= 0 || depth > itemDepth) {

            // avoiding having the same value rendered twice
            if (!stack.contains(value)) {
                stack.add(value);
                idToRecursiveValueMap.put(id, value);
                idToDepthMap.put(id,  itemDepth);
            }
            int i = 1;
            final List<RecursiveValue<?>> children = value.getChildren();
            if (children != null && !children.isEmpty()) {
                for (RecursiveValue<?> child : children) {
                    if (!ids.contains(id)) {
                        final String childId = id + "-" + i;
                        childToParentMap.put(childId, getPlaceholderClientId(id));
                        addToStack(child, stack, childId);
                        ids.add(childId);
                        i++;
                    }
                    else {
                        throw new RuntimeException("Two different objects with the same id: " + id);
                    }
                }
            }
        }
    }
    
    boolean beginRender(MarkupWriter writer) {
        final boolean continueRendering = recursiveValue != null;
        
        // Implemented this way so we don't have to rely on RecursiveValue implementations
        // to have a good hashCode() implementation.
        String id = findCurrentRecursiveValueId(recursiveValue);
        currentDepth = idToDepthMap.get(id) != null ? idToDepthMap.get(id) : 0;
        iterationWrappers.add(writer.element(ITERATION_WRAPPER_ELEMENT_NAME, "id", id));
        value = recursiveValue != null ? recursiveValue.getValue() : null;
        return continueRendering; 
    }

    private String findCurrentRecursiveValueId(RecursiveValue<?> recursiveValue) {
        String id = null;
        final Set<Entry<String, RecursiveValue<?>>> entrySet = idToRecursiveValueMap.entrySet();
        for (Entry<String, RecursiveValue<?>> entry : entrySet) {
            if (entry.getValue() == recursiveValue) {
                id = entry.getKey();
            }
        }
        return id;
    }
    
    boolean afterRender(MarkupWriter writer) {
        writer.end(); // iterationWrapper
        recursiveValue = iterator.hasNext() ? iterator.next() : null;
        return recursiveValue == null;
    }
    
    void cleanupRender(MarkupWriter writer) {
        writer.end(); // wrapper
        environment.pop(RecursiveContext.class);
        
        // place the elements inside the correct placeholders
        for (Element iterationWrapper : iterationWrappers) {
            final String id = iterationWrapper.getAttribute("id");
            final String parentId = childToParentMap.get(id);
            if (parentId != null) {
                Element placeholder = wrapper.getElementById(parentId);
                if (placeholder != null) {
                    // not using iterationWrapper.moveToBottom(placeholder) because of a bug on Tapestry 5.1.0.x
                    for (Node node : iterationWrapper.getChildren()) {
                        try {
                            node.moveToBottom(placeholder);
                        }
                        catch (IllegalArgumentException e) {
                            logger.error(e.getMessage() + " " + node + " " + placeholder);
                        }
                    }
                }
                // iterationWrapper.moveToBottom(placeholder);
                iterationWrapper.remove();
            }
            else {
                // not using iterationWrapper.pop() because of a bug on Tapestry 5.1.0.x.
                // important to iterate over the children in reverse order so that when we move them after the
                // iterationWrapper, they're in the same order they were in before the move
                List<Node> children = iterationWrapper.getChildren();
                for (int i = children.size(); i > 0; i --) {
                    children.get(i - 1).moveAfter(iterationWrapper);
                }
                iterationWrapper.remove();
            }
        }
        
        // remove the placeholders
        for (Element placeholder : placeholders) {
            placeholder.pop();
        }
        
        childToParentMap.clear();
        childToParentMap = null;
        placeholders.clear();
        placeholders = null;
        iterationWrappers.clear();
        iterationWrappers = null;
        wrapper.pop();
    }

    public String getClientId() {
        return clientId;
    }
    
    public String getClientId(String value) {
        return getClientId() + "-" + encode(value);
    }
    
    public String getPlaceholderClientId(String value) {
        return PLACEHOLDER_PREFIX + encode(value);
    }
    
    @SuppressWarnings("deprecation")
    final private String encode(String value) {
        // TODO: when Java 8 support is dropped, change line 
        // below to URLEncoder.encode(value, StandardCharsets.UTF_8);
        return URLEncoder.encode(value);
    }

    @Override
    public RecursiveValue<?> getCurrent() {
        return recursiveValue;
    }

    @Override
    public String getClientIdForCurrent() {
        return getPlaceholderClientId(findCurrentRecursiveValueId(getCurrent()));
    }

    @Override
    public void registerPlaceholder(Element element) {
        placeholders.add(element);
    }

}