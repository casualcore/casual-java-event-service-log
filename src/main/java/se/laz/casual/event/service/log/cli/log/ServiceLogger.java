/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.log;

import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.service.log.cli.internal.EventServiceLoggerException;
import se.laz.casual.event.service.log.cli.internal.StreamEncoder;
import se.laz.casual.event.service.log.cli.runner.EventServiceLogParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * Writes an event to the log file.
 */
public class ServiceLogger
{
    private final EventServiceLogParams eventServiceLogParams;
    private final String delimiter;
    private PrintWriter fileWriter;

    private final Object fileWriterLock = new Object();

    private ServiceLogger( Builder builder )
    {
        this.eventServiceLogParams = builder.eventServiceLogParams;
        this.delimiter = eventServiceLogParams.getLogColumnDelimiter();
        this.fileWriter = initialiseFileWriter( eventServiceLogParams.getLogFile() );
    }

    /**
     * Initialise the print writer using the file provided.
     * If the file already exists it will append.
     * If the file does not exist it will be created.
     *
     * @param file for PrintWriter to write to.
     * @return PrintWriter wrapping the file provided.
     */
    //2024-05-15 CK - File#createNewFile boolean return ignored, as no action required, see javadoc above.
    @SuppressWarnings( "squid:S899" )
    private PrintWriter initialiseFileWriter( File file )
    {
        try
        {
            file.createNewFile();
            return StreamEncoder.toPrintWriter( new PrintStream( new FileOutputStream( file, true ) ) );
        }
        catch( IOException e )
        {
            throw new EventServiceLoggerException( "Failed to open file.", e );
        }
    }

    public EventServiceLogParams getEventServiceLogParams()
    {
        return eventServiceLogParams;
    }

    @Override
    public String toString()
    {
        return "ServiceLogger{" +
                "eventServiceLogParams=" + eventServiceLogParams +
                '}';
    }

    /**
     * Format the event and write to the log file.
     * @param event to log.
     */
    public void logEvent( ServiceCallEvent event )
    {
        Objects.requireNonNull( event, "Event is null." );
        synchronized( fileWriterLock )
        {
            fileWriter.println( ServiceCallEventFormatter.format( event, delimiter ) );
            this.fileWriter.flush();
        }
    }

    /**
     * Reload the log file. Used, for example, to allow for log rotation.
     */
    public void reload()
    {
        synchronized( fileWriterLock )
        {
            this.fileWriter.flush();
            this.fileWriter.close();
            this.fileWriter = initialiseFileWriter( eventServiceLogParams.getLogFile() );
        }
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
