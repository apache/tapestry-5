package org.apache.tapestry5.ioc.services.metrics;

public class MetricsSymbols
{
    /**
     * The directory in which RRDb database files are stored. If blank (the default),
     * then RRD is set up for in-memory databases only (re-created on each launch of the application).
     */
    public static final String RRD_DB_DIR = "tapestry.rrd-dir";
}
