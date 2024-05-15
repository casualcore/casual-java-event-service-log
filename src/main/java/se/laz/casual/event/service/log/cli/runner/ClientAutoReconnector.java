/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner;

import se.laz.casual.event.service.log.cli.client.Client;
import se.laz.casual.event.service.log.cli.client.EventHandler;
import se.laz.casual.event.service.log.cli.internal.EventServerConnectionException;

import java.util.concurrent.CompletableFuture;

/**
 * Maintains a connection to the event server, initiating reconnection after disconnected.
 */
public class ClientAutoReconnector
{
    private final EventServiceLogRunner eventServiceLogRunner;
    private final long backoff;
    private CompletableFuture<Boolean> connected = new CompletableFuture<>();

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

    public void maintainClientConnection( EventHandler eventHandler )
    {
        while( !stop )
        {
            try
            {
                client = Client.newBuilder().eventServerUrl( eventServiceLogRunner.getParams().getEventServerUrl() )
                        .eventHandler( eventHandler ).build();
                connected.complete( true );
                eventServiceLogRunner.getOutputStream().println( "Connected." );
                eventServiceLogRunner.getOutputStream().flush();
                client.waitForDisconnect();
                eventServiceLogRunner.getOutputStream().println( "Disconnected, retrying in "+ backoff + "ms." );
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
                long start = System.currentTimeMillis();
                long remaining = backoff;
                do
                {
                    try
                    {
                        Thread.sleep( remaining );
                    }
                    catch( InterruptedException e )
                    {
                        Thread.currentThread().interrupt();
                    }
                    long end = System.currentTimeMillis();
                    remaining = backoff - (end-start);
                }
                while( remaining > 0 );
            }
        }
    }

    public void stop()
    {
        this.stop = true;
        if( client != null )
        {
            client.close();
        }
    }
}