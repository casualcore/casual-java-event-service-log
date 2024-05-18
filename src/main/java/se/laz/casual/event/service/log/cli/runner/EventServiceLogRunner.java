/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner;

import io.quarkus.runtime.Quarkus;
import se.laz.casual.event.ServiceCallEventStore;
import se.laz.casual.event.ServiceCallEventStoreFactory;
import se.laz.casual.event.service.log.cli.CommandRunner;
import se.laz.casual.event.service.log.cli.log.EventHandler;
import se.laz.casual.event.service.log.cli.log.LogRotateHandler;
import se.laz.casual.event.service.log.cli.log.ServiceLogger;

import java.io.PrintWriter;
import java.util.Objects;
import java.util.UUID;

/**
 * Runs the command based on the validated inputs provided.
 *
 * Input validation is performed prior this, by picocli.
 */
public class EventServiceLogRunner implements CommandRunner<EventServiceLogParams>
{
    private final EventServiceLogParams params;
    private final PrintWriter outputStream;

    public EventServiceLogRunner( EventServiceLogParams params, PrintWriter outputStream )
    {
        Objects.requireNonNull( params, "Params is null." );
        Objects.requireNonNull( outputStream, "Output stream is null." );
        this.params = params;
        this.outputStream = outputStream;
    }

    @Override
    public EventServiceLogParams getParams()
    {
        return this.params;
    }

    public PrintWriter getOutputStream()
    {
        return outputStream;
    }

    /**
     * The commands execution entry point.
     * <br/>
     * Initialise based on input params.
     * <br/>
     * Establish a client connection is maintained continuously placing incoming events in a store for processing.
     * <br/>
     * Filter all incoming events from the store logging the remaining formatted events to the log file.
     * @return exit code.
     */
    @Override
    public int run()
    {
        outputStream.println( printParams() );
        outputStream.flush();

        // Initialise event store, logger and event handler.
        ServiceCallEventStore store = ServiceCallEventStoreFactory.getStore( UUID.randomUUID() );
        ServiceLogger logger = initialiseLogger();
        EventHandler handler = initialiseEventHandler( logger );

        // Run event processor and establish client connection.
        EventStoreProcessor storeProcessor = new EventStoreProcessor( store, handler );
        ClientAutoReconnector clientAutoReconnector = new ClientAutoReconnector( store::put,this, 30000 );

        Quarkus.waitForExit();

        // Stop processing and disconnect client.
        storeProcessor.stop();
        clientAutoReconnector.stop();
        return 0;
    }

    private ServiceLogger initialiseLogger()
    {
        ServiceLogger logger = ServiceLogger.newBuilder().eventServiceLogParams( this.getParams() ).build();
        LogRotateHandler.newBuilder().serviceLogger( logger ).build();
        return logger;
    }

    private EventHandler initialiseEventHandler(ServiceLogger logger)
    {
        return EventHandler.newBuilder().serviceLogger( logger )
                .filterInclusive( this.getParams().getLogFilterInclusive().orElse( null ) )
                .filterExclusive( this.getParams().getLogFilterExclusive().orElse( null ) )
                .build();
    }

    private StringBuilder printParams()
    {
        StringBuilder builder = new StringBuilder()
                .append( "--file: " ).append( params.getLogFile() ).append( System.lineSeparator() )
                .append( "--delimiter: " ).append( params.getLogColumnDelimiter() ).append( System.lineSeparator() );

        builder.append( "--filter-inclusive: " );
        params.getLogFilterInclusive().ifPresent( p -> builder.append( p.pattern() ) );
        builder.append( System.lineSeparator() );

        builder.append( "--filter-exclusive: " );
        params.getLogFilterExclusive().ifPresent( p -> builder.append( p.pattern() ) );
        builder.append( System.lineSeparator() );

        builder.append( "--eventServerUrl: " ).append( params.getEventServerUrl() )
                .append( System.lineSeparator() );
        return builder;
    }
}
