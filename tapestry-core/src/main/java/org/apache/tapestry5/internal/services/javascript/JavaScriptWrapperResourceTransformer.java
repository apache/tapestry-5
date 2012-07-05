package org.apache.tapestry5.internal.services.javascript;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.assets.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contributed to to the {@link StreamableResourceSource} service, this  transformer converts files with the extension ".jsw" (JavaScript Wrapper) into
 * simple JavaScript. JavaScript Wrapper files consist of normal JavaScript, with a special {@code @include} directive.
 * The include directive must appear on a line by itself, and is of the form: {@code @include("relative-resource-path")} where the <em>relative-resource-path</em>
 * identifies another Resource to insert at that line.  The directive may be indented, and have whitespace around the before and after the parenthesis, but must
 * be one a single line.
 * <p/>
 * Currently, only resources in the same domain are supported (resource prefixes are not currently supported).
 * <p/>
 * The identified resource is obtained via the
 * StreamableResourceSource service(meaning that other resource transformations may occur).
 * <p/>
 * JavaScript Wrapper files are primarily used to adapt JavaScript libraries that are not compatible with the <a href="http://requirejs.org/docs/whyamd.html">AMD</a> format
 * (used by <a href="http://requirejs.org/">RequireJS</a>).
 * <p/>
 * JavaScript Wrapper files are expected to be encoded in utf-8.
 *
 * @since 5.4
 */
public class JavaScriptWrapperResourceTransformer implements ResourceTransformer
{
    private final StreamableResourceSource streamableResourceSource;

    private final Pattern includePattern = Pattern.compile("^\\s*@include\\s*\\(\\s*\"(.*?)\"\\s*\\)\\s*$");

    public JavaScriptWrapperResourceTransformer(StreamableResourceSource streamableResourceSource)
    {
        this.streamableResourceSource = streamableResourceSource;
    }

    public String getTransformedContentType()
    {
        return "text/javascript";
    }

    @Override
    public InputStream transform(Resource source, ResourceDependencies dependencies) throws IOException
    {
        InputStream is = source.openStream();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(bos, "utf-8"));

        try
        {
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(is, "utf-8"));

            while (true)
            {
                String line = reader.readLine();

                if (line == null)
                {
                    break;
                }

                Matcher matcher = includePattern.matcher(line);

                if (matcher.matches())
                {
                    String path = matcher.group(1);

                    // TODO: Checks for recursive includes!

                    Resource includedResource = source.forFile(path);

                    dependencies.addDependency(includedResource);

                    StreamableResource includedStreamable = streamableResourceSource.getStreamableResource(includedResource, StreamableResourceProcessing.FOR_AGGREGATION, dependencies);

                    pw.flush();

                    includedStreamable.streamTo(bos);

                } else
                {
                    pw.println(line);
                }
            }

            pw.close();
            reader.close();

        } finally
        {
            InternalUtils.close(is);
        }

        return new ByteArrayInputStream(bos.toByteArray());
    }
}
