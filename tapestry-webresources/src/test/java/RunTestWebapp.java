import org.apache.tapestry5.test.JettyRunner;

public class RunTestWebapp {

    public static void main(String[] args) throws Exception {
        new JettyRunner("src/test/webapp", "/", 8080, 8081).start();
    }

}
