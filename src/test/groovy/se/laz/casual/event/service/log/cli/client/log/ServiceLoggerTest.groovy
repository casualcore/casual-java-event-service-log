/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client.log

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.util.PrettyPrinter
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.service.log.cli.runner.EventServiceLogParams
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.nio.file.Files
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class ServiceLoggerTest extends Specification
{
    File logFile

    String delimiter = "|"

    ServiceLogger instance

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

    def setup()
    {
        logFile = Files.createTempFile( "stats", "log" ).toFile(  )

        TestParams params = new TestParams()
        params.logFile = logFile

        instance = ServiceLogger.newBuilder()
                .eventServiceLogParams( params )
                .build()
    }

    def "Write event to log file."()
    {
        String expected = "test1|parent|123|"+ PrettyPrinter.casualStringify( execution1 )+"|null:null:0|1713184496123456|1713184504123456|5|OK|C" + System.lineSeparator(  )

        when:
        instance.logEvent( event )
        String fileContents = new String( logFile.getBytes(  ) )

        then:
        fileContents == expected
    }

    def "Write event null, throws NullPointerException."()
    {
        when:
        instance.logEvent( null )

        then:
        thrown NullPointerException
    }

    class TestParams implements EventServiceLogParams
    {
        URI eventServerUrl = null
        File logFile = new File( "statistics.log" )
        String logColumnDelimiter = "|"
        Pattern logFilterInclusive = null
        Pattern logFilterExclusive = null

        TestParams()
        {

        }

        TestParams( File file, String delimiter, Pattern include, Pattern exclude )
        {
            this.logFile = file
            this.logColumnDelimiter = delimiter
            this.logFilterInclusive = include
            this.logFilterExclusive = exclude
        }

        @Override
        Optional<Pattern> getLogFilterInclusive()
        {
            return Optional.ofNullable( logFilterInclusive )
        }

        @Override
        Optional<Pattern> getLogFilterExclusive()
        {
            return Optional.ofNullable( logFilterExclusive )
        }
    }
}
