/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.test.CasualEmbeddedServer
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Ignore
class Client2Test extends Specification
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

        eventServerUrl = URI.create("http://localhost:" + embeddedServer.getEventServerPort(  ).get() )
    }

    def cleanupSpec()
    {
        if( embeddedServer != null )
        {
            embeddedServer.shutdown(  )
        }
    }

    def "Send some events."()
    {
        when:
        for( int i=0; i<=20; i++ )
        {
            embeddedServer.publishEvent( event )
            Thread.sleep( 5000 )
        }

        then:
        noExceptionThrown(  )
    }
}
