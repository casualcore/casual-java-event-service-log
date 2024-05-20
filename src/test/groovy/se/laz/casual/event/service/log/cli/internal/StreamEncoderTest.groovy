/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.internal

import spock.lang.Specification

class StreamEncoderTest extends Specification
{
    def "Create Reader with null, throws NullPointerException"()
    {
        when:
        StreamEncoder.toReader( null )

        then:
        thrown NullPointerException
    }

    def "Create PrintWriter with null, throws NullPointerException"()
    {
        when:
        StreamEncoder.toPrintWriter( null )

        then:
        thrown NullPointerException
    }

    def "Created PrintWriter is autoflush true."()
    {
        given:
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        PrintStream ps = new PrintStream( os )
        PrintWriter writer = StreamEncoder.toPrintWriter(  ps  )

        when:
        writer.println( "Hello" )

        then:
        os.toString(  ).trim(  ) == "Hello"
    }
}
