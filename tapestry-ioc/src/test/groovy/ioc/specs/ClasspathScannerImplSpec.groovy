package ioc.specs

import java.io.File;
import java.net.URL;

import org.apache.tapestry5.ioc.Locatable;
import org.apache.tapestry5.ioc.internal.services.ClasspathScannerImpl;
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClasspathMatcher;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;

import spock.lang.Specification;

class ClasspathScannerImplSpec extends Specification {
  
    // TAP5-2096
    def "can locate classes inside a subpackage, inside an extracted JAR file"() {
        setup:

        ClasspathMatcher matchAll = new ClasspathMatcher(){
          boolean matches(String arg0, String arg1) {
            true
          };
        }
        def scannerJob = new ClasspathScannerImpl.Job(matchAll, Thread.currentThread().getContextClassLoader(), new ClasspathURLConverterImpl())
        
        when:
        URL url = Locatable.class.getResource('Locatable.class');
        scannerJob.scanDir("org/apache/tapestry5/ioc/", new File(url.getPath()).parentFile)
        while (!scannerJob.queue.isEmpty())
        {
            def queued = scannerJob.queue.pop();

            queued.run();
        }
        def classes = scannerJob.matches
        
        then:
        classes.contains(InternalUtils.class.getName().replaceAll(/\./, "/")+'.class')
    }
    
  
}
