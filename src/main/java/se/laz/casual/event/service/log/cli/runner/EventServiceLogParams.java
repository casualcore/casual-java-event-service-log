/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner;

import java.io.File;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

public interface EventServiceLogParams
{
    /**
     * URI for the event server, from which to retrieve events.
     *
     * @return event server uri.
     */
    URI getEventServerUrl();

    /**
     * Get the file to use for output logging.
     *
     * @return log file.
     */
    File getLogFile();

    /**
     * Get the delimiter between columns.
     *
     * @return column delimiter.
     */
    String getLogColumnDelimiter( );

    /**
     * Regex for inclusive filter for logging services that matches the expression.
     *
     * @return inclusive filter regex.
     */
    Optional<Pattern> getLogFilterInclusive();

    /**
     * Regex for exclusive filter for logging services that do not match the expression.
     *
     * @return exclusive filte regex.
     */
    Optional<Pattern> getLogFilterExclusive();
}
