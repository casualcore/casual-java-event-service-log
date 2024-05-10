/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import se.laz.casual.event.service.log.cli.runner.EventServiceLogParams;
import se.laz.casual.event.service.log.cli.runner.EventServiceLogRunner;

import java.net.URI;
import java.util.concurrent.Callable;

import static se.laz.casual.event.service.log.cli.internal.StreamEncoder.toPrintWriter;

@Command(name = "casual-java-event-service-log", mixinStandardHelpOptions = true)
public class Main implements Callable<Integer>, EventServiceLogParams
{
    @Spec
    private CommandSpec spec;

    @Option( names = "--eventServerUrl", description = "Your name.", required = true )
    private URI eventServerUrl;

    @Override
    public URI getEventServerUrl( )
    {
        return this.eventServerUrl;
    }

    public static void main( String[] args )
    {
        System.exit( execute( args ) );
    }

    /**
     * Initialise the picocli setup with the provided command line args.
     * Then execute, which in turn parses the arguments and if successful,
     * invokes the {@link #call()} method.
     */
    private static int execute( String[] args )
    {
        return new CommandLine( new Main() )
                .setOut( toPrintWriter( System.out ) )
                .setErr( toPrintWriter( System.err ) )
                .setCaseInsensitiveEnumValuesAllowed( true )
                .execute( args );
    }

    /**
     * This method is called once picocli has completed its work parsing the
     * provided command line args.
     * This will only be called with valid arguments.
     * Invalid arguments will already have resulted in a failure in {@link #execute(String[])}
     * and will therefore not have been called.
     *
     * @return command line return code to represent status of running the command.
     */
    @Override
    public Integer call()
    {
        return new EventServiceLogRunner( this, spec.commandLine().getOut() ).run();
    }

}