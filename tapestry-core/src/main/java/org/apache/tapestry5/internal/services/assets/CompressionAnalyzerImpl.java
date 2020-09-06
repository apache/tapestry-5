package org.apache.tapestry5.internal.services.assets;

import java.util.Map;

import org.apache.tapestry5.http.services.CompressionAnalyzer;

public class CompressionAnalyzerImpl implements CompressionAnalyzer
{
    private final Map<String, Boolean> configuration;

    public CompressionAnalyzerImpl(Map<String, Boolean> configuration)
    {
        this.configuration = configuration;
    }

    public boolean isCompressable(String contentType)
    {
        if (contentType == null) {
            throw new IllegalStateException("Content type provided to CompressionAnalyzer is null, which is not allowed.");
        }

        int x = contentType.indexOf(';');

        String key = x < 0 ? contentType : contentType.substring(0, x);

        Boolean result = configuration.get(key);

        if (result != null) {
            return result;
        }

        // Now look for a wild card.

        x = contentType.indexOf('/');

        String wildKey = contentType.substring(0, x) + "/*";

        result = configuration.get(wildKey);

        return result == null ? true : result;
    }
}
