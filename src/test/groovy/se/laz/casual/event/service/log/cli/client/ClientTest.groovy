/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.client.EventClient
import se.laz.casual.event.client.EventObserver
import se.laz.casual.test.CasualEmbeddedServer
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ClientTest extends Specification
{
    @Shared CasualEmbeddedServer embeddedServer

    String service1 = "test1"
    String parent1 = "parent"
    int pid1 = 123
    UUID execution1 = UUID.randomUUID()
    Xid transactionId1 = Mock(Xid)
    long pending1 = 5L
    ErrorState code1 = ErrorState.OK
    Order order1 = Order.CONCURRENT

    Instant start1 = ZonedDateTime.parse( "2024-04-15T12:34:56.123456Z", DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant()
    Instant end1 = ZonedDateTime.parse( "2024-04-15T12:35:04.123456Z", DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant()
    ServiceCallEvent event = ServiceCallEvent.createBuilder(  )
            .withService(service1)
            .withParent(parent1)
            .withPID(pid1)
            .withExecution(execution1)
            .withTransactionId(transactionId1)
            .withPending( pending1 )
            .withStart( start1 )
            .withEnd( end1 )
            .withCode(code1)
            .withOrder(order1)
            .build()

    @Shared URI eventServerUrl

    def setupSpec()
    {
        embeddedServer = CasualEmbeddedServer.newBuilder()
                .eventServerEnabled( true )
                .build(  )
        embeddedServer.start(  )

        eventServerUrl = URI.create("tcp://localhost:" + embeddedServer.getEventServerPort(  ).get() )
    }

    def cleanupSpec()
    {
        if( embeddedServer != null )
        {
            embeddedServer.shutdown(  )
        }
    }

    def "Create then get"()
    {
        given:
        EventObserver observer = Mock()

        when:
        Client instance = Client.newBuilder().eventServerUrl( eventServerUrl ).eventObserver( observer ).build()

        then:
        instance.getEventServerUrl() == eventServerUrl
        instance.getEventObserver() == observer
        instance.getEventClient(  ) != null
    }

    def "Missing params, throws NullPointerException"()
    {
        when:
        Client.newBuilder()
            .eventServerUrl( _url )
            .eventObserver( _observer )
            .eventClient( _client  )
        .build()

        then:
        thrown NullPointerException

        where:
        _url           | _observer             | _client
        null           | Mock( EventObserver ) | Mock( EventClient )
        eventServerUrl | null                  | Mock( EventClient )
    }

    def "Connect to event server and receive events."()
    {
        given:
        EventObserver observer = Mock()
        Client instance = Client.newBuilder().eventServerUrl( eventServerUrl ).eventObserver( observer ).build()

        CountDownLatch latch = new CountDownLatch( 2 )
        2* observer.notify( event ) >> { latch.countDown(  ) }

        when:
        embeddedServer.publishEvent( event )
        embeddedServer.publishEvent( event )
        latch.await( 50, TimeUnit.MILLISECONDS )

        then:
        latch.getCount(  ) == 0

        cleanup:
        instance.close(  )
    }

    def "Connect to non existent server, throws EventServerConnectionException"()
    {
        given:
        EventObserver observer = Mock()

        when:
        Client.newBuilder().eventServerUrl( URI.create( "tcp://127.0.0.1:12345" ) ).eventObserver( observer ).build()

        then:
        thrown EventServerConnectionException
    }

    def "Client connects, received event, shutdown event server, client disconnects."()
    {
        given:
        EventObserver observer = Mock()
        CasualEmbeddedServer server = CasualEmbeddedServer.newBuilder()
                .eventServerEnabled( true )
                .build(  )
        server.start(  )

        URI url = URI.create("tcp://localhost:" + server.getEventServerPort(  ).get() )
        Client instance = Client.newBuilder().eventServerUrl( url ).eventObserver( observer ).build(  )

        CountDownLatch latch = new CountDownLatch( 1 )
        1* observer.notify( event ) >> { latch.countDown(  ) }

        when:
        server.publishEvent( event )
        latch.await( 50, TimeUnit.MILLISECONDS )

        then:
        latch.getCount(  ) == 0

        when:
        server.shutdown(  )
        instance.waitForDisconnect()

        then:
        noExceptionThrown()

        cleanup:
        server.shutdown(  )
    }


}
