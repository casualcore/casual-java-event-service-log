/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client;

import se.laz.casual.event.client.EventClient;
import se.laz.casual.event.service.log.cli.internal.EventServerConnectionException;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Client
{

    private final EventClient eventClient;
    private final URI eventServerUrl;
    private final EventHandler eventHandler;
    private final CompletableFuture<Boolean> disconnected;

    public Client( Builder builder )
    {
        this.eventClient = builder.eventClient;
        this.eventServerUrl = builder.eventServerUrl;
        this.eventHandler = builder.eventHandler;
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

    public EventHandler getEventHandler()
    {
        return eventHandler;
    }

    @Override
    public String toString()
    {
        return "Client{" +
                "eventClient=" + eventClient +
                ", eventServerUrl=" + eventServerUrl +
                ", eventHandler=" + eventHandler +
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
        private EventHandler eventHandler;
        private CompletableFuture<Boolean> disconnected = new CompletableFuture<>();

        public Builder()
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

        public Builder eventHandler( EventHandler handler )
        {
            this.eventHandler = handler;
            return this;
        }

        public Client build()
        {
            Objects.requireNonNull( eventServerUrl, "EventServerUrl is null." );
            Objects.requireNonNull( eventHandler, "Logger is null." );
            if( eventClient == null )
            {
                try
                {
                    eventClient = EventClient.createBuilder().withHost( eventServerUrl.getHost() )
                            .withPort( eventServerUrl.getPort() )
                            .withEventObserver( eventHandler )
                            .withConnectionObserver( (e)-> disconnected.complete( true ) )
                            .build();
                    eventClient.connect().get();
                }
                catch( Throwable t )
                {
                    throw new EventServerConnectionException( "Failed to connect to event server at: " + eventServerUrl, t );
                }
            }
            return new Client( this );
        }
    }
}
