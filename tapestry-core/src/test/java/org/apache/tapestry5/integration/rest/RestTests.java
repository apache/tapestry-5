// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.integration.app1.App1TestCase;
import org.apache.tapestry5.integration.app1.pages.rest.RestRequestNotHandledDemo;
import org.apache.tapestry5.integration.app1.pages.rest.RestWithEventHandlerMethodNameDemo;
import org.apache.tapestry5.integration.app1.pages.rest.RestWithOnEventDemo;
import org.testng.annotations.Test;

/**
 * Tests REST-related stuff.
 */
public class RestTests extends App1TestCase
{
    final private static String POST_CONTENT = "órgão and ôthèr words with äccents";
    
    final private static String ON_EVENT_ENDPOINT_URL = RestWithOnEventDemo.class.getSimpleName();
    
    final private static String METHOD_NAME_ENDPOINT_URL = RestWithEventHandlerMethodNameDemo.class.getSimpleName();
    
    final private static String PATH_PARAMETER_VALUE = "somethingNice";
    
    @Test
    public void on_event_http_get() throws IOException
    {
        test(EventConstants.HTTP_GET, new HttpGet(getUrl(ON_EVENT_ENDPOINT_URL)));
    }
    
    @Test
    public void on_event_http_post() throws IOException
    {
        test(EventConstants.HTTP_POST, new HttpPost(getUrl(ON_EVENT_ENDPOINT_URL)));
    }

    @Test
    public void on_event_http_put() throws IOException
    {
        test(EventConstants.HTTP_PUT, new HttpPut(getUrl(ON_EVENT_ENDPOINT_URL)));
    }

    @Test(enabled = false)
    public void on_event_http_put_without_other_parameters() throws IOException
    {
        test(EventConstants.HTTP_PUT, new HttpPut(getBaseURL() + "/rest/" + ON_EVENT_ENDPOINT_URL));
    }

    @Test
    public void on_event_http_delete() throws IOException
    {
        test(EventConstants.HTTP_DELETE, new HttpDelete(getUrl(ON_EVENT_ENDPOINT_URL)));
    }

    @Test
    public void on_event_http_patch() throws IOException
    {
        test(EventConstants.HTTP_PATCH, new HttpPatch(getUrl(ON_EVENT_ENDPOINT_URL)));
    }

    @Test
    public void on_event_http_head() throws IOException
    {
        test(EventConstants.HTTP_HEAD, new HttpHead(getUrl(ON_EVENT_ENDPOINT_URL)));
    }
    
    @Test
    public void on_http_get() throws IOException
    {
        test(EventConstants.HTTP_GET, new HttpGet(getUrl(METHOD_NAME_ENDPOINT_URL)));
    }
    
    @Test
    public void on_http_post() throws IOException
    {
        test(EventConstants.HTTP_POST, new HttpPost(getUrl(METHOD_NAME_ENDPOINT_URL)));
    }

    @Test
    public void on_http_put() throws IOException
    {
        test(EventConstants.HTTP_PUT, new HttpPut(getUrl(METHOD_NAME_ENDPOINT_URL)));
    }

    @Test
    public void on_http_delete() throws IOException
    {
        test(EventConstants.HTTP_DELETE, new HttpDelete(getUrl(METHOD_NAME_ENDPOINT_URL)));
    }

    @Test
    public void on_http_patch() throws IOException
    {
        test(EventConstants.HTTP_PATCH, new HttpPatch(getUrl(METHOD_NAME_ENDPOINT_URL)));
    }

    @Test
    public void on_http_head() throws IOException
    {
        test(EventConstants.HTTP_HEAD, new HttpHead(getUrl(METHOD_NAME_ENDPOINT_URL)));
    }

