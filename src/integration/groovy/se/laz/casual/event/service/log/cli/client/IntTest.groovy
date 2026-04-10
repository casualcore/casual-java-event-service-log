/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.ServiceCallEventStoreFactory
import se.laz.casual.event.service.log.cli.log.EventHandler
import se.laz.casual.event.service.log.cli.log.ServiceCallEventFormatter
import se.laz.casual.event.service.log.cli.log.ServiceLogger
import se.laz.casual.event.service.log.cli.runner.EventStoreProcessor
import se.laz.casual.event.service.log.cli.runner.TestEventServiceLogParams
import se.laz.casual.jca.SpanId
import se.laz.casual.test.CasualEmbeddedServer
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * verifies that events on the wire as JSON
 * are handled correctly, both for versions with and without tracing information
 */
class IntTest extends Specification
{
    @Shared CasualEmbeddedServer embeddedServer
    @Shared String service1 = "a nifty service"
    @Shared String parent1 = "bob"
    @Shared String spanId = SpanId.of().asHex()
    @Shared String parentSpanId = SpanId.of().asHex()
    @Shared long userDefinedCode = 42L
    @Shared int pid1 = 123
    @Shared UUID execution1 = UUID.randomUUID()
    @Shared Xid transactionId1 = Mock(Xid){
       getGlobalTransactionId() >> {
          'gtrid' as byte[]
       }
       getBranchQualifier() >> {
          'btrid' as byte[]
       }
       getFormatId() >> {
          42
       }
    }
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

   @Shared ServiceCallEvent eventWithTracing = ServiceCallEvent.createBuilder(  )
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
           .withSpanId(spanId)
           .withParentSpanId(parentSpanId)
           .withUserCode(userDefinedCode)
           .build()

    @Shared URI eventServerUrl
    @Shared File tempFile
    @Shared String delimiter = "|"
    @Shared EventStoreProcessor storeProcessor
    @Shared Client client

    def setupSpec()
    {
        embeddedServer = CasualEmbeddedServer.newBuilder()
                .eventServerEnabled( true )
                .build(  )
        embeddedServer.start(  )

        eventServerUrl = URI.create("tcp://localhost:" + embeddedServer.getEventServerPort(  ).get() )

        tempFile = File.createTempFile( "test-events", ".log" )
        tempFile.deleteOnExit(  )

        def params = new TestEventServiceLogParams( eventServerUrl, tempFile, delimiter, null, null )
        def store = ServiceCallEventStoreFactory.getStore( UUID.randomUUID(  ) )
        def logger = ServiceLogger.newBuilder(  ).eventServiceLogParams( params ).build(  )
        def handler = EventHandler.newBuilder(  ).serviceLogger( logger ).build(  )
        storeProcessor = new EventStoreProcessor( store, handler )

        client = Client.newBuilder()
                .eventServerUrl( eventServerUrl )
                .eventObserver( store::put )
                .build()
    }

    def cleanupSpec()
    {
        storeProcessor?.stop(  )
        client?.close(  )
        if( embeddedServer != null )
        {
            embeddedServer.shutdown(  )
        }
    }

    def "Events are published and written to file with correct content"()
    {
        when:
        embeddedServer.publishEvent( event )
        embeddedServer.publishEvent( eventWithTracing )

        // poll until events are written to file
        def lines = []
        while( lines.size(  ) < 2)
        {
            Thread.sleep( 100 )
            lines = tempFile.readLines(  )
        }

        then:
        lines.size(  ) == 2

        and: 'event without tracing data matches expected format'
        def expectedEvent = ServiceCallEventFormatter.format( event, delimiter )
        lines[0] == expectedEvent
        println("line 1: ${lines[0]}")

        and: 'event with tracing data matches expected format'
        def expectedEventWithTracing = ServiceCallEventFormatter.format( eventWithTracing, delimiter )
        lines[1] == expectedEventWithTracing
        println("line 2: ${lines[1]}")
    }

}
