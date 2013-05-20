package org.apache.tapestry5.internal.wro4j;

import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.wro4j.services.ResourceProcessor;
import org.apache.tapestry5.wro4j.services.ResourceProcessorSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class CSSMinimizer extends AbstractMinimizer
{
    private final ResourceProcessor processor;

    public CSSMinimizer(Logger logger, OperationTracker tracker, AssetChecksumGenerator checksumGenerator, ResourceProcessorSource processorSource)
    {
        super(logger, tracker, checksumGenerator, "text/css");

        processor = processorSource.getProcessor("CSSMinimizer");
    }

    @Override
    protected InputStream doMinimize(StreamableResource resource) throws IOException
    {
        return processor.process("Minimizing " + resource, resource.getDescription(), resource.openStream(), "text/css");
    }
}
