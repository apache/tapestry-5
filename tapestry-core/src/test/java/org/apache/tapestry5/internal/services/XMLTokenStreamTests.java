package org.apache.tapestry5.internal.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.internal.services.XMLTokenStream;
import org.apache.tapestry5.internal.services.XMLTokenType;
import org.apache.tapestry5.ioc.internal.util.AbstractResource;
import org.testng.Assert;
import org.testng.annotations.Test;


public class XMLTokenStreamTests
{
    private static class MappedConfigurationStub<K,V> extends HashMap<K, V> implements MappedConfiguration<K, V>
    {
        public void add(K key, V value)
        {
            put(key,value);
        }
        public void override(K key, V value)
        {
            put(key,value);
        }
        public void addInstance(K key, Class<? extends V> clazz)
        {
            throw new UnsupportedOperationException();
        }
        public void overrideInstance(K key, Class<? extends V> clazz)
        {
            throw new UnsupportedOperationException();
        }
    }


    
    private static class ResourceStub extends AbstractResource
    {
        final byte[] content;
        public ResourceStub(byte[] content)
        {
            super("");
            this.content=content;
        }
        public URL toURL()
        {
            return null;
        }
        protected Resource newResource(String path)
        {
            return null;
        }
        @Override
        public InputStream openStream() throws IOException
        {
            return new ByteArrayInputStream(content);
        }
    }

    protected void resetDefaultCharset() throws Exception
    {
        // The charset is cached after system start - so we ned to clear that cache to force a certain file.encoding
        Field field=Charset.class.getDeclaredField("defaultCharset");
        field.setAccessible(true);
        field.set(null, null);
    }
    
    /**
     * This test sets the system's default encoding to cp1252 (as on german windows) and tries to parse a non-ascii xml file
     * with XMLTokenStream. This is to test a critical section within XMLTokenStream.openStream() where the
     * binary file is converted to charcters and back before it is parsed. 
     * @throws Exception
     */
    @Test
    public void testStreamEncoding() throws Exception
    {
        String oldEncoding=System.getProperty("file.encoding");
        System.setProperty("file.encoding","cp1252");
        resetDefaultCharset();
        try
        {
            MappedConfigurationStub<String, URL> parserUrlMap=new MappedConfigurationStub<String, URL>();
            
            String unicodeString="\u00FC";
            
            String testDocument="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<root>"+unicodeString+"</root>\n";
            XMLTokenStream xts=new XMLTokenStream(new ResourceStub(testDocument.getBytes("utf-8")),parserUrlMap);
            
            // Ugly way to handle exceptions like java.io.IOException: Server returned HTTP response code: 503 for URL: http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent
            // when running tests
            int attempts = 0;
            int maxAttempts = 10;
            boolean success = false;
            while (!success && attempts < maxAttempts)
            {
                try 
                {
                    xts.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.sleep(5000);
                    attempts++;
                    continue;
                }
                success = true;
            }
            if (attempts == maxAttempts) 
            {
                throw new RuntimeException("Maximum number of attempts reached");
            }
            Assert.assertEquals(xts.next(), XMLTokenType.START_ELEMENT);
            Assert.assertEquals(xts.getLocalName(), "root");
            Assert.assertEquals(xts.next(), XMLTokenType.CHARACTERS);
            Assert.assertEquals(xts.getText(), unicodeString);
            Assert.assertEquals(xts.next(), XMLTokenType.END_ELEMENT);
            Assert.assertEquals(xts.next(), XMLTokenType.END_DOCUMENT);
        }
        finally
        {
            System.setProperty("file.encoding", oldEncoding);
            resetDefaultCharset();
        }
    }

}
