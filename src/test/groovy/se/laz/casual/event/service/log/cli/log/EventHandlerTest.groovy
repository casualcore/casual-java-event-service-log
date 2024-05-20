/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.log

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class EventHandlerTest extends Specification
{
    Pattern inclusive = Pattern.compile( "^[s]" )
    Pattern exclusive = Pattern.compile( "[a]?" )

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
    ServiceCallEvent event = createEvent( service1 )

    ServiceLogger logger
    EventHandler instance

    def setup()
    {
        logger = Mock()
        instance = EventHandler.newBuilder()
                .serviceLogger( logger )
                .filterInclusive( inclusive )
                .filterExclusive( exclusive )
                .build()
    }

    def "Create then get."()
    {
        expect:
        instance.getFilterInclusive() == inclusive
        instance.getFilterExclusive() == exclusive
        instance.getServiceLogger() == logger
    }

    def "Create without logger, throws NullPointerException"()
    {
        when:
        EventHandler.newBuilder()
                .filterInclusive( inclusive )
                .filterExclusive( exclusive )
                .build()

        then:
        thrown NullPointerException
    }

    def "Filters checks"()
    {
        given:
        ServiceLogger _logger = Mock()
        Pattern inc = _inclusive == null ? null : Pattern.compile( _inclusive )
        Pattern exc = _exclusive == null ? null : Pattern.compile( _exclusive )
        instance = EventHandler.newBuilder()
                .serviceLogger( _logger )
                .filterInclusive( inc )
                .filterExclusive( exc )
                .build()
        ServiceCallEvent event = createEvent( name )

        when:
        instance.handle( event )

        then:
        expected * _logger.logEvent( event )

        where:
        name            | _inclusive    | _exclusive    | expected
        "service"       | null          | null          | 1
        "tervice"       | null          | null          | 1
        "internal.fred" | null          | null          | 1
        "service"       | "^s.*"        | null          | 1
        "tervice"       | "^s.*"        | null          | 0
        "internal.fred" | "^s.*"        | null          | 0
        "service"       | ".*[e]+.*"    | null          | 1
        "tervice"       | ".*[vice]+.*" | null          | 1
        "internal.fred" | "^internal.*" | null          | 1
        "service"       | null          | "^t.*"        | 1
        "tervice"       | null          | "^t.*"        | 0
        "internal.fred" | null          | "^t.*"        | 1
        "service"       | null          | "^internal.*" | 1
        "tervice"       | null          | "^s.*"        | 1
        "internal.fred" | null          | ".*"          | 0
        "service"       | "^s.*"        | "^t.*"        | 1
        "tervice"       | "^t.*"        | "^t.*"        | 0
        "internal.fred" | ".*[.]+.*"    | "^z.*"        | 1
        "service"       | "^s.*"        | ".*[e]+.*"    | 0
        "tervice"       | "^s.*"        | ".*[z]+.*"    | 0
        "internal.fred" | ".*[_]+.*"    | "^internal.*" | 0
    }

    def "notify null event, throws NullPointerException."()
    {
        when:
        instance.handle( null )

        then:
        thrown NullPointerException
    }

    ServiceCallEvent createEvent( String serviceName )
    {
        return ServiceCallEvent.createBuilder(  )
                .withService(serviceName)
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
    }
}
