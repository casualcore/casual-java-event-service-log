/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.internal;

public class EventServiceLoggerException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public EventServiceLoggerException()
    {
    }

    public EventServiceLoggerException( String message )
    {
        super( message );
    }

    public EventServiceLoggerException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public EventServiceLoggerException( Throwable cause )
    {
        super( cause );
    }
}
