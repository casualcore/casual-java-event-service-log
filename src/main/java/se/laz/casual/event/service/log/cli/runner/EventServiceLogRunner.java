/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner;

import se.laz.casual.event.service.log.cli.CommandRunner;

import java.io.PrintWriter;
import java.util.Objects;

public class EventServiceLogRunner implements CommandRunner<EventServiceLogParams>
{
    private final EventServiceLogParams params;
    private final PrintWriter outputStream;

    public EventServiceLogRunner( EventServiceLogParams params, PrintWriter outputStream )
    {
        Objects.requireNonNull( params, "Params is null." );
        Objects.requireNonNull( outputStream, "Output stream is null." );
        this.params = params;
        this.outputStream = outputStream;
    }

    @Override
    public EventServiceLogParams getParams( )
    {
        return this.params;
    }

    @Override
    public int run()
    {
        outputStream.print( "Hello" );
        outputStream.flush();
        return 0;
    }
}
