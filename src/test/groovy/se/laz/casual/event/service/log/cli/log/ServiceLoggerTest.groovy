/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.log

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.util.PrettyPrinter
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.service.log.cli.runner.TestEventServiceLogParams
import spock.lang.IgnoreIf
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.nio.file.Files
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE

class ServiceLoggerTest extends Specification
{
    File logFile

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
    ServiceCallEvent event = createEvent( service1 )

    TestEventServiceLogParams params

    def setup()
    {
        logFile = Files.createTempFile( "stats", "log" ).toFile(  )

        params = new TestEventServiceLogParams()
        params.logFile = logFile

        instance = ServiceLogger.newBuilder()
                .eventServiceLogParams( params )
                .build()
    }

    def "Write event to log file."()
    {
        given:
        String expected = "test1|parent|123|"+ PrettyPrinter.casualStringify( execution1 )+"|null:null:0|1713184496123456|1713184504123456|5|OK|C" + System.lineSeparator(  )

        when:
        instance.logEvent( event )
        String fileContents = new String( logFile.getBytes(  ) )

        then:
        fileContents == expected
    }

    def "Write event to log file, delimiter ~"()
    {
        given:
        params.logColumnDelimiter = "~"
        instance = ServiceLogger.newBuilder(  ).eventServiceLogParams( params ).build(  )
        String expected = "test1~parent~123~"+ PrettyPrinter.casualStringify( execution1 )+"~null:null:0~1713184496123456~1713184504123456~5~OK~C" + System.lineSeparator(  )

        when:
        instance.logEvent( event )
        String fileContents = new String( logFile.getBytes(  ) )

        then:
        fileContents == expected
    }

    def "Write event to existing log file, appends"()
    {
        given:
        String expected1 = "test1|parent|123|"+ PrettyPrinter.casualStringify( execution1 )+"|null:null:0|1713184496123456|1713184504123456|5|OK|C" + System.lineSeparator(  )
        String expected2 = expected1 + expected1

        when:
        ServiceLogger logger = ServiceLogger.newBuilder(  ).eventServiceLogParams( params ).build(  )
        logger.logEvent( event )
        String fileContents = new String( logFile.getBytes(  ) )

        then:
        fileContents == expected1

        when:
        logger = ServiceLogger.newBuilder(  ).eventServiceLogParams( params ).build(  )
        logger.logEvent( event )
        String fileContents2 = new String( logFile.getBytes(  ) )

        then:
        fileContents2 == expected2
    }

    def "Write event null, throws NullPointerException."()
    {
        when:
        instance.logEvent( null )

        then:
        thrown NullPointerException
    }

    @IgnoreIf( {os.windows} )
    def "Write event to log, move file and restart logger, writes to new file."()
    {
        given:
        String expected1 = "test1|parent|123|"+ PrettyPrinter.casualStringify( execution1 )+"|null:null:0|1713184496123456|1713184504123456|5|OK|C" + System.lineSeparator(  )
        String expected2 = expected1 + expected1

        when: "log file is written to and rotated, subsequent writes still update but in the new location."
        instance.logEvent( event )

        File rotatedLogFile = Files.createTempFile( "stats", "logrotated" ).toFile(  )
        Files.move( logFile.toPath(  ), rotatedLogFile.toPath(  ), ATOMIC_MOVE )

        instance.logEvent( event )
        String rotatedLogFileContents = new String( rotatedLogFile.getBytes(  ) )

        then:
        rotatedLogFileContents == expected2

        when:
        instance.reload()
        instance.logEvent( event )
        String logFileContents = new String( logFile.getBytes(  ) )
        String rotatedLogFileContents2 = new String( rotatedLogFile.getBytes(  ) )

        then:
        rotatedLogFileContents2 == expected2
        logFileContents == expected1
    }

    def "Reload without file move, continues to write to the same file."()
    {
        given:
        String expected1 = "test1|parent|123|"+ PrettyPrinter.casualStringify( execution1 )+"|null:null:0|1713184496123456|1713184504123456|5|OK|C" + System.lineSeparator(  )
        String expected2 = expected1 + expected1

        when: "log file is written to and rotated, subsequent writes still update but in the new location."
        instance.logEvent( event )
        instance.reload()
        instance.logEvent( event )
        String logFileContents = new String( logFile.getBytes(  ) )

        then:
        logFileContents == expected2
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
