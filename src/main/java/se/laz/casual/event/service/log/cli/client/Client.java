/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client;

import se.laz.casual.event.client.EventClient;
import se.laz.casual.event.client.EventObserver;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Connects to the event server, forwarding incoming events to an observer for processing.
 * <br/>
 * A wrapper for {@link EventClient} providing a configurable
 * {@link EventObserver} to process received events as well as
 * convenience methods to handle disconnection from the event server.
 */
public class Client
{
    private final EventClient eventClient;
    private final URI eventServerUrl;
    private final EventObserver eventObserver;
    private final CompletableFuture<Boolean> disconnected;

    public Client( Builder builder )
    {
        this.eventClient = builder.eventClient;
        this.eventServerUrl = builder.eventServerUrl;
        this.eventObserver = builder.eventObserver;
        this.disconnected = builder.disconnected;
    }

    public EventClient getEventClient()
    {
        return eventClient;
    }

    public URI getEventServerUrl()
    {
        return eventServerUrl;
    }

    public EventObserver getEventObserver()
    {
        return eventObserver;
    }

    @Override
    public String toString()
    {
        return "Client{" +
                "eventClient=" + eventClient +
                ", eventServerUrl=" + eventServerUrl +
                ", eventObserver=" + eventObserver +
                '}';
    }

    public void close()
    {
        if( eventClient != null )
        {
            eventClient.close();
        }
    }

    public void waitForDisconnect()
    {
        this.disconnected.join();
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private EventClient eventClient;
        private URI eventServerUrl;
        private EventObserver eventObserver;
        private CompletableFuture<Boolean> disconnected = new CompletableFuture<>();

        private Builder()
        {
        }

        public Builder eventClient( EventClient client )
        {
            this.eventClient = client;
            return this;
        }

        public Builder eventServerUrl( URI eventServerUrl )
        {
            this.eventServerUrl = eventServerUrl;
            return this;
        }

        public Builder eventObserver( EventObserver handler )
        {
            this.eventObserver = handler;
            return this;
        }

        public Client build()
        {
            Objects.requireNonNull( eventServerUrl, "EventServerUrl is null." );
            Objects.requireNonNull( eventObserver, "Event Observer is null." );
            if( eventClient == null )
            {
                try
                {
                    eventClient = EventClient.createBuilder().withHost( eventServerUrl.getHost() )
                            .withPort( eventServerUrl.getPort() )
                            .withEventObserver( eventObserver )
                            .withConnectionObserver( e-> disconnected.complete( true ) )
                            .build();
                    eventClient.connect().get();
                }
                catch( InterruptedException e )
                {
                    Thread.currentThread().interrupt();
                    throw new EventServerConnectionException( "Thread interupted during connection.", e );
                }
                catch( Exception t )
                {
                    throw new EventServerConnectionException( "Failed to connect to event server at: " + eventServerUrl, t );
                }
            }
            return new Client( this );
        }
    }
}
