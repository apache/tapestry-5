package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.ioc.annotations.Inject;

public class MissingAssetDemo
{
    @Inject @Path("does-not-exist.txt")
    private Asset missing;
}
