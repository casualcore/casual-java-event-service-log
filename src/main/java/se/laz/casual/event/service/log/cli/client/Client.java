/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client;

import se.laz.casual.event.client.EventClient;
import se.laz.casual.event.service.log.cli.client.log.ServiceLogger;
import se.laz.casual.event.service.log.cli.internal.EventServerConnectionException;

import java.net.URI;
import java.util.Objects;

public class Client
{

    private final EventClient eventClient;
    private final URI eventServerUrl;
    private final ServiceLogger logger;

    public Client( Builder builder )
    {
        this.eventClient = builder.eventClient;
        this.eventServerUrl = builder.eventServerUrl;
        this.logger = builder.logger;
    }

    public EventClient getEventClient()
    {
        return eventClient;
    }

    public URI getEventServerUrl()
    {
        return eventServerUrl;
    }

    public ServiceLogger getLogger()
    {
        return logger;
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
        Client client1 = (Client) o;
        return Objects.equals( eventClient, client1.eventClient ) && Objects.equals( eventServerUrl, client1.eventServerUrl ) && Objects.equals( logger, client1.logger );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( eventClient, eventServerUrl, logger );
    }

    @Override
    public String toString()
    {
        return "Client{" +
                "client=" + eventClient +
                ", eventServerUrl='" + eventServerUrl + '\'' +
                ", logger=" + logger +
                '}';
    }

    public void close()
    {
        if( eventClient != null )
        {
            eventClient.close();
        }
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private EventClient eventClient;
        private URI eventServerUrl;
        private ServiceLogger logger;

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

        public Builder logger( ServiceLogger logger )
        {
            this.logger = logger;
            return this;
        }

        public Client build()
        {
            Objects.requireNonNull( eventServerUrl, "EventServerUrl is null." );
            Objects.requireNonNull( logger, "Logger is null." );
            if( eventClient == null )
            {
                try
                {
                    eventClient = EventClient.createBuilder().withHost( eventServerUrl.getHost() )
                            .withPort( eventServerUrl.getPort() )
                            .withEventObserver( e -> {
                                logger.logEvent( e );
                                System.out.println( "Received: " + e );
                            } )
                            .withConnectionObserver( (eventClient) -> { System.out.println( "Closed." );throw new EventServerConnectionException( "Connection to event server closed." ); } )
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
