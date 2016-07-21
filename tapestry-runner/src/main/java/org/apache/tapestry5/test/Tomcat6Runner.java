package org.apache.tapestry5.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Please use {@link TomcatRunner} instead
 *
 */
@Deprecated
public class Tomcat6Runner extends TomcatRunner
{

    private final static Logger logger = LoggerFactory.getLogger(Tomcat6Runner.class);

    public Tomcat6Runner(String webappFolder, String contextPath, int port, int sslPort) throws Exception
    {
        super(webappFolder, contextPath, port, sslPort);
        logger.warn("The {} class should no longer be used, please switch to {}", Tomcat6Runner.class.getName(), TomcatRunner.class.getName());
    }

}