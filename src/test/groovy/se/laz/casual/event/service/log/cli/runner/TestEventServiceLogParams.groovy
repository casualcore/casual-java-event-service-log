/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner

import java.util.regex.Pattern

class TestEventServiceLogParams implements EventServiceLogParams
{
    URI eventServerUrl = null
    File logFile = new File( "statistics.log" )
    String logColumnDelimiter = "|"
    Pattern logFilterInclusive = null
    Pattern logFilterExclusive = null

    TestEventServiceLogParams()
    {

    }

    TestEventServiceLogParams( URI eventServerUrl, File file, String delimiter, Pattern include, Pattern exclude )
    {
        this.eventServerUrl = eventServerUrl
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
