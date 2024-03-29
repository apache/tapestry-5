// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.integration.app1.data.Track;
import org.apache.tapestry5.integration.app1.pages.music.MusicDetails;
import org.apache.tapestry5.integration.app1.services.MusicLibrary;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.StreamPageContent;
import org.apache.tapestry5.util.TextStreamResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Tests the various event handler method return types.
 *
 * @see ComponentEventResultProcessor
 */
public class ReturnTypes
{
    @InjectPage
    private Index index;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private MusicLibrary library;

    Object onActionFromNullReturnValue()
    {
        return null;
    }

    Object onActionFromStringReturnValue()
    {
        return "index";
    }

    Object onActionFromClassReturnValue()
    {
        return Index.class;
    }

    Object onActionFromPageReturnValue()
    {
        return index;
    }

    Object onActionFromLinkReturnValue()
    {
        return linkSource.createPageRenderLink("index");
    }

    Object onActionFromStreamReturnValue()
    {
        String text = "<html><body>Success!</body></html>";
        return new TextStreamResponse("text/html", text);
    }

    Object onActionFromBadReturnValue()
    {
        // What is Tapestry supposed to do with this? Let's see that Exception Report page.
        return 20;
    }

    Object onActionFromURL() throws MalformedURLException
    {
        return new URL("http://google.com");
    }

    Object onActionFromStreamPageContent()
    {
        return new StreamPageContent(Index.class);
    }

    Object onActionFromStreamPageContentWithContext()
    {
        Track track = library.getById(294L);

        return new StreamPageContent(MusicDetails.class, track);
    }


    Object onActionFromHttpError()
    {
        return new HttpError(HttpServletResponse.SC_GONE, "Oups! Resource disappeared!");
    }
}
