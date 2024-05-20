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

import java.io.File;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static se.laz.casual.event.service.log.cli.internal.StreamEncoder.toPrintWriter;

@Command(name = "casual-java-event-service-log", mixinStandardHelpOptions = true)
public class Main implements Callable<Integer>, EventServiceLogParams
{
    @Spec
    private CommandSpec spec;

    @Option( names = {"-f", "--file"}, description = "where to log (default: ${DEFAULT-VALUE})", defaultValue = "statistics.log" )
    private File logFile;
    @Option( names = {"-d", "--delimiter"}, description = "delimiter between columns (default: ${DEFAULT-VALUE})", defaultValue = "|" )
    private String logColumnDelimiter;
    @Option( names = {"--filter-inclusive"}, description = "only services that match the expression are logged" )
    private Pattern logFilterInclusive;

    @Option( names = {"--filter-exclusive"}, description = "only services that do not match the expression are logged" )
    private Pattern logFilterExclusive;
    @Option( names = {"--eventServerUrl"}, description = "event server from which to retrieve events.", required = true )
    private URI eventServerUrl;

    @Override
    public URI getEventServerUrl( )
    {
        return this.eventServerUrl;
    }

    @Override
    public File getLogFile()
    {
        return this.logFile;
    }

    @Override
    public String getLogColumnDelimiter()
    {
        return this.logColumnDelimiter;
    }

    @Override
    public Optional<Pattern> getLogFilterInclusive()
    {
        return Optional.ofNullable( this.logFilterInclusive );
    }

    @Override
    public Optional<Pattern> getLogFilterExclusive()
    {
        return Optional.ofNullable( this.logFilterExclusive );
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
        return newCommandLine( new Main() )
                .execute( args );
    }

    //2024-05-15 CK - This is a command line app, it is meant to write to System.out and System.err.
    @SuppressWarnings( "squid:S106" )
    static CommandLine newCommandLine( Main instance )
    {
        return new CommandLine( instance )
                .setOut( toPrintWriter( System.out ) )
                .setErr( toPrintWriter( System.err ) )
                .setCaseInsensitiveEnumValuesAllowed( true )
                .setTrimQuotes(true)
                ;
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