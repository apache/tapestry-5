// Copyright 2007 The Apache Software Foundation
//
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

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.integration.app1.data.Track;
import org.apache.tapestry5.integration.app1.services.MusicLibrary;
import org.apache.tapestry5.ioc.annotations.AnnotationUseContext;
import org.apache.tapestry5.ioc.annotations.Inject;

import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AutocompleteDemo
{
    @Inject
    private MusicLibrary library;

    @Persist
    @Property
    private String title;

    @Persist
    @Property
    private String withContext;

    @Persist
    @Property
    private String required;

    @Inject
    private AlertManager alertManager;

    public Object[] getContext() {
        return new Object[] {1, RetentionPolicy.RUNTIME, AnnotationUseContext.MIXIN};
    }

    List onProvideCompletionsFromWithContext(String partial, Integer integer,
            RetentionPolicy retentionPolicy, AnnotationUseContext annotationUseContext) {
        return Arrays.asList(String.format("%s : %03d, %s, %s", partial,
                integer, retentionPolicy, annotationUseContext));
    }

    List onProvideCompletionsFromRequired(String partial) {
        return Collections.singletonList("foo");
    }

    List onProvideCompletionsFromTitle(String partialTitle) throws Exception
    {
        boolean roundabout = false;

        List<Track> matches = library.findByMatchingTitle(partialTitle);

        List<String> result = CollectionFactory.newList();

        for (Track t : matches)
        {
            result.add(t.getTitle());

            roundabout |= t.getTitle().equals("Roundabout");
        }

        // Thread.sleep(1000);

        if (roundabout)
        {
            alertManager.info("Completions include 'Roundabout'.");
        }

        return result;
    }

    void onSuccess() {
        alertManager.alert(Duration.SINGLE, Severity.INFO, String.format("Title: %s", title));
        alertManager.alert(Duration.SINGLE, Severity.INFO, String.format("With context: %s", withContext));
        alertManager.alert(Duration.SINGLE, Severity.INFO, String.format("required: %s", required));
    }

}
