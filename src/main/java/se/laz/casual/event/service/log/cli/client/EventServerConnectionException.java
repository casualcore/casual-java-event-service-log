/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client;

import se.laz.casual.event.service.log.cli.internal.EventServiceLoggerException;

public class EventServerConnectionException extends EventServiceLoggerException
{
    private static final long serialVersionUID = 1L;

    public EventServerConnectionException( String message )
    {
        super( message );
    }

    public EventServerConnectionException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
