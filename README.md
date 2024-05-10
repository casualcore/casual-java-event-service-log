# casual-java-event-service-log

Provides metrics for all service calls that har invoked within a casual java domain.

Provides same functionality for casual java domains
as: http://casual.laz.se/documentation/en/1.6/middleware/event/documentation/service.log.html

Metrics in a casual java domain are provided by an Event Server to which the tools is connected via an http url.

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

