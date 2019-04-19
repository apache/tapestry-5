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

package org.apache.tapestry5.internal.plastic;

import org.apache.tapestry5.internal.plastic.asm.*;
import org.apache.tapestry5.internal.plastic.asm.commons.JSRInlinerAdapter;
import org.apache.tapestry5.internal.plastic.asm.tree.ClassNode;
import org.apache.tapestry5.internal.plastic.asm.tree.MethodNode;
import org.apache.tapestry5.internal.plastic.asm.util.TraceClassVisitor;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.MethodDescription;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
public class PlasticInternalUtils
{
    public static final String[] EMPTY = new String[0];

    public static boolean isEmpty(Object[] input)
    {
        return input == null || input.length == 0;
    }

    public static String[] orEmpty(String[] input)
    {
        return input == null ? EMPTY : input;
    }

    public static boolean isBlank(String input)
    {
        return input == null || input.length() == 0 || input.trim().length() == 0;
    }

    public static boolean isNonBlank(String input)
    {
        return !isBlank(input);
    }

    public static String toInternalName(String className)
    {
        assert isNonBlank(className);

        return className.replace('.', '/');
    }

    public static String toClassPath(String className)
    {
        return toInternalName(className) + ".class";
    }

    public static String toMessage(Throwable t)
    {
        String message = t.getMessage();

        return isBlank(message) ? t.getClass().getName() : message;
    }

    public static void close(Closeable closeable)
    {
        try
        {
            if (closeable != null)
                closeable.close();
        } catch (IOException ex)
        {
            // Ignore it.
        }
    }

    @SuppressWarnings("unchecked")
    public static MethodDescription toMethodDescription(MethodNode node)
    {
        String returnType = Type.getReturnType(node.desc).getClassName();

        String[] arguments = toClassNames(Type.getArgumentTypes(node.desc));

        List<String> exceptions = node.exceptions;

        String[] exceptionClassNames = new String[exceptions.size()];

        for (int i = 0; i < exceptionClassNames.length; i++)
        {
            exceptionClassNames[i] = exceptions.get(i).replace('/', '.');
        }

        return new MethodDescription(node.access, returnType, node.name, arguments, node.signature, exceptionClassNames);
    }

    private static String[] toClassNames(Type[] types)
    {
        if (isEmpty(types))
            return EMPTY;

        String[] result = new String[types.length];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = types[i].getClassName();
        }

