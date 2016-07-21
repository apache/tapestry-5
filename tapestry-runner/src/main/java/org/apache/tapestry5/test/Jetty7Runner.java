package org.apache.tapestry5.test;

/**
 * @deprecated please use {@link JettyRunner} instead
 *
 */
@Deprecated
public class Jetty7Runner extends JettyRunner
{

    public Jetty7Runner() {
        super();
        System.out.printf("The %s class should no longer be used, please switch to %s", Jetty7Runner.class.getName(), JettyRunner.class.getName());
    }

    public Jetty7Runner(String webappFolder, String contextPath, int port, int sslPort) throws Exception {
        super(webappFolder, contextPath, port, sslPort);
        System.out.printf("The %s class should no longer be used, please switch to %s", Jetty7Runner.class.getName(), JettyRunner.class.getName());
    }

}
