/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner

import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.ServiceCallEventStore
import se.laz.casual.event.ServiceCallEventStoreFactory
import se.laz.casual.event.service.log.cli.client.EventHandler
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class EventProcessorTest extends Specification
{
    EventHandler handler = Mock()
    UUID storeId = UUID.randomUUID(  )
    ServiceCallEventStore store = ServiceCallEventStoreFactory.getStore( storeId )

    EventProcessor instance

    def setup()
    {
        instance = new EventProcessor( store, handler )
    }

    def "Place item on the store, is processed."()
    {
        given:
        ServiceCallEvent event = Mock()
        CountDownLatch latch = new CountDownLatch(1)
        1* handler.notify( event ) >> { latch.countDown(  ) }

        when:
        store.put( event )
        latch.await( 50, TimeUnit.MILLISECONDS )

        then:
        latch.getCount(  ) == 0
    }

    def "Stop processing, things not processed anymore, remain on queue."()
    {
        given:
        instance.stop()
        ServiceCallEvent event = Mock()
        CountDownLatch latch = new CountDownLatch(1)
        handler.notify( event ) >> { latch.countDown(  ) }

        when:
        store.put( event )
        latch.await( 50, TimeUnit.MILLISECONDS )

        then:
        latch.getCount(  ) == 1

        when:
        ServiceCallEvent actual = store.take(  )

        then:
        actual == event
    }
}