        return result;
    }

    /**
     * Converts a class's internal name (i.e., using slashes)
     * to Java source code format (i.e., using periods).
     */
    public static String toClassName(String internalName)
    {
        assert isNonBlank(internalName);

        return internalName.replace('/', '.');
    }

    /**
     * Converts a primitive type or fully qualified class name (or array form) to
     * a descriptor.
     * <ul>
     * <li>boolean --&gt; Z
     * <li>
     * <li>java.lang.Integer --&gt; Ljava/lang/Integer;</li>
     * <li>char[] --&gt; [C</li>
     * <li>java.lang.String[][] --&gt; [[java/lang/String;
     * </ul>
     */
    public static String toDescriptor(String className)
    {
        String buffer = className;
        int arrayDepth = 0;

        while (buffer.endsWith("[]"))
        {
            arrayDepth++;
            buffer = buffer.substring(0, buffer.length() - 2);
        }

        // Get the description of the base element type, then figure out if and
        // how to identify it as an array type.

        PrimitiveType type = PrimitiveType.getByName(buffer);

        String baseDesc = type == null ? "L" + buffer.replace('.', '/') + ";" : type.descriptor;

        if (arrayDepth == 0)
            return baseDesc;

        StringBuilder b = new StringBuilder();

        for (int i = 0; i < arrayDepth; i++)
        {
            b.append('[');
        }

        b.append(baseDesc);

        return b.toString();
    }

    private static final Pattern DESC = Pattern.compile("^L(.*);$");

    /**
     * Converts an object type descriptor (i.e. "Ljava/lang/Object;") to a class name
     * ("java.lang.Object").
     */
    public static String objectDescriptorToClassName(String descriptor)
    {
        assert descriptor != null;

        Matcher matcher = DESC.matcher(descriptor);

        if (!matcher.matches())
            throw new IllegalArgumentException(String.format("Input '%s' is not an object descriptor.", descriptor));

        return toClassName(matcher.group(1));
    }

    public static <K, V> Map<K, V> newMap()
    {
        return new HashMap<K, V>();
    }

    public static <K, V> ConcurrentMap<K, V> newConcurrentMap()
    {
        return new ConcurrentHashMap<K, V>();
    }

    public static <T> Set<T> newSet()
    {
        return new HashSet<T>();
    }

    public static <T> List<T> newList()
    {
        return new ArrayList<T>();
    }

    public static String dissasembleBytecode(ClassNode classNode)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        TraceClassVisitor visitor = new TraceClassVisitor(writer);

        classNode.accept(visitor);

        writer.close();

        return stringWriter.toString();
    }

    private static final Pattern PROPERTY_PATTERN = Pattern.compile("^(m?_+)?(.+?)_*$", Pattern.CASE_INSENSITIVE);

    /**
     * Strips out leading and trailing underscores, leaving the real property name.
     * In addition, "m_foo" is converted to "foo".
     *
     * @param fieldName to convert
     * @return the property name
     */
    public static String toPropertyName(String fieldName)
    {
        Matcher matcher = PROPERTY_PATTERN.matcher(fieldName);

        if (!matcher.matches())
            throw new IllegalArgumentException(String.format(
                    "Field name '%s' can not be converted to a property name.", fieldName));

        return matcher.group(2);
    }

    /**
     * Capitalizes the input string, converting the first character to upper case.
     *
     * @param input a non-empty string
     * @return the same string if already capitalized, or a capitalized version
     */
    public static String capitalize(String input)
    {
        char first = input.charAt(0);

        if (Character.isUpperCase(first))
            return input;

        return String.valueOf(Character.toUpperCase(first)) + input.substring(1);
    }

    private static final Map<String, Class> PRIMITIVES = new HashMap<String, Class>();

    static
    {
        PRIMITIVES.put("boolean", boolean.class);
        PRIMITIVES.put("char", char.class);
        PRIMITIVES.put("byte", byte.class);
        PRIMITIVES.put("short", short.class);
        PRIMITIVES.put("int", int.class);
        PRIMITIVES.put("long", long.class);
        PRIMITIVES.put("float", float.class);
        PRIMITIVES.put("double", double.class);
        PRIMITIVES.put("void", void.class);
    }

    /**
     * @param loader   class loader to look up in
     * @param javaName java name is Java source format (e.g., "int", "int[]", "java.lang.String", "java.lang.String[]", etc.)
     * @return class instance
     * @throws ClassNotFoundException
     */
    public static Class toClass(ClassLoader loader, String javaName) throws ClassNotFoundException
    {
        int depth = 0;

        while (javaName.endsWith("[]"))
        {
            depth++;
            javaName = javaName.substring(0, javaName.length() - 2);
        }

        Class primitive = PRIMITIVES.get(javaName);

        if (primitive != null)
        {
            Class result = primitive;
            for (int i = 0; i < depth; i++)
            {
                result = Array.newInstance(result, 0).getClass();
            }

            return result;
        }

        if (depth == 0)
            return Class.forName(javaName, true, loader);

        StringBuilder builder = new StringBuilder(20);

        for (int i = 0; i < depth; i++)
        {
            builder.append('[');
        }

        builder.append('L').append(javaName).append(';');

        return Class.forName(builder.toString(), true, loader);
    }

    public static Object getFromInstanceContext(InstanceContext context, String javaName)
    {
        ClassLoader loader = context.getInstanceType().getClassLoader();

        try
        {
            Class valueType = toClass(loader, javaName);

            return context.get(valueType);
        } catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns true if both objects are the same instance, or both null, or left equals right.
     */
    public static boolean isEqual(Object left, Object right)
    {
        return left == right || (left != null && left.equals(right));
    }

    static byte[] readBytestream(InputStream stream) throws IOException
    {
        byte[] buffer = new byte[5000];

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while (true)
        {
            int length = stream.read(buffer);

            if (length < 0)
                break;

            bos.write(buffer, 0, length);
        }

        bos.close();

        return bos.toByteArray();
    }

    public static byte[] readBytecodeForClass(ClassLoader loader, String className, boolean mustExist)
    {
        String path = toClassPath(className);
        InputStream stream = null;

        try
        {
            stream = getStreamForPath(loader, path);

            if (stream == null)
            {
                if (mustExist)
                    throw new RuntimeException(String.format("Unable to locate class file for '%s' in class loader %s.",
                            className, loader));

                return null;
            }

            return readBytestream(stream);
        } catch (IOException ex)
        {
            throw new RuntimeException(String.format("Failure reading bytecode for class %s: %s", className,
                    toMessage(ex)), ex);
        } finally
        {
            close(stream);
        }
    }

    static InputStream getStreamForPath(ClassLoader loader, String path) throws IOException
    {
        URL url = loader.getResource(path);

        if (url == null)
        {
            return null;
        }

        // This *should* handle Tomcat better, where the Tomcat class loader appears to be caching
        // the contents of files; this bypasses Tomcat to re-read the files from the disk directly.

        if (url.getProtocol().equals("file"))
        {
            try {
                return new FileInputStream(new File(url.toURI()));
            } catch (URISyntaxException e)
            {
                return null;
            }
        }

        return url.openStream();
    }

    public static ClassNode convertBytecodeToClassNode(byte[] bytecode)
    {
        ClassReader cr = new ClassReader(bytecode);

        ClassNode result = new ClassNode();

        ClassVisitor adapter = new ClassVisitor(Opcodes.ASM7, result)
        {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
            {
                MethodVisitor delegate = super.visitMethod(access, name, desc, signature, exceptions);

                return new JSRInlinerAdapter(delegate, access, name, desc, signature, exceptions);
            }
        };

        cr.accept(adapter, 0);

        return result;
    }
}
