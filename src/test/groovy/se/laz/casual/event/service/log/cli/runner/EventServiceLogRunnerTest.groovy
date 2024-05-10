/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner

import spock.lang.Specification

class EventServiceLogRunnerTest extends Specification
{
    StringWriter sw = new StringWriter()
    PrintWriter writer = new PrintWriter( sw )

    def "Set and retrieve params"()
    {
        given:
        EventServiceLogParams params = Mock( EventServiceLogParams )
        EventServiceLogRunner instance = new EventServiceLogRunner( params, writer )

        when:
        EventServiceLogParams actual = instance.getParams(  )

        then:
        actual == params
    }

    def "Set params null, throws NullPointerException"()
    {
        when:
        new EventServiceLogRunner( _params, _writer )

        then:
        thrown NullPointerException

        where:
        _params                       | _writer
        null                          | new PrintWriter( new StringWriter() )
        Mock( EventServiceLogParams ) | null
    }
}
