/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.log;

import java.util.Objects;

/**
 * Ensures that after SIGHUP is received that the log file is rotated.
 */
//2024-05-15 CK - We have to use sun.misc.Signal. Other alternatives don't currently work fully.
@SuppressWarnings( "squid:S1191" )
public class LogRotateHandler implements sun.misc.SignalHandler
{
    public static final sun.misc.Signal SIGHUP = new sun.misc.Signal( "HUP" );
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
    public void handle( sun.misc.Signal sig )
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

        private void initialiseSun( sun.misc.SignalHandler handler )
        {
            sun.misc.Signal.handle( SIGHUP, handler );
        }
    }
}
