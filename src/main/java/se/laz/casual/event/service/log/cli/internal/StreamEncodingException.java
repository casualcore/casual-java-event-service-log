/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.internal;

public class StreamEncodingException extends EventServiceLoggerException
{
    private static final long serialVersionUID = 1L;

    public StreamEncodingException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
