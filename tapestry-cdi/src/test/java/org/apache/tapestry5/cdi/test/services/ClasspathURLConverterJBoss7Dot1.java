package org.apache.tapestry5.cdi.test.services;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

import org.apache.tapestry5.ioc.services.ClasspathURLConverter;

public class ClasspathURLConverterJBoss7Dot1 implements ClasspathURLConverter
{
    
    public URL convert(URL url)
    {
        // If the URL is a "vfs" URL (JBoss 7.1 uses a Virtual File System)...

        if (url != null && url.getProtocol().startsWith("vfs"))
        {
            // Ask the VFS what the physical URL is...

            try
            {
                String urlString = url.toString();
                        
                // If the virtual URL involves a JAR file, 
                // we have to figure out its physical URL ourselves because
                // in JBoss 7.1 the JAR files exploded into the VFS are empty 
                // (see https://issues.jboss.org/browse/JBAS-8786).
                // Our workaround is that they are available, unexploded, 
                // within the otherwise exploded WAR file.

                if (urlString.contains(".jar")) {
                                        
                    // An example URL: "vfs:/devel/jboss-7.1.0.Final/server/default/deploy/myapp.ear/myapp.war/WEB-INF/lib/tapestry-core-5.3.3.jar/org/apache/tapestry5/corelib/components/"
                    // Break the URL into its WAR part, the JAR part, 
                    // and the Java package part.
                                        
                    int warPartEnd = urlString.indexOf(".war") + 4;
                    String warPart = urlString.substring(0, warPartEnd);
                    int jarPartEnd = urlString.indexOf(".jar") + 4;
                    String jarPart = urlString.substring(warPartEnd, jarPartEnd);
                    String packagePart = urlString.substring(jarPartEnd);

                    // Ask the VFS where the exploded WAR is.

                    URL warURL = new URL(warPart);
                    URLConnection warConnection = warURL.openConnection();
                    Object jBossVirtualWarDir = warConnection.getContent();
                    // Use reflection so that we don't need JBoss in the classpath at compile time.
                    File physicalWarDir = (File) invokerGetter(jBossVirtualWarDir, "getPhysicalFile");
                    String physicalWarDirStr = physicalWarDir.toURI().toString();

                    // Return a "jar:" URL constructed from the parts
                    // eg. "jar:file:/devel/jboss-7.1.0.Final/server/default/tmp/vfs/automount40a6ed1db5eabeab/myapp.war-43e2c3dfa858f4d2//WEB-INF/lib/tapestry-core-5.3.3.jar!/org/apache/tapestry5/corelib/components/".

                    String actualJarPath = "jar:" + physicalWarDirStr + jarPart + "!" + packagePart;
                    return new URL(actualJarPath);
                }
                                
                // Otherwise, ask the VFS what the physical URL is...
                                
                else {

                    URLConnection connection = url.openConnection();
                    Object jBossVirtualFile = connection.getContent();
                    // Use reflection so that we don't need JBoss in the classpath at compile time.
                    File physicalFile = (File) invokerGetter(jBossVirtualFile, "getPhysicalFile");
                    URL physicalFileURL = physicalFile.toURI().toURL();
                    return physicalFileURL;
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return url;
    }

    private Object invokerGetter(Object target, String getter) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Class<?> type = target.getClass();
        Method method;
        try
        {
            method = type.getMethod(getter);
        }
        catch (NoSuchMethodException e)
        {
            method = type.getDeclaredMethod(getter);
            method.setAccessible(true);
        }
        return method.invoke(target);
    }

}
