/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner;

import se.laz.casual.event.client.EventObserver;
import se.laz.casual.event.service.log.cli.client.Client;
import se.laz.casual.event.service.log.cli.client.EventServerConnectionException;

import java.io.PrintWriter;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Maintains a connection to the event server, initiating reconnection after disconnected
 * with configurable fixed backoff.
 * Utilises a {@link ScheduledExecutorService} for perform client connection attempts.
 */
public class ClientAutoReconnector
{
    private final EventObserver eventObserver;
    private final URI eventServerUrl;
    private final PrintWriter outputStream;
    private final long backoff;
    private CompletableFuture<Boolean> connected = new CompletableFuture<>();

    private final ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor( 1 );

    private boolean stop = false;
    private Client client;

    public ClientAutoReconnector( EventObserver observer, EventServiceLogRunner eventServiceLogRunner, long backoff )
    {
        this.eventObserver = observer;
        this.eventServerUrl = eventServiceLogRunner.getParams().getEventServerUrl();
        this.outputStream = eventServiceLogRunner.getOutputStream();
        this.backoff = backoff;
        initialiseConnection(  );
    }

    /**
     * Wait for the connection to be successful.
     */
    public void waitForConnection( )
    {
        boolean done;
        do
        {
            done = connected.join();
        }
        while( !done );
    }

    private void initialiseConnection( )
    {
        scheduledExecutor.schedule( this::tryConnect, 0, TimeUnit.MILLISECONDS );
    }

    private void tryConnect( )
    {
        try
        {
            client = Client.newBuilder().eventServerUrl( eventServerUrl ).eventObserver( eventObserver ).build();
            connected.complete( true );
            outputStream.println( "Connected to: " + eventServerUrl );
            outputStream.flush();
            client.waitForDisconnect();
            outputStream.println( "Disconnected from: " + eventServerUrl + ", retrying in " + backoff + "ms." );
            outputStream.flush();
        }
        catch( EventServerConnectionException e )
        {
            connected.complete( false );
            outputStream.println( "Connection failed, retrying in " + backoff + "ms: " + e.getMessage() );
            outputStream.flush();
        }
        finally
        {
            connected = new CompletableFuture<>();
            if( !stop )
            {
                scheduledExecutor.schedule( this::tryConnect, backoff, TimeUnit.MILLISECONDS );
            }
        }
    }

    /**
     * Stop current client connection and any reconnection attempts.
     */
    public void stop()
    {
        this.stop = true;
        this.scheduledExecutor.shutdown();
        if( client != null )
        {
            client.close();
        }
        //TODO: 2024-05-18 - CK - Check the order for these, should we call close first before shutdown?
    }
}