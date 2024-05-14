/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client.log;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.Objects;

public class LogRotateHandler implements SignalHandler
{
    public static final Signal SIGHUP = new Signal( "HUP" );
    private final ServiceLogger serviceLogger;

    private LogRotateHandler( Builder builder )
    {
        this.serviceLogger = builder.serviceLogger;
    }

    public ServiceLogger getServiceLogger()
    {
        return serviceLogger;
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    @Override
    public void handle( Signal sig )
    {
        this.serviceLogger.reload();
    }

    public static final class Builder
    {
        private ServiceLogger serviceLogger;

        private Builder()
        {
        }

        public Builder serviceLogger( ServiceLogger serviceLogger )
        {
            this.serviceLogger = serviceLogger;
            return this;
        }

        public LogRotateHandler build()
        {
            Objects.requireNonNull( serviceLogger, "ServiceLogger is null." );
            LogRotateHandler handler = new LogRotateHandler( this );

            initialiseSun( handler );

            return handler;
        }

        private void initialiseSun( SignalHandler handler )
        {
            Signal.handle( SIGHUP, handler );
        }
    }
}
