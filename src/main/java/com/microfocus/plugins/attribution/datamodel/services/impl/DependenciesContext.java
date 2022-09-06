package com.microfocus.plugins.attribution.datamodel.services.impl;

import org.apache.maven.plugin.logging.Log;

public class DependenciesContext
{
    private Log log;

    private boolean skipDownloadUrl;
    private boolean includeTransitiveDependencies;
    private int threads;

    public DependenciesContext( final Log log, final boolean skipDownloadUrl, final boolean includeTransitiveDependencies, final int threads )
    {
        this.log = log;
        this.skipDownloadUrl = skipDownloadUrl;
        this.includeTransitiveDependencies = includeTransitiveDependencies;
        this.threads = threads;
    }

    public Log getLog()
    {
        return log;
    }

    public boolean isSkipDownloadUrl()
    {
        return skipDownloadUrl;
    }

    public boolean isIncludeTransitiveDependencies()
    {
        return includeTransitiveDependencies;
    }

    public int getThreads()
    {
        return threads;
    }
}
