package org.apache.tapestry5.test;

/**
 * @deprecated Please use {@link TomcatRunner} instead
 *
 */
@Deprecated
public class Tomcat6Runner extends TomcatRunner
{

    public Tomcat6Runner(String webappFolder, String contextPath, int port, int sslPort) throws Exception
    {
        super(webappFolder, contextPath, port, sslPort);
        System.out.printf("The %s class should no longer be used, please switch to %s", Tomcat6Runner.class.getName(), TomcatRunner.class.getName());
    }

}