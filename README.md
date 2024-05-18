# casual-java-event-service-log

Provides metrics for all service calls that ar invoked within a casual java domain.

Provides same functionality for casual java domains
as: http://casual.laz.se/documentation/en/1.6/middleware/event/documentation/service.log.html

Metrics in a casual java domain are provided by an Event Server to which the tools is connected via a tcp url.

## configuration¶
```shell
Usage: casual-java-event-service-log [-hV] [-d=<logColumnDelimiter>]
                                     --eventServerUrl=<eventServerUrl>
                                     [-f=<logFile>]
                                     [--filter-exclusive=<logFilterExclusive>]
                                     [--filter-inclusive=<logFilterInclusive>]
  -d, --delimiter=<logColumnDelimiter> delimiter between columns (default: |)
      --eventServerUrl=<eventServerUrl> event server from which to retrieve events.
  -f, --file=<logFile>   where to log (default: statistics.log)
      --filter-exclusive=<logFilterExclusive> only services that do not match the expression are logged
      --filter-inclusive=<logFilterInclusive> only services that match the expression are logged
  -h, --help             Show this help message and exit.
  -V, --version          Print version information and exit.
```

## example¶
```shell
casual-java-event-service-log --file= logs/service.log
```

## log format¶
Columns are separated by the provided delimiter option (default |)

| column    | format    | description                                                                                                     |
|-----------|-----------|-----------------------------------------------------------------------------------------------------------------|
| service   | string    | name of the invoked service                                                                                     |
| parent    | string    | name of the parent service, if any.                                                                             |
| pid       | integer   | process id of the invoked instance                                                                              |
| execution | uuid      | unique execution id, like breadcrumbs                                                                           |
| trid      | xid       | transaction id                                                                                                  |
| start     | integer   | when the service was invoked, `us` since epoch.                                                                 |
| end       | integer   | when the service was done, `us` since epoch                                                                     |
| pending   | integer   | how long caller had to wait for a non busy server, in `us`                                                      |
| code      | string    | outcome of the service call. `OK` if ok, otherwise the reported error from the service                          |
| order     | character | “order” of the service - sequential or concurrent, denoted by `S` or `C`. `S` reserves a process, `C` does not. |

## example¶

```
some/service|some/parent/service|9585|ff75bcc6ef1b4d1c8ae8d58ee0918f81|3d7519f801e4f65a127d9ac09fa159d:b81a4d8715ad44e8afccb796a02fd77f:42:123|1670372749162496|1670372749162723|0|OK|S
```

## Usage

NB - Although this is a java tool, it can only run on linux environment due to the usage of signals for log rotation.

Initial release of this application is to be as an "uber-jar" i.e. a single self-contained jar containing all required dependencies.

The uber-jar can be built from source with the following command.

```shell
./gradlew clean build
```

The resulting jar file will be located in `./build/casual-java-event-service-log-<VERSION>.jar`.

Alternatively the uber-jar will be published to maven central and can be downloaded directly from there.

e.g. 
https://s01.oss.sonatype.org/content/repositories/snapshots/se/laz/casual/casual-java-event-service-log/0.0.1-SNAPSHOT/

https://s01.oss.sonatype.org/content/repositories/snapshots/se/laz/casual/casual-java-event-service-log/0.0.1-SNAPSHOT/casual-java-event-service-log-0.0.1-20240516.131741-4.jar

NB - An uber-jar is only expected to be run without additional jars on the classpath to avoid dependency conflicts. It should not be used as
a dependency for other code. Rather the normal artifacts for this code base are also published and available with the traditional classifiers: javadoc, sources, jar.

To run the application you can then run the uber-jar, providing the necessary arguments as detailed above, for example:

```shell
java -jar <path-to/uber.jar> --eventServerUrl=tcp://<server>:<port>
```

### Shell wrapper
If you wish to wrap this, you can use the following example shell script.

casual-java-event-service-log.sh:
```shell
#!/bin/bash
java -jar ./casual-java-event-service-log-0.0.1.jar $@
```
NB - ensure that the jar file location is correct.

