/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli

import picocli.CommandLine
import spock.lang.Shared
import spock.lang.Specification

import java.util.regex.Pattern

class MainTest extends Specification
{
    @Shared String eUrl = "--eventServerUrl=tcp://event.casual.laz.se:7774"

    CommandLine commandLine
    Main instance = new Main()

    def setup()
    {
        commandLine = Main.newCommandLine( instance )
    }

    def "Call with #desc throws"()
    {
        when:
        commandLine.parseArgs( args as String[] )

        then:
        thrown exception

        where:
        desc          | args                || exception
        "no args"     | []                  || CommandLine.MissingParameterException
        "invalid arg" | [eUrl, "--unknown"] || CommandLine.UnmatchedArgumentException
    }

    def "Call with #desc"()
    {
        when:
        CommandLine.ParseResult result = commandLine.parseArgs( args as String[] )

        then:
        result.errors().size() == 0
        instance.getEventServerUrl() == URI.create( exUrl )
        instance.getLogFile(  ) != null
        instance.getLogColumnDelimiter(  ) == "|"
        instance.getLogFilterExclusive(  ).isEmpty(  )
        instance.getLogFilterExclusive(  ).isEmpty(  )

        where:
        desc                  | args                                                  | exUrl
        "valid url no port"   | ["--eventServerUrl=tcp://events.casual.laz.se"]      | "tcp://events.casual.laz.se"
        "valid url with port" | ["--eventServerUrl=tcp://events.casual.laz.se:7774"] | "tcp://events.casual.laz.se:7774"
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

    def "Call with #desc file"()
    {
        when:
        CommandLine.ParseResult result = commandLine.parseArgs( args as String[] )

        then:
        result.errors().size() == 0
        instance.getLogFile() == new File( exFile )

        where:
        desc             | args                       | exFile
        "missing option" | [eUrl]                     | "statistics.log"
        "simple option"  | [eUrl, "-fsomeone.log"]    | "someone.log"
        "long option"    | [eUrl, "--file=stats.log"] | "stats.log"
    }

    def "Call with #desc delimiter"()
    {
        when:
        CommandLine.ParseResult result = commandLine.parseArgs( args as String[] )

        then:
        result.errors().size() == 0
        instance.getLogColumnDelimiter(  ) == exDelimiter

        where:
        desc             | args                     | exDelimiter
        "missing option" | [eUrl]                   | "|"
        "simple option"  | [eUrl, "-d~"]            | "~"
        "long option"    | [eUrl, "--delimiter=##"] | "##"
    }

    def "Call with #desc filter-inclusive"()
    {
        when:
        CommandLine.ParseResult result = commandLine.parseArgs( args as String[] )

        then:
        result.errors().size() == 0
        instance.getLogFilterInclusive(  ).get( ).pattern(  ) == exPattern.pattern(  )

        where:
        desc     | args                                     | exPattern
        "simple" | [eUrl, "--filter-inclusive=^start"]      | Pattern.compile( "^start" )
        "group"  | [eUrl, "--filter-inclusive=^[a-z]+"]     | Pattern.compile( "^[a-z]+" )
        "quotes" | [eUrl, "--filter-inclusive=\"^[0-9]+\""] | Pattern.compile( "^[0-9]+" )
    }

    def "Call with #desc filter-inclusive"()
    {
        when:
        CommandLine.ParseResult result = commandLine.parseArgs( args as String[] )

        then:
        result.errors().size() == 0
        instance.getLogFilterExclusive(  ).get( ).pattern(  ) == exPattern.pattern(  )

        where:
        desc     | args                                     | exPattern
        "simple" | [eUrl, "--filter-exclusive=^start"]      | Pattern.compile( "^start" )
        "group"  | [eUrl, "--filter-exclusive=^[a-z]+"]     | Pattern.compile( "^[a-z]+" )
        "quotes" | [eUrl, "--filter-exclusive=\"^[0-9]+\""] | Pattern.compile( "^[0-9]+" )
    }
}
