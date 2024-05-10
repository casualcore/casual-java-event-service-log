/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli

import picocli.CommandLine
import spock.lang.Specification

class MainTest extends Specification
{
    CommandLine commandLine
    Main instance = new Main()

    def setup()
    {
        commandLine = new CommandLine( instance ).setCaseInsensitiveEnumValuesAllowed( true )
    }

    def "Call with #desc throws"()
    {
        when:
        commandLine.parseArgs( args as String[] )

        then:
        thrown exception

        where:
        desc          | args          || exception
        "no args"     | []            || CommandLine.MissingParameterException
        "invalid arg" | ["--eventServerUrl=http://localhost:8080","--unknown"] || CommandLine.UnmatchedArgumentException
    }

    def "Call with #desc"()
    {
        when:
        CommandLine.ParseResult result = commandLine.parseArgs( args as String[] )

        then:
        result.errors().size() == 0
        instance.getEventServerUrl() == URI.create( exUrl )

        where:
        desc                  | args                                                  | exUrl
        "valid url no port"   | ["--eventServerUrl=http://events.casual.laz.se"]      | "http://events.casual.laz.se"
        "valid url with port" | ["--eventServerUrl=http://events.casual.laz.se:7774"] | "http://events.casual.laz.se:7774"
    }

    def "Call with invalid event server uri throws ParameterException."()
    {
        when:
        commandLine.parseArgs( args as String[] )

        then:
        thrown CommandLine.ParameterException

        where:
        desc               | args
        "event server url" | "--eventServerUrl=h://>123"
        "event server url" | "--eventServerUrl=f://_23"
    }
}