    @Test
    public void no_matching_rest_event_handler() throws IOException
    {
        final String url = getBaseURL() + "/rest/" + RestRequestNotHandledDemo.class.getSimpleName();
        try (final CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpHead httpHead = new HttpHead(url);
            try (CloseableHttpResponse response = httpClient.execute(httpHead))
            {
                assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    @Test
    public void returning_http_status() throws IOException
    {
        final String url = getBaseURL() + "/rest/" + RestWithOnEventDemo.class.getSimpleName() + "/returningHttpStatus";
        try (final CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(new StringEntity(PATH_PARAMETER_VALUE, "UTF-8"));
            try (CloseableHttpResponse response = httpClient.execute(httpPut))
            {
                final StatusLine statusLine = response.getStatusLine();
                assertEquals(statusLine.getStatusCode(), HttpServletResponse.SC_CREATED);
                assertEquals(IOUtils.toString(response.getEntity().getContent()), PATH_PARAMETER_VALUE);
                assertEquals(response.getHeaders("Content-Location").length, 1);
                assertEquals(response.getHeaders("ETag").length, 1);
                assertEquals(response.getFirstHeader("Content-Location").getValue(), PATH_PARAMETER_VALUE + ".txt");
                assertEquals(response.getFirstHeader("ETag").getValue(), PATH_PARAMETER_VALUE + ".etag");
            }
        }
    }
    
    @Test
    public void returning_http_status_part_2() throws IOException
    {
        final String url = getBaseURL() + "/rest/" + RestWithOnEventDemo.class.getSimpleName() + "/returningHttpStatusSimple";
        try (final CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(new StringEntity(PATH_PARAMETER_VALUE, "UTF-8"));
            try (CloseableHttpResponse response = httpClient.execute(httpPut))
            {
                final StatusLine statusLine = response.getStatusLine();
                assertEquals(statusLine.getStatusCode(), HttpServletResponse.SC_CREATED);
                assertEquals(IOUtils.toString(response.getEntity().getContent()), "");
                assertEquals(response.getAllHeaders().length, 2); // content-length, server
            }
        }
    }

    @Test
    public void asset_requested_with_head() throws IOException
    {
        openLinks("AssetDemo");
        String url = getBaseURL() + getText("assetUrl");
        try (final CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            // Copied directly from AssetWithWrongChecksum.js, which shouldn't change anyway
            final String assetContents = "document.getElementById('assetWithWrongChecksum').style.display = 'block';";
            
            final HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse httpGetResponse = httpClient.execute(httpGet))
            {
                assertEquals(httpGetResponse.getEntity().getContentLength(), assetContents.length());
                assertEquals(IOUtils.toString(httpGetResponse.getEntity().getContent()), assetContents);
            }

            final HttpHead httpHead = new HttpHead(url);
            try (CloseableHttpResponse httpHeadResponse = httpClient.execute(httpHead))
            {
                assertNull(httpHeadResponse.getEntity());
                assertEquals(httpHeadResponse.getFirstHeader("Content-Length").getValue(), "0");
            }
            
        }
        
    }
    
    private void test(String eventName, HttpRequestBase method) throws ClientProtocolException, IOException
    {
        String expectedResponse = eventName + ":" + PATH_PARAMETER_VALUE;
        if (method instanceof HttpEntityEnclosingRequest)
        {
            HttpEntityEnclosingRequest heer = (HttpEntityEnclosingRequest) method;
            heer.setEntity(new StringEntity(POST_CONTENT, "UTF-8"));
            expectedResponse = expectedResponse + ":" + POST_CONTENT;
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(method))
        {
            assertEquals(response.getStatusLine().getStatusCode(), 200);
            HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                assertEquals(IOUtils.toString(entity.getContent()), expectedResponse);
            }
            else
            {
                assertEquals(eventName, EventConstants.HTTP_HEAD);
            }
            Header[] headers = response.getHeaders(RestWithOnEventDemo.EXTRA_HTTP_HEADER);
            assertEquals(headers.length, 1);
            assertEquals(headers[0].getValue(), eventName);
        }
    }

    private String getUrl(String page) {
        return getBaseURL() + "/rest/" + page + "/" + 
                RestWithOnEventDemo.SUBPATH + "/" + PATH_PARAMETER_VALUE;
    }

}
