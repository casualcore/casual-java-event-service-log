/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.util.time.InstantUtil
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.test.CasualEmbeddedServer
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Currently these tests are manually run to allow testing of different scenarios.
 * They just start an embedded event server and push events.
 * The client needs to be manually run and monitored to check the results.
 */
class ClientIntTest extends Specification
{
    @Shared CasualEmbeddedServer embeddedServer

    @Shared String service1 = "test1"
    @Shared String parent1 = "parent"
    @Shared int pid1 = 123
    @Shared UUID execution1 = UUID.randomUUID()
    @Shared Xid transactionId1 = Mock(Xid)
    @Shared long pending1 = 5L
    @Shared ErrorState code1 = ErrorState.OK
    @Shared Order order1 = Order.CONCURRENT

    @Shared Instant start1 = ZonedDateTime.parse( "2024-04-15T12:34:56.123456Z", DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant()
    @Shared Instant end1 = ZonedDateTime.parse( "2024-04-15T12:35:04.123456Z", DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant()
    @Shared ServiceCallEvent event = ServiceCallEvent.createBuilder(  )
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
                .eventServerPort( 7774 )
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

    def "Send some events no restarting."()
    {
        System.out.println( "Connect to me.")
        Thread.sleep( 10000 )
        System.out.println( "Firing away...")

        when:
        Instant start = Instant.now()
        for( int i=1; i<=1000; i++ )
        {
            embeddedServer.publishEvent( event )
            Thread.sleep( 10 )
        }
        Instant end = Instant.now()
        System.out.println( "Duration" + InstantUtil.toDurationMicro( start, end ) )
        Thread.sleep( 10000 )

        then:
        noExceptionThrown(  )
    }

    def "Send some events with restarting."()
    {
        System.out.println( "Connect to me.")
        Thread.sleep( 10000 )
        System.out.println( "Firing away...")

        when:
        Instant start = Instant.now()
        for( int i=1; i<=2000; i++ )
        {
            embeddedServer.publishEvent( event )
            Thread.sleep( 5000 )
            if( i % 10 == 0 )
            {
                System.out.println( "shutting down.")
                embeddedServer.shutdown(  )
                Thread.sleep( 20000 )
                embeddedServer.start(  )
                System.out.println( "restarting." )
            }
        }
        Instant end = Instant.now()
        System.out.println( "Duration" + InstantUtil.toDurationMicro( start, end ) )
        Thread.sleep( 10000 )

        then:
        noExceptionThrown(  )
    }

    def "Send events fast."()
    {
        System.out.println( "Connect to me.")
        Thread.sleep( 10000 )
        System.out.println( "Firing away...")

        when:
        Instant start = Instant.now()
        for( int i=1; i<=100000; i++ )
        {
            embeddedServer.publishEvent( event )
            //Thread.sleep( 1 )
        }
        Instant end = Instant.now()
        System.out.println( "Duration" + InstantUtil.toDurationMicro( start, end ) )
        Thread.sleep( 10000 )

        then:
        noExceptionThrown(  )
    }
}
