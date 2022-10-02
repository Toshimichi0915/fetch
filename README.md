# fetch

A placeholder API expansion for fetching data from a specified URL.

## Usage

```
%fetch_ name cache url %
```

| Name | Description |
| --- | --- |
| name | The name of this data, which will be used for caching data. |
| cache | The cache duration (tick).|
| url | The URL to fetch data from. |

## Example

```
%fetch_ serverStatus 72000 https://getserverdata.com %
```

This example will fetch data from `https://getserverdata.com`, save it as serverStatus, and the cache will last for 1 hour (72000 = 20 (tick) * 60 (seconds) * 60 (minutes)).
