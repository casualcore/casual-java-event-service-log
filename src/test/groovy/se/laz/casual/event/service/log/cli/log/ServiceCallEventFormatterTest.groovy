/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.log

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.util.PrettyPrinter
import se.laz.casual.api.util.time.InstantUtil
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ServiceCallEventFormatterTest extends Specification
{
    @Shared UUID uuid = UUID.randomUUID(  )
    @Shared String uuidString = PrettyPrinter.casualStringify( uuid )
    @Shared Xid xid = Mock(Xid)
    @Shared String xidString = PrettyPrinter.casualStringify( xid )
    @Shared ErrorState ok = ErrorState.OK
    @Shared Order C = Order.CONCURRENT
    @Shared Order S = Order.SEQUENTIAL
    @Shared ErrorState fail = ErrorState.TPESVCFAIL
    @Shared Instant start1 = ZonedDateTime.parse( "2024-04-15T12:34:56.123456Z", DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant()
    @Shared Instant end1 = ZonedDateTime.parse( "2024-04-15T12:35:04.123456Z", DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant()
    @Shared long startLong1 = InstantUtil.toEpochMicro( start1 )
    @Shared long endLong1 = InstantUtil.toEpochMicro( end1 )

    def "Format events."()
    {
        given:
        ServiceCallEvent event = ServiceCallEvent.createBuilder(  )
            .withService( service )
            .withParent( parent )
            .withPID( pid )
            .withExecution( execution )
            .withTransactionId( trid )
            .withStart( start )
            .withEnd( end )
            .withPending( pending )
            .withCode( code )
            .withOrder( order )
            .build()

        when:
        String actual = ServiceCallEventFormatter.format( event, delimiter )

        then:
        actual == expected

        where:
        service | parent | pid | execution | trid | start | end | pending | code | order | delimiter || expected
        "test1" | "" | 123 | uuid | xid | start1 | end1 | 5 | ok | C | "|" || "test1||123|" + uuidString + "|" + xidString + "|" + startLong1 + "|" + endLong1 + "|5|OK|C"
        "test2" | "parent" | 123 | uuid | xid | start1 | end1 | 15 | fail | S | "|" || "test2|parent|123|" + uuidString + "|" + xidString + "|" + startLong1 + "|" + endLong1 + "|15|TPESVCFAIL|S"
    }

}
