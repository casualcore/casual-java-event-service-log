/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner;

import se.laz.casual.event.client.EventObserver;
import se.laz.casual.event.service.log.cli.client.Client;
import se.laz.casual.event.service.log.cli.internal.EventServerConnectionException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Maintains a connection to the event server, initiating reconnection after disconnected
 * with configurable fixed backoff.
 */
public class ClientAutoReconnector
{
    private final EventServiceLogRunner eventServiceLogRunner;
    private final long backoff;
    private CompletableFuture<Boolean> connected = new CompletableFuture<>();

    private final ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor( 1 );

    private boolean stop = false;
    private Client client;

    public ClientAutoReconnector( EventServiceLogRunner eventServiceLogRunner, long backoff )
    {
        this.eventServiceLogRunner = eventServiceLogRunner;
        this.backoff = backoff;
    }

    public void waitForConnection( )
    {
        boolean done;
        do
        {
            done = connected.join();
        }
        while( !done );
    }

    /**
     * Continually reconnect the client until told to stop via {@link #stop}.
     *
     * @param eventHandler the handler the client should use when receiving events.
     */
    public void maintainClientConnection( EventObserver eventHandler )
    {
        if( stop )
        {
            throw new IllegalStateException( "ClientAutoReconnector has already been stopped." );
        }
        scheduledExecutor.schedule( ()->tryConnect( eventHandler ), 0, TimeUnit.MILLISECONDS );
    }

    private void tryConnect( EventObserver eventHandler )
    {
        try
        {
            client = Client.newBuilder().eventServerUrl( eventServiceLogRunner.getParams().getEventServerUrl() )
                            .eventHandler( eventHandler ).build();
            connected.complete( true );
            eventServiceLogRunner.getOutputStream().println( "Connected to: " + eventServiceLogRunner.getParams().getEventServerUrl() );
            eventServiceLogRunner.getOutputStream().flush();
            client.waitForDisconnect();
            eventServiceLogRunner.getOutputStream().println( "Disconnected, retrying in " + backoff + "ms." );
            eventServiceLogRunner.getOutputStream().flush();
        }
        catch( EventServerConnectionException e )
        {
            connected.complete( false );
            eventServiceLogRunner.getOutputStream().println( "Connection failed, retrying in " + backoff + "ms: " + e.getMessage() );
            eventServiceLogRunner.getOutputStream().flush();
        }
        finally
        {
            connected = new CompletableFuture<>();
            if( !stop )
            {
                scheduledExecutor.schedule( () -> tryConnect( eventHandler ), backoff, TimeUnit.MILLISECONDS );
            }
        }
    }

    /**
     * Stop the reconnection attempts.
     */
    public void stop()
    {
        this.stop = true;
        this.scheduledExecutor.shutdown();
        if( client != null )
        {
            client.close();
        }
    }
}