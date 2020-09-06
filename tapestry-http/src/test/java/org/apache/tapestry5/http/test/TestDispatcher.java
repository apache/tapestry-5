package org.apache.tapestry5.http.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;

public class TestDispatcher implements Dispatcher {

    final private BaseURLSource baseUrlSource;
    
    public TestDispatcher(BaseURLSource baseUrlSouce) {
        super();
        this.baseUrlSource = baseUrlSouce;
    }

    @Override
    public boolean dispatch(Request request, Response response) throws IOException {
        
        boolean dispatched = request.getPath().startsWith("/hello");
        if (dispatched) 
        {
            try (OutputStream outputStream = response.getOutputStream("text/html");
                    PrintWriter writer = new PrintWriter(outputStream);)
            {
                writer.append(String.format(
                        "<html><body>Hello, world! <a href='%s'>Return to base URL</a></body></html>",
                        baseUrlSource.getBaseURL(request.isSecure())));
            }
        }
        return dispatched;
    }

}
