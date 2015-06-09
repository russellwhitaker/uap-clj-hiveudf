# uap-clj-hiveudf

An Apache Hadoop Hive GenericUDF wrapper around the [`uap-clj`](https://github.com/russellwhitaker/uap-clj) library providing Browser, O/S, and Device field extraction functions.

##Setup

###Running the test suite (for developers)

This project uses [`speclj`](http://speclj.com). The core test suite comprises almost entirely test generators built from reading in test fixtures from the [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) repository, which themselves are pulled into the local workspace as dependencies using [`tobyhede/lein-git-deps`](https://github.com/tobyhede/lein-git-deps).

```bash
→ lein clean && lein spec --reporter=c
Compiling uap-clj.udf.hive.generic.browser
Compiling uap-clj.udf.hive.generic.common
Compiling uap-clj.udf.hive.generic.device
Compiling uap-clj.udf.hive.generic.os

Ran 53383 tests containing 53383 assertions.
0 failures, 0 errors.
```

###Deploy artifact(s) to Hadoop

```bash
→ lein clean && lein uberjar
Retrieving uap-clj/uap-clj/1.0.0/uap-clj-1.0.0.pom from clojars
Retrieving uap-clj/uap-clj/1.0.0/uap-clj-1.0.0.jar from clojars
Compiling uap-clj.udf.hive.generic.browser
Compiling uap-clj.udf.hive.generic.common
Compiling uap-clj.udf.hive.generic.device
Compiling uap-clj.udf.hive.generic.os
Created /Users/<username>/dev/uap-clj-hiveudf/target/uap-clj-hiveudf-1.0.0.jar
Created /Users/<username>/dev/uap-clj-hiveudf/target/uap-clj-hiveudf-1.0.0-standalone.jar
```

Copy one or both of these artifacts to a preferred location in HDFS (e.g. `hdfs:///shared/jars`).
The standalone version is huge; the other version tiny. The latter is preferred assuming you have
all the prerequisite dependencies (specified in `project.clj`) in your classpath. In the example below,
we'll be assuming the standalone version for the purposes of initially trying out the UDF while
minimizing setup hassle.

###Java and Hive version dependencies

This code has been tested using the `hive` commandline client versions v0.12.0, v0.14.0, and v1.0.0, and the `beeline` HiveServer2 client used in the examples here. The deployed hadoop artifact itself compiles and runs under either Java v1.7 or v1.8 (tested with OpenJDK).

## Use

```sql
→ beeline
[SNIP]
Connected to: Apache Hive (version 0.14.0.2.2.0.0-2041)
Driver: Hive JDBC (version 1.0.0)
Transaction isolation: TRANSACTION_REPEATABLE_READ
Beeline version 1.0.0 by Apache Hive
0: jdbc:hive2://example.com:> list jars;
+-----------+--+
| resource  |
+-----------+--+
+-----------+--+
No rows selected (1.047 seconds)

0: jdbc:hive2://example.com:> add jar hdfs:///shared/jars/uap-clj-hiveudf-1.0.0-standalone.jar;
INFO  : converting to local hdfs:///shared/jars/uap-clj-hiveudf-1.0.0-standalone.jar
INFO  : Added [/tmp/40a3f76b-d46f-4b45-bf9f-15d6f7a745ba_resources/uap-clj-hiveudf-1.0.0-standalone.jar] to class path
INFO  : Added resources: [hdfs:///shared/jars/uap-clj-hiveudf-1.0.0-standalone.jar]
No rows affected (0.864 seconds)

0: jdbc:hive2://example.com:> list jars;
+-------------------------------------------------------------------------------------------+--+
|                                         resource                                          |
+-------------------------------------------------------------------------------------------+--+
| /tmp/40a3f76b-d46f-4b45-bf9f-15d6f7a745ba_resources/uap-clj-hiveudf-1.0.0-standalone.jar  |
+-------------------------------------------------------------------------------------------+--+
1 row selected (1.417 seconds)
```

Register your functions with names of your choice:

```sql
0: jdbc:hive2://example.com:> create temporary function browser as 'uap-clj.udf.hive.generic.Browser';
No rows affected (2.026 seconds)
0: jdbc:hive2://example.com:> create temporary function os as 'uap-clj.udf.hive.generic.OS';
No rows affected (1.028 seconds)
0: jdbc:hive2://example.com:> create temporary function device as 'uap-clj.udf.hive.generic.Device';
No rows affected (0.878 seconds)
```

Usage instructions for these functions can be looked up if needed:
```sql
0: jdbc:hive2://example.com:> describe function device;
+---------------------------------------------------------+--+
|                        tab_name                         |
+---------------------------------------------------------+--+
| Takes a useragent & returns struct<family,brand,model>  |
+---------------------------------------------------------+--+
1 row selected (0.728 seconds)
```

If you don't already have a source of useragent data in form of SELECTable columns of useragent strings in an existing Hive table, you can populate an external table by copying a text file comprising user agent strings, one per line, to a location in HDFS:

```sql
0: jdbc:hive2://example.com:> CREATE EXTERNAL TABLE raw_useragent(agent STRING)
. . . . . . . . . . . . . . >   ROW FORMAT DELIMITED
. . . . . . . . . . . . . . >   LINES TERMINATED BY '\n'
. . . . . . . . . . . . . . >   STORED AS TEXTFILE
. . . . . . . . . . . . . . >   LOCATION '/shared/data/raw/useragent';
No rows affected (0.755 seconds)
```

Note that we've left off a FIELDS TERMINATED BY specification, since each line corresponds to one field. This is the simplest case of a Hive table, EXTERNAL or regular:

```sql
0: jdbc:hive2://example.com:> describe raw_useragent;
+-----------+------------+----------+--+
| col_name  | data_type  | comment  |
+-----------+------------+----------+--+
| agent     | string     |          |
+-----------+------------+----------+--+
1 row selected (1.534 seconds)
```

Assuming you've moved a source text file (or several) to `hdfs:///shared/data/raw/useragent`, you should see a populated external table the size of the number of lines in your source file:

```sql
0: jdbc:hive2://example.com:> select count(*) as rowcount from raw_useragent;
[SNIP]
INFO  : 2015-06-03 03:12:57,478 Stage-1 map = 0%,  reduce = 0%
INFO  : 2015-06-03 03:13:03,704 Stage-1 map = 100%,  reduce = 0%, Cumulative CPU 1.53 sec
INFO  : 2015-06-03 03:13:09,917 Stage-1 map = 100%,  reduce = 100%, Cumulative CPU 2.95 sec
INFO  : MapReduce Total cumulative CPU time: 2 seconds 950 msec
INFO  : Ended Job = job_1432342545485_0007
+-----------+--+
| rowcount  |
+-----------+--+
| 6832      |
+-----------+--+
1 row selected (19.59 seconds)
```

Now you're ready to play with your data and try out your UDFs:

```sql
: jdbc:hive2://example.com:> SELECT agent,
. . . . . . . . . . . . . .>   browser(agent) AS browser,
. . . . . . . . . . . . . .>   os(agent) AS os,
. . . . . . . . . . . . . .>   device(agent) AS device
. . . . . . . . . . . . . .> FROM raw_useragent
. . . . . . . . . . . . . .> LIMIT 10;
+----------------------------------------------------------------------------------------------------------------------------------+------------------+--------------------+------------------------+--+
|                                                              agent                                                               |     browser      |         os         |         device         |
+----------------------------------------------------------------------------------------------------------------------------------+------------------+--------------------+------------------------+--+
| AppleWebKit/531.0 (KHTML, like Gecko) Chrome/1111100111 Safari/531.0                                                             | {"family":"Safari","major":"","minor":"","patch":""}        | {"family":"Other","major":"","minor":"","patch":"","patch_minor":""}          | {"family":"Other","brand":"","model":""}                |
| Chrome/15.0.860.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/15.0.860.0               | {"family":"Chrome","major":"15","minor":"0","patch":"860"}  | {"family":"Windows Vista","major":"","minor":"","patch":"","patch_minor":""}  | {"family":"Other","brand":"","model":""}                |
| Iron/2.0.168.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/530.1 (KHTML, like Gecko)                                         | {"family":"Iron","major":"2","minor":"0","patch":"168"}     | {"family":"Windows 7","major":"","minor":"","patch":"","patch_minor":""}      | {"family":"Other","brand":"","model":""}                |
| MMozilla/5.0 (Windows NT 6.0) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30 ChromePlus/1.6.3.0alpha4  | {"family":"Chrome","major":"12","minor":"0","patch":"742"}  | {"family":"Windows Vista","major":"","minor":"","patch":"","patch_minor":""}  | {"family":"Other","brand":"","model":""}                |
| Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)                                                                                | {"family":"IE","major":"10","minor":"0","patch":""}         | {"family":"Windows 3.1","major":"","minor":"","patch":"","patch_minor":""}    | {"family":"Other","brand":"","model":""}                |
| Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1)                                                                    | {"family":"IE","major":"7","minor":"0","patch":""}          | {"family":"Windows XP","major":"","minor":"","patch":"","patch_minor":""}     | {"family":"Other","brand":"","model":""}                |
| Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1; .NET CLR 3.0.04506.30)                                             | {"family":"IE","major":"7","minor":"0","patch":""}          | {"family":"Windows XP","major":"","minor":"","patch":"","patch_minor":""}     | {"family":"Other","brand":"","model":""}                |
| Mozilla/4.0 (Windows; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)                                                         | {"family":"IE","major":"7","minor":"0","patch":""}          | {"family":"Windows XP","major":"","minor":"","patch":"","patch_minor":""}     | {"family":"Other","brand":"","model":""}                |
| Mozilla/4.0 (compatible; Crawler; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)                                             | {"family":"Crawler","major":"","minor":"","patch":""}       | {"family":"Windows XP","major":"","minor":"","patch":"","patch_minor":""}     | {"family":"Spider","brand":"Spider","model":"Desktop"}  |
| Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)                                                                 | {"family":"IE","major":"10","minor":"0","patch":""}         | {"family":"Windows 7","major":"","minor":"","patch":"","patch_minor":""}      | {"family":"Other","brand":"","model":""}                |
+----------------------------------------------------------------------------------------------------------------------------------+-------------------------------------------------------------+-------------------------------------------------------------------------------+---------------------------------------------------------+--+
10 rows selected (1.182 seconds)
```

You can also pull values out of the browser, os, or device structs, e.g.:

```sql
0: jdbc:hive2://master02.smoke.vpc.rgops.com:> SELECT agent as received_useragent,
. . . . . . . . . . . . . . . . . . . . . . .>   browser(agent).family as browser_family,
. . . . . . . . . . . . . . . . . . . . . . .>   os(agent).family as os_family,
. . . . . . . . . . . . . . . . . . . . . . .>   device(agent).family as device_family
. . . . . . . . . . . . . . . . . . . . . . .> FROM raw_useragent
. . . . . . . . . . . . . . . . . . . . . . .> WHERE device(agent).family != "Other"
. . . . . . . . . . . . . . . . . . . . . . .> LIMIT 10;
+----------------------------------------------------------------------------------------------------------------------------------------------------+-----------------+-------------+-----------------------+--+
|                                                                 received_useragent                                                                 | browser_family  |  os_family  |     device_family     |
+----------------------------------------------------------------------------------------------------------------------------------------------------+-----------------+-------------+-----------------------+--+
| Mozilla/4.0 (compatible; Crawler; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)                                                               | Crawler         | Windows XP  | Spider                |
| Mozilla/5.0 (Linux; Android 4.0.4; BNTV600 Build/IMM76L) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.138 Safari/537.36                 | Chrome          | Android     | BNTV600               |
| Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19     | Chrome Mobile   | Android     | Samsung Galaxy Nexus  |
| Mozilla/5.0 (Linux; Android 4.0.4; SAMSUNG-SGH-I437 Build/IMM76D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.93 Mobile Safari/537.36  | Chrome Mobile   | Android     | Samsung SGH-I437      |
| Mozilla/5.0 (Linux; Android 4.1.1; TBDG1073 Build/JRO03C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.93 Safari/537.36                 | Chrome          | Android     | TBDG1073              |
| Mozilla/5.0 (Linux; Android 4.1.2; B1-710 Build/JZO54K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.93 Safari/537.36                   | Chrome          | Android     | B1-710                |
| Mozilla/5.0 (Linux; Android 4.1.2; LG-LS970 Build/JZO54K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.93 Mobile Safari/537.36          | Chrome Mobile   | Android     | LG-LS970              |
| Mozilla/5.0 (Linux; Android 4.2.2; NX785QC8G Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.94 Safari/537.36                 | Chrome          | Android     | NX785QC8G             |
| Mozilla/5.0 (Linux; Android 4.3; GT-I9300 Build/JSS15J) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.93 Mobile Safari/537.36            | Chrome Mobile   | Android     | Samsung GT-I9300      |
| Mozilla/5.0 (Linux; Android 4.3; K01A Build/JSS15Q) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.93 Safari/537.36                       | Chrome          | Android     | K01A                  |
+----------------------------------------------------------------------------------------------------------------------------------------------------+-----------------+-------------+-----------------------+--+
10 rows selected (13.715 seconds)
```

## Future / Enhancements

What's up next:

1. Replace BDD tests with test generators using ua-parser/uap-core fixtures.

Pull requests will be very happily considered.

__Maintained by Russell Whitaker__

## License

The MIT License (MIT)

Copyright (c) 2015 Russell Whitaker

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
