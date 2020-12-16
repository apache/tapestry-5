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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.*;
import org.apache.tapestry5.beanmodel.PropertyConduit;
import org.apache.tapestry5.beanmodel.PropertyConduit2;
import org.apache.tapestry5.beanmodel.internal.InternalPropertyConduit;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.CommonsUtils;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.ioc.Orderable;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.LinkCreationListener;
import org.apache.tapestry5.services.LinkCreationListener2;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.javascript.StylesheetLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Shared utility methods used by various implementation classes.
 */
@SuppressWarnings("all")
public class TapestryInternalUtils
{
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^\\w]");

    private static final int BUFFER_SIZE = 5000;

    /**
     * Capitalizes the string, and inserts a space before each upper case character (or sequence of upper case
     * characters). Thus "userId" becomes "User Id", etc. Also, converts underscore into space (and capitalizes the
     * following word), thus "user_id" also becomes "User Id".
     */
    public static String toUserPresentable(String id)
    {
        return InternalUtils.toUserPresentable(id);
    }

    public static Map<String, String> mapFromKeysAndValues(String... keysAndValues)
    {
        Map<String, String> result = CollectionFactory.newMap();

        int i = 0;
        while (i < keysAndValues.length)
        {
            String key = keysAndValues[i++];
            String value = keysAndValues[i++];

            result.put(key, value);
        }

        return result;
    }

    /**
     * Converts a string to an {@link OptionModel}. The string is of the form "value=label". If the equals sign is
     * omitted, then the same value is used for both value and label.
     */
    public static OptionModel toOptionModel(String input)
    {
        assert input != null;
        int equalsx = input.indexOf('=');

        if (equalsx < 0)
            return new OptionModelImpl(input);

        String value = input.substring(0, equalsx);
        String label = input.substring(equalsx + 1);

        return new OptionModelImpl(label, value);
    }

    /**
     * Parses a string input into a series of value=label pairs compatible with {@link #toOptionModel(String)}. Splits
     * on commas. Ignores whitespace around commas.
     *
     * @param input comma seperated list of terms
     * @return list of option models
     */
    public static List<OptionModel> toOptionModels(String input)
    {
        assert input != null;
        List<OptionModel> result = CollectionFactory.newList();

        for (String term : input.split(","))
            result.add(toOptionModel(term.trim()));

        return result;
    }

    /**
     * Wraps the result of {@link #toOptionModels(String)} as a {@link SelectModel} (with no option groups).
     *
     * See TAP5-2184 for why this ends up causing some trouble!
     */
    public static SelectModel toSelectModel(String input)
    {
        List<OptionModel> options = toOptionModels(input);

        return new SelectModelImpl(null, options);
    }

    /**
     * Converts a map entry to an {@link OptionModel}.
     */
    public static OptionModel toOptionModel(Map.Entry input)
    {
        assert input != null;
        String label = input.getValue() != null ? String.valueOf(input.getValue()) : "";

        return new OptionModelImpl(label, input.getKey());
    }

    /**
     * Processes a map input into a series of map entries compatible with {@link #toOptionModel(Map.Entry)}.
     *
     * @param input map of elements
     * @return list of option models
     */
    public static <K, V> List<OptionModel> toOptionModels(Map<K, V> input)
    {
        assert input != null;
        List<OptionModel> result = CollectionFactory.newList();

        for (Map.Entry entry : input.entrySet())
            result.add(toOptionModel(entry));

        return result;
    }

    /**
     * Wraps the result of {@link #toOptionModels(Map)} as a {@link SelectModel} (with no option groups).
     */
    public static <K, V> SelectModel toSelectModel(Map<K, V> input)
    {
        List<OptionModel> options = toOptionModels(input);

        return new SelectModelImpl(null, options);
    }

    /**
     * Converts an object to an {@link OptionModel}.
     */
    public static OptionModel toOptionModel(Object input)
    {
        String label = (input != null ? String.valueOf(input) : "");

        return new OptionModelImpl(label, input);
    }

    /**
     * Processes a list input into a series of objects compatible with {@link #toOptionModel(Object)}.
     *
     * @param input list of elements
     * @return list of option models
     */
    public static <E> List<OptionModel> toOptionModels(List<E> input)
    {
        assert input != null;
        List<OptionModel> result = CollectionFactory.newList();

        for (E element : input)
            result.add(toOptionModel(element));

        return result;
    }

    /**
     * Wraps the result of {@link #toOptionModels(List)} as a {@link SelectModel} (with no option groups).
     */
    public static <E> SelectModel toSelectModel(List<E> input)
    {
        List<OptionModel> options = toOptionModels(input);

        return new SelectModelImpl(null, options);
    }

    /**
     * Parses a key/value pair where the key and the value are seperated by an equals sign. The key and value are
     * trimmed of leading and trailing whitespace, and returned as a {@link KeyValue}.
     */
    public static KeyValue parseKeyValue(String input)
    {
        int pos = input.indexOf('=');

        if (pos < 1)
            throw new IllegalArgumentException(String.format("Key/value pair '%s' is not properly formatted (it does not contain an equals sign).", input));

        String key = input.substring(0, pos);
        String value = input.substring(pos + 1);

        return new KeyValue(key.trim(), value.trim());
    }

    /**
     * Used to convert a property expression into a key that can be used to locate various resources (Blocks, messages,
     * etc.). Strips out any punctuation characters, leaving just words characters (letters, number and the
     * underscore).
     *
     * @param expression a property expression
     * @return the expression with punctuation removed
     */
    public static String extractIdFromPropertyExpression(String expression)
    {
        return InternalUtils.extractIdFromPropertyExpression(expression);
    }

    /**
     * Looks for a label within the messages based on the id. If found, it is used, otherwise the name is converted to a
     * user presentable form.
     */
    public static String defaultLabel(String id, Messages messages, String propertyExpression)
    {
        return InternalUtils.defaultLabel(id, messages, propertyExpression);
    }

    /**
     * Strips a dotted sequence (such as a property expression, or a qualified class name) down to the last term of that
     * expression, by locating the last period ('.') in the string.
     */
    public static String lastTerm(String input)
    {
        int dotx = input.lastIndexOf('.');

        return input.substring(dotx + 1);
    }

    /**
     * Converts an list of strings into a space-separated string combining them all, suitable for use as an HTML class
     * attribute value.
     *
     * @param classes classes to combine
     * @return the joined classes, or null if classes is empty
     */
    public static String toClassAttributeValue(List<String> classes)
    {
        if (classes.isEmpty())
            return null;

        return InternalUtils.join(classes, " ");
    }

    /**
     * Converts an enum to a label string, allowing for overrides from a message catalog.
     *
     * <ul>
     * <li>As key <em>prefix</em>.<em>name</em> if present. Ex: "ElementType.LOCAL_VARIABLE"
     * <li>As key <em>name</em> if present, i.e., "LOCAL_VARIABLE".
     * <li>As a user-presentable version of the name, i.e., "Local Variable".
     * </ul>
     *
     * @param messages the messages to search for the label
     * @param prefix   prepended to key
     * @param value    to get a label for
     * @return the label
     */
    public static String getLabelForEnum(Messages messages, String prefix, Enum value)
    {
        String name = value.name();

        String key = prefix + "." + name;

        if (messages.contains(key))
            return messages.get(key);

        if (messages.contains(name))
            return messages.get(name);

        return toUserPresentable(name.toLowerCase());
    }

    public static String getLabelForEnum(Messages messages, Enum value)
    {
        String prefix = lastTerm(value.getClass().getName());

        return getLabelForEnum(messages, prefix, value);
    }

    private static String replace(String input, Pattern pattern, String replacement)
    {
        return InternalUtils.replace(input, pattern, replacement);
    }

    /**
     * Determines if the two values are equal. They are equal if they are the exact same value (including if they are
     * both null). Otherwise standard equals() comparison is used.
     *
     * @param left  value to compare, possibly null
     * @param right value to compare, possibly null
     * @return true if same value, both null, or equal
     */
    public static <T> boolean isEqual(T left, T right)
    {
        if (left == right)
            return true;

        if (left == null)
            return false;

        return left.equals(right);
    }

    /**
     * Splits a path at each slash.
     */
    public static String[] splitPath(String path)
    {
        return CommonsUtils.splitPath(path);
    }

    /**
     * Splits a value around commas. Whitespace around the commas is removed, as is leading and trailing whitespace.
     *
     * @since 5.1.0.0
     */
    public static String[] splitAtCommas(String value)
    {
        return CommonsUtils.splitAtCommas(value);
    }

    /**
     * Copies some content from an input stream to an output stream. It is the caller's responsibility to close the
     * streams.
     *
     * @param in  source of data
     * @param out sink of data
     * @throws IOException
     * @since 5.1.0.0
     */
    public static void copy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];

        while (true)
        {
            int length = in.read(buffer);

            if (length < 0)
                break;

            out.write(buffer, 0, length);
        }

        // TAPESTRY-2415: WebLogic needs this flush() call.
        out.flush();
    }

    public static boolean isEqual(EventContext left, EventContext right)
    {
        if (left == right)
            return true;

        int count = left.getCount();

        if (count != right.getCount())
            return false;

        for (int i = 0; i < count; i++)
        {
            if (!left.get(Object.class, i).equals(right.get(Object.class, i)))
                return false;
        }

        return true;
    }

    public static InternalPropertyConduit toInternalPropertyConduit(final PropertyConduit conduit)
    {
        if (conduit instanceof InternalPropertyConduit)
            return (InternalPropertyConduit) conduit;

        return new InternalPropertyConduit()
        {

            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return conduit.getAnnotation(annotationClass);
            }

            public void set(Object instance, Object value)
            {
                conduit.set(instance, value);
            }

            public Class getPropertyType()
            {
                return conduit.getPropertyType();
            }

            public Type getPropertyGenericType()
            {
                if (conduit instanceof PropertyConduit2)
                {
                    return ((PropertyConduit2) conduit).getPropertyGenericType();
                }
                return conduit.getPropertyType();
            }
            
            public Object get(Object instance)
            {
                return conduit.get(instance);
            }

            public String getPropertyName()
            {
                return null;
            }
        };
    }

    /**
     * @param mixinDef the original mixin definition.
     * @return an Orderable whose id is the mixin name.
     */
    public static Orderable<String> mixinTypeAndOrder(String mixinDef)
    {
        int idx = mixinDef.indexOf("::");
        if (idx == -1)
        {
            return new Orderable<String>(mixinDef, mixinDef);
        }
        String type = mixinDef.substring(0, idx);
        String[] constraints = splitMixinConstraints(mixinDef.substring(idx + 2));

        return new Orderable<String>(type, type, constraints);
    }

    public static String[] splitMixinConstraints(String s)
    {
        return InternalUtils.isBlank(s) ? null : s.split(";");
    }

    /**
     * Common mapper, used primarily with {@link org.apache.tapestry5.func.Flow#map(org.apache.tapestry5.func.Mapper)}
     *
     * @since 5.2.0
     */
    public static Mapper<Asset, StylesheetLink> assetToStylesheetLink = new Mapper<Asset, StylesheetLink>()
    {
        public StylesheetLink map(Asset input)
        {
            return new StylesheetLink(input);
        }
    };

    public static LinkCreationListener2 toLinkCreationListener2(final LinkCreationListener delegate)
    {
        return new LinkCreationListener2()
        {

            public void createdPageRenderLink(Link link, PageRenderRequestParameters parameters)
            {
                delegate.createdPageRenderLink(link);
            }

            public void createdComponentEventLink(Link link, ComponentEventRequestParameters parameters)
            {
                delegate.createdComponentEventLink(link);
            }
        };
    }

    /**
     * @since 5.3
     */
    public static String toFileSuffix(String fileName)
    {
        int dotx = fileName.lastIndexOf('.');

        return dotx < 0 ? "" : fileName.substring(dotx + 1);
    }

    /**
     * Extracts a value from a  map of references. Handles the case where the reference does not exist,
     * and the case where the reference itself now contains null.
     *
     * @since 5.3
     */
    public static <K, V> V getAndDeref(Map<K, ? extends Reference<V>> map, K key)
    {
        Reference<V> ref = map.get(key);

        return ref == null ? null : ref.get();
    }

    /**
     * Gathers together an array containing all the threads.
     * @since 5.4 */
    public static Thread[] getAllThreads() {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();

        while (true) {
            ThreadGroup parentGroup = rootGroup.getParent();
            if (parentGroup == null) {
                break;
            }
            rootGroup = parentGroup;
        }

        Thread[] threads = new Thread[rootGroup.activeCount()];

        while (true) {
            // A really ugly API. threads.length must be larger than
            // the actual number of threads, just so we can determine
            // if we're done.
            int count = rootGroup.enumerate(threads, true);
            if (count < threads.length) {
                return Arrays.copyOf(threads, count);
            }
            threads = new Thread[threads.length * 2];
        }
    }
}

