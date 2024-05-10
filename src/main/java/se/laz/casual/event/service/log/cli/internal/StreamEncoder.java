/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.internal;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Encode raw streams in the appropriate encoding UTF-8.
 */
public class StreamEncoder
{
    private StreamEncoder()
    {
    }

    /**
     * Create a reader from the input stream read UTF-8.
     *
     * @param inputStream to wrap inside a {@link Reader}
     * @return Reader with UTF-8 encoding for read.
     */
    public static Reader toReader( InputStream inputStream )
    {
        try
        {
            Objects.requireNonNull( inputStream, "InputStream is null." );
            return new InputStreamReader( inputStream, StandardCharsets.UTF_8.name() );
        }
        catch( UnsupportedEncodingException e )
        {
            throw new StreamEncodingException( "Reader creation failed.", e );
        }
    }

    /**
     * Ensure that the {@link PrintWriter} is using UTF-8 encoding.
     *
     * @param stream to wrap for UTF-8 writing.
     * @return PrintWriter with UTF-8 encoding for write.
     */
    public static PrintWriter toPrintWriter( PrintStream stream )
    {
        try
        {
            Objects.requireNonNull( stream, "PrintStream is null." );
            return new PrintWriter(new OutputStreamWriter( stream, StandardCharsets.UTF_8.name() ), true );
        }
        catch( UnsupportedEncodingException e )
        {
            throw new StreamEncodingException( "PrintWriter creation failed.", e );
        }
    }
}
