/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli.runner;

import java.net.URI;

public interface EventServiceLogParams
{
    /**
     * URI for the event server, from which to retrieve events.
     *
     * @return event server uri.
     */
    URI getEventServerUrl();
}
