package org.apache.tapestry5.beanvalidator;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.StylesheetLink;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BeanValidatorStack implements JavaScriptStack
{
    public static final String STACK_ID = "BeanValidatorStack";

    private final List<Asset> javaScriptStack;

    public BeanValidatorStack(final AssetSource assetSource, final ThreadLocale threadLocale)
    {
        javaScriptStack = Arrays.asList(assetSource.getAsset(null,
                "org/apache/tapestry5/beanvalidator/tapestry-beanvalidator.js", threadLocale.getLocale()));
    }

    public List<String> getStacks()
    {
        return Collections.emptyList();
    }

    public List<Asset> getJavaScriptLibraries()
    {

        return javaScriptStack;
    }

    public List<StylesheetLink> getStylesheets()
    {

        return Collections.emptyList();
    }

    public String getInitialization()
    {
        return null;
    }
}
