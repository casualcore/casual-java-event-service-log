/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.log;

import se.laz.casual.event.ServiceCallEvent;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Handle incoming events.
 * Applying necessary filtering prior to logging.
 */
public class EventHandler
{
    private final ServiceLogger serviceLogger;
    private final Pattern filterInclusive;
    private final Pattern filterExclusive;

    private EventHandler( Builder builder )
    {
        this.serviceLogger = builder.serviceLogger;
        this.filterInclusive = builder.filterInclusive;
        this.filterExclusive = builder.filterExclusive;
    }

    public ServiceLogger getServiceLogger()
    {
        return serviceLogger;
    }

    public Pattern getFilterInclusive()
    {
        return filterInclusive;
    }

    public Pattern getFilterExclusive()
    {
        return filterExclusive;
    }

    @Override
    public String toString()
    {
        return "EventHandler{" +
                "serviceLogger=" + serviceLogger +
                ", filterInclusive=" + filterInclusive +
                ", filterExclusive=" + filterExclusive +
                '}';
    }

    public void handle( ServiceCallEvent event )
    {
        Objects.requireNonNull( event, "Event is null." );
        if( filterInclusive != null && !filterInclusive.matcher( event.getService() ).matches() )
        {
            return;
        }

        if( filterExclusive != null && filterExclusive.matcher( event.getService() ).matches() )
        {
            return;
        }

        serviceLogger.logEvent( event );
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private ServiceLogger serviceLogger;
        private Pattern filterInclusive;
        private Pattern filterExclusive;

        private Builder()
        {
        }

        public Builder serviceLogger( ServiceLogger serviceLogger )
        {
            this.serviceLogger = serviceLogger;
            return this;
        }

        public Builder filterInclusive( Pattern filterInclusive )
        {
            this.filterInclusive = filterInclusive;
            return this;
        }

        public Builder filterExclusive( Pattern filterExclusive )
        {
            this.filterExclusive = filterExclusive;
            return this;
        }

        public EventHandler build()
        {
            Objects.requireNonNull( serviceLogger, "ServiceLogger is null." );
            return new EventHandler( this );
        }
    }
}
