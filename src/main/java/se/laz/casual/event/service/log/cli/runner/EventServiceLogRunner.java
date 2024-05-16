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
import se.laz.casual.event.service.log.cli.client.EventHandler;
import se.laz.casual.event.service.log.cli.client.log.LogRotateHandler;
import se.laz.casual.event.service.log.cli.client.log.ServiceLogger;

import java.io.PrintWriter;
import java.util.Objects;
import java.util.UUID;

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

    @Override
    public int run()
    {
        outputStream.print( printParams() );
        outputStream.flush();

        ServiceCallEventStore store = ServiceCallEventStoreFactory.getStore( UUID.randomUUID() );

        ServiceLogger logger = initialiseLogger();
        EventHandler handler = initialiseEventHandler( logger );

        EventProcessor processor = new EventProcessor( store, handler );

        ClientAutoReconnector clientAutoReconnector = new ClientAutoReconnector( this, 30000 );

        clientAutoReconnector.maintainClientConnection( store::put );
        Quarkus.waitForExit();
        processor.stop();
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
