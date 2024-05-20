/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.log;

import se.laz.casual.event.ServiceCallEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Formatter for events.
 */
public class ServiceCallEventFormatter
{
    private final String delimiter;
    private final List<String> columnValues = new ArrayList<>();

    private ServiceCallEventFormatter( String delimiter )
    {
        this.delimiter = delimiter;
    }

    /**
     * Format the event using the provided delimiter.
     *
     * @param event to format.
     * @param delimiter to use when formatting.
     * @return the formatted event.
     */
    public static String format( ServiceCallEvent event, String delimiter )
    {
        Objects.requireNonNull( event, "Event is null." );
        Objects.requireNonNull( delimiter, "Delimiter is null" );
        return new ServiceCallEventFormatter( delimiter ).formatEvent( event );
    }

    private String formatEvent( ServiceCallEvent event )
    {
        appendColumn( event.getService() );
        appendColumn( event.getParent() );
        appendColumn( event.getPid() );
        appendColumn( event.getExecution() );
        appendColumn( event.getTransactionId() );
        appendColumn( event.getStart() );
        appendColumn( event.getEnd() );
        appendColumn( event.getPending() );
        appendColumn( event.getCode() );
        appendColumn( event.getOrder() );

        return String.join( delimiter, columnValues );
    }
    private void appendColumn( long data )
    {
        appendColumn( String.valueOf( data ) );
    }

    private void appendColumn( char data )
    {
        appendColumn( String.valueOf( data ) );
    }
    private void appendColumn( String data )
    {
        columnValues.add( data );
    }

}