Make the shell script executable:

```shell
chmod +x casual-java-event-service-log.sh
```

Example output:
```shell
$ ./casual-java-event-service-log.sh --eventServerUrl=tcp://127.0.0.1:7774
--file: statistics.log
--delimiter: |
--filter-inclusive:
--filter-exclusive:
--eventServerUrl: tcp://127.0.0.1:7774
Connected to: tcp://127.0.0.1:7774.
```

## Event Server Connection
The connection to the event server is continuously maintained whilst running.
In the event of disconnection the server will retry the connection after 30 seconds.

Though this client tool requires a connection to the event server to receive events.
There is no requirement that the event server is running prior to starting this tool.
Upon startup it will just continuously attempt to connect every 30 seconds until successful.

To stop the tool, just perform, `Ctrl+C`.

### Example

The following is example output from running when the event server is initial not running:

```shell
./casual-java-event-service-log.sh --eventServerUrl=tcp://127.0.0.1:7774
--file: statistics.log
--delimiter: |
--filter-inclusive:
--filter-exclusive:
--eventServerUrl: tcp://127.0.0.1:7774
Connection failed, retrying in 30000ms: Failed to connect to event server at: tcp://127.0.0.1:7774
Connected to: tcp://127.0.0.1:7774.
Disconnected from: tcp://127.0.0.1:7774, retrying in 30000ms.
Connected to: tcp://127.0.0.1:7774.
```

In this example, we see an initial connection failure. After the event server was started, we can
see that the client successfully connected.

The event server was then manually stopped, shown with the Disconnected output.

It was finally restarted, with the client successfully reconnecting.

## Log file rotation

It is possible to seamlessly rotate the logs whilst running the client application.

The process for log file rotation is as follows:

* Perform a move upon the existing log file.
* Raise a SIGHUP upon the running client.
NB - if using the shell wrapper that the SIGHUP must be raised upon the child java process, not the shell wrapper.

After performing the SIGHUP a new log file with the original name will now receive the new events.
Whilst the moved log file will contain all the log entries prior to raising the SIGHUP.

### Example

Initially the log file:
```shell
-rw-rw-r-- 1 ck ck     1030 maj 16 13:37 statistics.log
```

Move the file:
```shell
mv statistics.log statistics.log.rotated
```

The file is moved and still being written to, until the SIGHUP is raised.

```shell
-rw-rw-r-- 1 ck ck     4429 maj 16 13:38 statistics.log.rotated
```

Get the pid for raising the SIGHUP.
Note - if using the shell wrapper that the sighup must be raised upon the child java process, not the shell wrapper.
```shell
ps -ef | grep casual-java-event-service-log | grep "java -jar"
```
Output:
```shell
ck        517785  517784  7 13:37 pts/2    00:00:01 java -jar ./casual-java-event-service-log-0.0.1-runner.jar --eventServerUrl=tcp://192.168.68.117:7774
```

Raise SIGHUP:
```shell
kill -n 1 517785
```
There are now two files:
```shell
-rw-rw-r-- 1 ck ck      309 maj 16 13:39 statistics.log
-rw-rw-r-- 1 ck ck     6077 maj 16 13:38 statistics.log.rotated
```

The `statistics.log` file continues to receive new updates whilst the `statistics.log.rotated` file has all the 
events logged prior to raising the SIGHUP.

The log file has therefore been successfully rotated with no missing event log entries.

## Design

The code is made up of four key components.

* The client is responsible for maintaining a connection to the event server and receiving events, placing them in an event store for processing.
* The event store processor is responsible for retrieving events from the event store, passing them on to the event handler.
* The event handler is responsible for filtering the events and sending them to the logger.
* The logger is responsible for formatting the events and saving them to the log file.

This separation allows for incoming events received over the network to be quickly placed on a queue (event store) for further processing.
Whilst the slower operation of writing to the log file will be queued, eventually all loggable events will be written to the log.
This prevents the log writing from slowing down network reads which could cause new events to be rejected under high load.

Testing has shown that the application can process more than 100K events per second.