/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.service.log.cli.client.EventHandler
import se.laz.casual.test.CasualEmbeddedServer
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import static se.laz.casual.event.service.log.cli.internal.StreamEncoder.toPrintWriter

class ClientAutoReconnectorTest extends Specification
{

    CasualEmbeddedServer embeddedServer

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

    URI eventServerUrl

    StringWriter sw = new StringWriter()
    PrintWriter writer = new PrintWriter( sw )

    EventServiceLogParams params = new TestEventServiceLogParams()

    ClientAutoReconnector instance

    ExecutorService executor = Executors.newFixedThreadPool( 1 )

    def setup()
    {
        embeddedServer = CasualEmbeddedServer.newBuilder()
                .eventServerEnabled( true )
                .build(  )
        embeddedServer.start(  )

        eventServerUrl = URI.create("tcp://localhost:" + embeddedServer.getEventServerPort(  ).get() )

        params.eventServerUrl = eventServerUrl
        EventServiceLogRunner runner = new EventServiceLogRunner( params, toPrintWriter( System.out ) )
        instance = new ClientAutoReconnector( runner, 50 )
    }

    def cleanup()
    {
        instance.stop(  )
        executor.shutdownNow(  )
        if( embeddedServer != null )
        {
            embeddedServer.shutdown(  )
        }
    }

    def "Connect initially works, no reconnect required."()
    {
        given:
        EventHandler handler = Mock()
        CountDownLatch latch = new CountDownLatch( 1 )
        handler.notify( event ) >> { latch.countDown(  ) }

        when:
        executor.submit {instance.maintainClientConnection( handler ) }
        instance.waitForConnection()
        embeddedServer.publishEvent( event )
        latch.await( 50, TimeUnit.MILLISECONDS )

        then:
        latch.getCount(  ) == 0
    }

    def "Connect initially works, disconnects, reconnect continues to log."()
    {
        given:
        EventHandler handler = Mock()
        CountDownLatch latch = new CountDownLatch( 2 )
        handler.notify( event ) >> { latch.countDown(  ) }

        when:
        executor.submit {instance.maintainClientConnection( handler ) }
        instance.waitForConnection(  )
        embeddedServer.publishEvent( event )
        embeddedServer.shutdown(  )
        embeddedServer.start(  )
        instance.waitForConnection(  )

        int count = 0;
        while( latch.getCount(  ) != 0 )
        {
            embeddedServer.publishEvent( event )
            latch.await( 50, TimeUnit.MILLISECONDS )
            count++
        }

        then:
        latch.getCount(  ) == 0
        count >= 1
    }

    def "Initially not connected, retries until connected."()
    {
        given:
        embeddedServer.shutdown(  )
        EventHandler handler = Mock()
        CountDownLatch latch = new CountDownLatch( 1 )
        handler.notify( event ) >> {
            latch.countDown(  )
        }

        when:
        executor.submit {instance.maintainClientConnection( handler ) }
        embeddedServer.start(  )
        instance.waitForConnection(  )

        int count = 0;
        while( latch.getCount(  ) != 0 )
        {
            embeddedServer.publishEvent( event )
            latch.await( 50, TimeUnit.MILLISECONDS )
            count++
        }

        then:
        latch.getCount(  ) == 0
        count >= 1
    }

    def "Immediately stop, fails IllegalStateException."()
    {
        when:
        instance.stop(  )
        instance.maintainClientConnection( Mock(EventHandler) )

        then:
        thrown IllegalStateException
    }

    def "Run then stop, then run, fails IllegalStateException."()
    {
        when:
        instance.maintainClientConnection( Mock(EventHandler) )
        instance.stop()
        instance.maintainClientConnection( Mock(EventHandler) )

        then:
        thrown IllegalStateException
    }

}
