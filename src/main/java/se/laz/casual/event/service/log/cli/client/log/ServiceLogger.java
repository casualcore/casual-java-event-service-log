/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client.log;

import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.service.log.cli.internal.StreamEncoder;
import se.laz.casual.event.service.log.cli.runner.EventServiceLogParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Objects;

public class ServiceLogger
{
    private final EventServiceLogParams eventServiceLogParams;
    private final String delimiter;
    private final PrintWriter fileWriter;

    private ServiceLogger( Builder builder )
    {
        this.eventServiceLogParams = builder.eventServiceLogParams;
        this.delimiter = eventServiceLogParams.getLogColumnDelimiter();
        this.fileWriter = initialiseFileWriter( eventServiceLogParams.getLogFile() );
    }

    private PrintWriter initialiseFileWriter( File file )
    {
        try
        {
            if( !file.exists() )
            {
                file.createNewFile();
            }
            return StreamEncoder.toPrintWriter( new PrintStream( new FileOutputStream( file, true ) ) );
        }
        catch( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public EventServiceLogParams getEventServiceLogParams()
    {
        return eventServiceLogParams;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        ServiceLogger that = (ServiceLogger) o;
        return Objects.equals( eventServiceLogParams, that.eventServiceLogParams );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( eventServiceLogParams );
    }

    @Override
    public String toString()
    {
        return "ServiceLogger{" +
                "eventServiceLogParams=" + eventServiceLogParams +
                '}';
    }

    public void logEvent( ServiceCallEvent event )
    {
        Objects.requireNonNull( event, "Event is null." );
        fileWriter.println( ServiceCallEventFormatter.format( event, delimiter ) );
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private EventServiceLogParams eventServiceLogParams;

        private Builder()
        {
        }

        public Builder eventServiceLogParams( EventServiceLogParams params )
        {
            this.eventServiceLogParams = params;
            return this;
        }

        public ServiceLogger build()
        {
            return new ServiceLogger( this );
        }
    }
}
