package org.apache.tapestry5.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated please use {@link JettyRunner} instead
 *
 */
@Deprecated
public class Jetty7Runner extends JettyRunner
{

    private final static Logger logger = LoggerFactory.getLogger(Jetty7Runner.class);

    public Jetty7Runner() {
        super();
        logger.warn("The {} class should no longer be used, please switch to {}", Jetty7Runner.class.getName(), JettyRunner.class.getName());
    }

    public Jetty7Runner(String webappFolder, String contextPath, int port, int sslPort) throws Exception {
        super(webappFolder, contextPath, port, sslPort);
        logger.warn("The {} class should no longer be used, please switch to {}", Jetty7Runner.class.getName(), JettyRunner.class.getName());
    }

}
