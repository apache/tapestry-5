package org.apache.tapestry5.integration.locallib.alpha.pages

import org.apache.tapestry5.Asset
import org.apache.tapestry5.annotations.Import
import org.apache.tapestry5.annotations.Path
import org.apache.tapestry5.ioc.annotations.Inject

@Import(library="show-logo.js")
class Logo {

    @Inject @Path("feature.jpg")
    Asset featureImage;
}
