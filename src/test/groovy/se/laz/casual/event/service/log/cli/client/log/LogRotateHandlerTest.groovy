/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.client.log


import spock.lang.IgnoreIf
import spock.lang.Specification
import sun.misc.Signal

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@IgnoreIf({os.windows})
class LogRotateHandlerTest extends Specification
{
    ServiceLogger serviceLogger  = Mock()
    LogRotateHandler instance


    def setup()
    {
        instance = LogRotateHandler.newBuilder().serviceLogger( serviceLogger ).build()
    }

    def "Create and get."()
    {
        expect:
        instance.getServiceLogger() == serviceLogger
    }

    def "Create no logger, throws NullPointerException"()
    {
        when:
        LogRotateHandler.newBuilder(  ).build(  )

        then:
        thrown NullPointerException
    }

    def "When the sighup handler is called, it reloads the logger."()
    {
        when:
        instance.handle( LogRotateHandler.SIGHUP )

        then:
        1* serviceLogger.reload()
    }

    def "When sighup is raised, it reloads the logger."()
    {
        given:
        // NB: Using a latch here, as the Signal#raise appears to be processed in another thread?
        // Without the await, spock incorrectly fails the test due to a missing interactions with the mock.
        CountDownLatch called = new CountDownLatch(1 )
        1* serviceLogger.reload() >> { called.countDown() }

        when:
        Signal.raise( LogRotateHandler.SIGHUP )
        called.await( 1, TimeUnit.SECONDS )

        then:
        called.getCount() == 0
    }
}
