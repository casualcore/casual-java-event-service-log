/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.service.log.cli;

/**
 * Run commands with a provided parameters of type T
 * @param <T> parameters type.
 */
public interface CommandRunner<T>
{
    /**
     * Get the parameters associated with this runner.
     * @return parameters of the runner.
     */
    T getParams();

    /**
     * Run the command.
     * @return exit code from running command.
     */
    int run();
}
