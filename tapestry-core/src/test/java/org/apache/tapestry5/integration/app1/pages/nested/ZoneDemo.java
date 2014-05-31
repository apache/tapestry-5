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

package org.apache.tapestry5.integration.app1.pages.nested;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.QueryParameterConstants;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.integration.app1.data.RegistrationData;
import org.apache.tapestry5.integration.app1.pages.SecurePage;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.javascript.StylesheetLink;
import org.apache.tapestry5.services.javascript.StylesheetOptions;

import java.util.Date;

@Import(library = "zonedemo.js")
public class ZoneDemo
{
    @Component(id = "registrationForm")
    private BeanEditForm regform;

    private String name;

    @SessionState
    private RegistrationData registration;

    private static final String[] NAMES =
            {"Fred & Wilma", "Mr. <Roboto>", "Grim Fandango", "Registration", "Vote", "CSS Injection"};

    @Inject
    private Block registrationForm, registrationOutput, voteForm, voteOutput, empty, forUnknownZone, forNotAZone, ajaxCSS;

    @Property
    private String vote;

    @InjectComponent
    private Zone output;

    @InjectComponent
    private Zone zoneWithEmptyBody;
    
    @InjectPage
    private SecurePage securePage;

    @Environmental
    private JavaScriptSupport jss;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    @Path("zonedemo-overrides.css")
    private Asset overridesCSS;

    @Inject
    @Path("zonedemo-viaajax.css")
    private Asset viaAjaxCSS;

    public String[] getNames()
    {
        return NAMES;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Log
    Object onActionFromSelect(String name, @RequestParameter(QueryParameterConstants.ZONE_ID)
    String zoneId)
    {
        if (!zoneId.equals("output"))
            throw new AssertionError("Expected zoneId 'output' to be passed up in request.");

        this.name = name;

        if (name.equals("Registration"))
        {
            return registrationForm;
        }

        if (name.equals("Vote"))
        {
            return voteForm;
        }

        if (name.equals("CSS Injection"))
        {
            ajaxResponseRenderer.addCallback(new JavaScriptCallback()
            {
                public void run(JavaScriptSupport javascriptSupport)
                {
                    javascriptSupport.importStylesheet(viaAjaxCSS);
                }
            });

            return ajaxCSS;
        }

        return output.getBody();
    }

    void onActionFromFail()
    {
        throw new RuntimeException("Server-side exception.");
    }

    void onActionFromPoorlyFormattedFail()
    {
        throw new RuntimeException("Failure &\n\n<Stuff>!");
    }

    Object onSuccessFromRegistrationForm()
    {
        return registrationOutput;
    }

    Object onActionFromClear()
    {
        regform.clearErrors();
        registration = null;

        return registrationForm;
    }

    public RegistrationData getRegistration()
    {
        return registration;
    }

    Object onActionFromJSON()
    {
        JSONObject response = new JSONObject();

        response.put("content", "Directly coded JSON content");

        return response;
    }

    public Date getCurrentTime()
    {
        return new Date();
    }

    public ValueEncoder<String> getEncoder()
    {
        return new StringValueEncoder();
    }

    void onSelectedFromVoteYes()
    {
        vote = "Yes";
    }

    void onSelectedFromVoteNo()
    {
        vote = "No";
    }

    Object onSuccessFromVote()
    {
        return voteOutput;
    }

    Object onActionFromRedirect()
    {
        return AssetDemo.class;
    }

    Object onActionFromSecureRedirect()
    {
        return securePage;
    }

    Object onActionFromBlankUpdate()
    {
        return empty;
    }
    
    Object onActionFromUpdateZoneWithEmptyBody()
    {
        return zoneWithEmptyBody.getBody();
    }
    
    void afterRender()
    {
        jss.importStylesheet(new StylesheetLink(overridesCSS, new StylesheetOptions().asAjaxInsertionPoint()));
    }

    Object onActionFromBadZone()
    {
        return new MultiZoneUpdate("unknownZone", forUnknownZone);
    }

    Object onActionFromNonZoneUpdate()
    {
        return new MultiZoneUpdate("notAZone", forNotAZone);
    }
    
    void onActionFromUpdateViaAjaxResponseRenderer()
    {
        name = "AjaxResponseRenderer";
        ajaxResponseRenderer.addRender(output);
    }
}
