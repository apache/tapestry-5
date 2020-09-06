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

package org.apache.tapestry5.corelib.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.ContentType;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.UnknownActivationContextCheck;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.corelib.base.AbstractInternalPage;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.PageActivationContextCollector;
import org.apache.tapestry5.internal.services.ReloadHelper;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ExceptionReporter;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.URLEncoder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Responsible for reporting runtime exceptions. This page is quite verbose and is usually overridden in a production
 * application. When {@link org.apache.tapestry5.http.TapestryHttpSymbolConstants#PRODUCTION_MODE} is "true", it is very abbreviated.
 *
 * @see org.apache.tapestry5.corelib.components.ExceptionDisplay
 */
@UnknownActivationContextCheck(false)
@ContentType("text/html")
@Import(stylesheet = "ExceptionReport.css")
public class ExceptionReport extends AbstractInternalPage implements ExceptionReporter
{
    private static final String PATH_SEPARATOR_PROPERTY = "path.separator";

    // Match anything ending in .(something?)path.

    private static final Pattern PATH_RECOGNIZER = Pattern.compile("\\..*path$");

    @Property
    private String attributeName;

    @Inject
    @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
    @Property(write = false)
    private boolean productionMode;

    @Inject
    @Symbol(SymbolConstants.TAPESTRY_VERSION)
    @Property(write = false)
    private String tapestryVersion;

    @Inject
    @Symbol(TapestryHttpSymbolConstants.APPLICATION_VERSION)
    @Property(write = false)
    private String applicationVersion;

    @Property(write = false)
    private Throwable rootException;

    @Property
    private String propertyName;

    @Inject
    private RequestGlobals requestGlobals;

    @Inject
    private AlertManager alertManager;

    @Inject
    private PageActivationContextCollector pageActivationContextCollector;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private BaseURLSource baseURLSource;

    @Inject
    private ReloadHelper reloadHelper;

    @Inject
    private URLEncoder urlEncoder;

    @Property
    private String rootURL;

    @Property
    private ThreadInfo thread;

    @Inject
    private ComponentResources resources;

    private String failurePage;

    /**
     * A link the user may press to perform an action (e.g., "Reload page").
     */
    public static class ActionLink
    {
        public final String uri, label;


        public ActionLink(String uri, String label)
        {
            this.uri = uri;
            this.label = label;
        }
    }

    @Property
    private ActionLink actionLink;

    public class ThreadInfo implements Comparable<ThreadInfo>
    {
        public final String className, name, state, flags;

        public final ThreadGroup group;

        public ThreadInfo(String className, String name, String state, String flags, ThreadGroup group)
        {
            this.className = className;
            this.name = name;
            this.state = state;
            this.flags = flags;
            this.group = group;
        }

        @Override
        public int compareTo(ThreadInfo o)
        {
            return name.compareTo(o.name);
        }
    }

    private final String pathSeparator = System.getProperty(PATH_SEPARATOR_PROPERTY);

    /**
     * Returns true for normal, non-XHR requests. Links (to the failure page, or to root page) are only
     * presented if showActions is true.
     */
    public boolean isShowActions()
    {
        return !request.isXHR();
    }

    /**
     * Returns true in development mode; enables the "with reload" actions.
     */
    public boolean isShowReload()
    {
        return !productionMode;
    }

    public void reportException(Throwable exception)
    {
        rootException = exception;

        rootURL = baseURLSource.getBaseURL(request.isSecure());

        // Capture this now ... before the gears are shifted around to make ExceptionReport the active page.
        failurePage = (request.getAttribute(InternalConstants.ACTIVE_PAGE_LOADED) == null)
                ? null
                : requestGlobals.getActivePageName();
    }

    private static void add(List<ActionLink> links, Link link, String format, Object... arguments)
    {
        String label = String.format(format, arguments);
        links.add(new ActionLink(link.toURI(), label));
    }

    public List<ActionLink> getActionLinks()
    {
        List<ActionLink> links = CollectionFactory.newList();

        if (failurePage != null)
        {

            try
            {

                Object[] pac = pageActivationContextCollector.collectPageActivationContext(failurePage);

                add(links,
                        linkSource.createPageRenderLinkWithContext(failurePage, pac),
                        "Go to page <strong>%s</strong>", failurePage);

                if (!productionMode)
                {
                    add(links,
                            resources.createEventLink("reloadFirst", pac).addParameter("loadPage",
                                    urlEncoder.encode(failurePage)),
                            "Go to page <strong>%s</strong> (with reload)", failurePage);
                }

            } catch (Throwable t)
            {
                // Ignore.
            }
        }

        links.add(new ActionLink(rootURL,
                String.format("Go to <strong>%s</strong>", rootURL)));


        if (!productionMode)
        {
            add(links,
                    resources.createEventLink("reloadFirst"),
                    "Go to <strong>%s</strong> (with reload)", rootURL);
        }

        return links;
    }


    Object onReloadFirst(EventContext reloadContext)
    {
        reloadHelper.forceReload();

        return linkSource.createPageRenderLinkWithContext(urlEncoder.decode(request.getParameter("loadPage")), reloadContext);
    }

    Object onReloadRoot() throws MalformedURLException
    {
        reloadHelper.forceReload();

        return new URL(baseURLSource.getBaseURL(request.isSecure()));
    }


    public boolean getHasSession()
    {
        return request.getSession(false) != null;
    }

    public Session getSession()
    {
        return request.getSession(false);
    }

    public Object getAttributeValue()
    {
        return getSession().getAttribute(attributeName);
    }

    /**
     * Returns a <em>sorted</em> list of system property names.
     */
    public List<String> getSystemProperties()
    {
        return InternalUtils.sortedKeys(System.getProperties());
    }

    public String getPropertyValue()
    {
        return System.getProperty(propertyName);
    }

    public boolean isComplexProperty()
    {
        return PATH_RECOGNIZER.matcher(propertyName).find() && getPropertyValue().contains(pathSeparator);
    }

    public String[] getComplexPropertyValue()
    {
        // Neither : nor ; is a regexp character.

        return getPropertyValue().split(pathSeparator);
    }

    public List<ThreadInfo> getThreads()
    {
        return F.flow(TapestryInternalUtils.getAllThreads()).map(new Mapper<Thread, ThreadInfo>()
        {
            @Override
            public ThreadInfo map(Thread t)
            {
                List<String> flags = CollectionFactory.newList();

                if (t.isDaemon())
                {
                    flags.add("daemon");
                }
                if (!t.isAlive())
                {
                    flags.add("NOT alive");
                }
                if (t.isInterrupted())
                {
                    flags.add("interrupted");
                }

                if (t.getPriority() != Thread.NORM_PRIORITY)
                {
                    flags.add("priority " + t.getPriority());
                }

                return new ThreadInfo(Thread.currentThread() == t ? "active-thread" : "",
                        t.getName(),
                        t.getState().name(),
                        InternalUtils.join(flags),
                        t.getThreadGroup());
            }
        }).sort().toList();
    }
}
