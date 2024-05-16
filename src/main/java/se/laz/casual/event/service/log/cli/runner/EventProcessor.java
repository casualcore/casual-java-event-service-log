/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner;

import se.laz.casual.event.ServiceCallEventStore;
import se.laz.casual.event.service.log.cli.client.EventHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Read events from the store and send them handling.
 */
public class EventProcessor
{
    private final ExecutorService executorService = Executors.newFixedThreadPool( 1 );

    private final ServiceCallEventStore store;
    private final EventHandler handler;

    private boolean stop = false;

    public EventProcessor( ServiceCallEventStore store, EventHandler handler )
    {
        this.store = store;
        this.handler = handler;
        initialiseProcessing();
    }

    private void initialiseProcessing()
    {
        executorService.submit( this::tryProcess );
    }

    private void tryProcess()
    {
        while( !stop )
        {
            this.handler.notify( this.store.take() );
        }
    }

    public void stop()
    {
        this.stop = true;
        executorService.shutdownNow();
    }
}
