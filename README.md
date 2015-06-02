# uap-clj-hiveudf

An Apache Hadoop Hive Simple UDF wrapper around the [`uap-clj`](https://github.com/russellwhitaker/uap-clj) library providing Browser, O/S, and Device field extraction functions.

##Setup

###Run test suite

```bash
→ lein clean && lein spec --reporter=c
Compiling uap-clj.udf.hive.browser
Compiling uap-clj.udf.hive.device
Compiling uap-clj.udf.hive.os

Ran 24 tests containing 24 assertions.
0 failures, 0 errors.
```

###Deploy artifact(s) to Hadoop

```bash
→ lein clean && lein uberjar
Retrieving uap-clj/uap-clj/1.0.0/uap-clj-1.0.0.pom from clojars
Retrieving uap-clj/uap-clj/1.0.0/uap-clj-1.0.0.jar from clojars
Compiling uap-clj.udf.hive.browser
Compiling uap-clj.udf.hive.device
Compiling uap-clj.udf.hive.os
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
0: jdbc:hive2://example.com:> create temporary function browser as 'uap-clj.udf.hive.Browser';
No rows affected (2.026 seconds)
0: jdbc:hive2://example.com:> create temporary function os as 'uap-clj.udf.hive.OS';
No rows affected (1.028 seconds)
0: jdbc:hive2://example.com:> create temporary function device as 'uap-clj.udf.hive.Device';
No rows affected (0.878 seconds)
```

If you don't already have a source of useragent data in form of SELECTable columns of useragent strings in an existing Hive table, you can populate an external table by copying a text file comprising user agent strings, one per line, to a temporary location in HDFS:

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
0: jdbc:hive2://example.com:> select count(*) from raw_useragent;
[SNIP]
INFO  : MapReduce Total cumulative CPU time: 2 seconds 840 msec
INFO  : Ended Job = job_1432342545485_0004
+-------+--+
|  _c0  |
+-------+--+
| 6832  |
+-------+--+
1 row selected (19.442 seconds)
```

Now you're ready to play with your data and try out your UDFs:

```sql
: jdbc:hive2://example.com:> SELECT agent,
. . . . . . . . . . . . . .>   browser(agent) AS browser,
. . . . . . . . . . . . . .>   os(agent) AS os,
. . . . . . . . . . . . . .>   device(agent) AS device
. . . . . . . . . . . . . .> FROM raw_useragent
. . . . . . . . . . . . . .> LIMIT 10;
+----------------------------------------------------------------------------------------------------------------------------------+------------------+----------------------------------------+------------------------+--+
|                                                              agent                                                               |     browser      |                   os                   |         device         |
+----------------------------------------------------------------------------------------------------------------------------------+------------------+----------------------------------------+------------------------+--+
| AppleWebKit/531.0 (KHTML, like Gecko) Chrome/1111100111 Safari/531.0                                                             | Safari			        | Other	<empty>	<empty>	<empty>	<empty>  | Other	<empty>	<empty>  |
| Chrome/15.0.860.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/15.0.860.0               | Chrome	15	0	860  | Windows Vista				                      | Other	<empty>	<empty>  |
| Iron/2.0.168.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/530.1 (KHTML, like Gecko)                                         | Iron	2	0	168     | Windows 7				                          | Other	<empty>	<empty>  |
| MMozilla/5.0 (Windows NT 6.0) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30 ChromePlus/1.6.3.0alpha4  | Chrome	12	0	742  | Windows Vista				                      | Other	<empty>	<empty>  |
| Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)                                                                                | IE	10	0	         | Windows 3.1				                        | Other	<empty>	<empty>  |
| Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1)                                                                    | IE	7	0	          | Windows XP				                         | Other	<empty>	<empty>  |
| Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1; .NET CLR 3.0.04506.30)                                             | IE	7	0	          | Windows XP				                         | Other	<empty>	<empty>  |
| Mozilla/4.0 (Windows; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)                                                         | IE	7	0	          | Windows XP				                         | Other	<empty>	<empty>  |
| Mozilla/4.0 (compatible; Crawler; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)                                             | Crawler			       | Windows XP				                         | Spider	Spider	Desktop  |
| Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)                                                                 | IE	10	0	         | Windows 7				                          | Other	<empty>	<empty>  |
+----------------------------------------------------------------------------------------------------------------------------------+------------------+----------------------------------------+------------------------+--+
10 rows selected (0.979 seconds)
```

See that three output columns are specified in the query, but what looks like twelve columns are displayed: that's because each of the UDFs - `browser()`, `os()`, and `device()` - returns a simple Hive Text object comprising several strings concatenated by non-printing tab characters. To store these values cleanly and usably, you might consider creating a target table containing the outputs of the UDFs split on the embedded tab delimiters stored in columns of Hive `array<string>` type, e.g.:

```sql
0: jdbc:hive2://example.com:> CREATE TABLE processed_useragent(
. . . . . . . . . . . . . . >   agent STRING,
. . . . . . . . . . . . . . >   browser array<STRING>,
. . . . . . . . . . . . . . >   os array<STRING>,
. . . . . . . . . . . . . . >   device array<STRING>)
. . . . . . . . . . . . . . > LOCATION '/shared/data/processed/useragent';
No rows affected (0.57 seconds)
0: jdbc:hive2://example.com:> describe processed_useragent;
+-----------+----------------+----------+--+
| col_name  |   data_type    | comment  |
+-----------+----------------+----------+--+
| agent     | string         |          |
| browser   | array<string>  |          |
| os        | array<string>  |          |
| device    | array<string>  |          |
+-----------+----------------+----------+--+
4 rows selected (1.115 seconds)
```

Now populate this new table from the `raw_useragent` external table:


```sql
0: jdbc:hive2://example.com:> INSERT INTO TABLE processed_useragent
. . . . . . . . . . . . . . > SELECT agent,
. . . . . . . . . . . . . . >   split(browser(agent), '\\t'),
. . . . . . . . . . . . . . >   split(os(agent), '\\t'),
. . . . . . . . . . . . . . >   split(device(agent), '\\t')
. . . . . . . . . . . . . . > FROM raw_useragent;
[SNIP]
INFO  : Table mytable.processed_useragent stats: [numFiles=0, numRows=6832, totalSize=0, rawDataSize=1330370]

0: jdbc:hive2://example.com:> select count(*) as rowcount from processed_useragent;
+-----------+--+
| rowcount  |
+-----------+--+
| 6832      |
+-----------+--+
1 row selected (1.08 seconds)
```

Finally, you have a data source set up for use in useragent analytics:

```sql
0: jdbc:hive2://example.com:> select * from processed_useragent limit 10;
+----------------------------------------------------------------------------------------------------------------------------------+------------------------------+----------------------------------------------------+--------------------------------+--+
|                                                    processed_useragent.agent                                                     | processed_useragent.browser  |               processed_useragent.os               |   processed_useragent.device   |
+----------------------------------------------------------------------------------------------------------------------------------+------------------------------+----------------------------------------------------+--------------------------------+--+
| AppleWebKit/531.0 (KHTML, like Gecko) Chrome/1111100111 Safari/531.0                                                             | ["Safari","","",""]          | ["Other","<empty>","<empty>","<empty>","<empty>"]  | ["Other","<empty>","<empty>"]  |
| Chrome/15.0.860.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/15.0.860.0               | ["Chrome","15","0","860"]    | ["Windows Vista","","","",""]                      | ["Other","<empty>","<empty>"]  |
| Iron/2.0.168.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/530.1 (KHTML, like Gecko)                                         | ["Iron","2","0","168"]       | ["Windows 7","","","",""]                          | ["Other","<empty>","<empty>"]  |
| MMozilla/5.0 (Windows NT 6.0) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30 ChromePlus/1.6.3.0alpha4  | ["Chrome","12","0","742"]    | ["Windows Vista","","","",""]                      | ["Other","<empty>","<empty>"]  |
| Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)                                                                                | ["IE","10","0",""]           | ["Windows 3.1","","","",""]                        | ["Other","<empty>","<empty>"]  |
| Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1)                                                                    | ["IE","7","0",""]            | ["Windows XP","","","",""]                         | ["Other","<empty>","<empty>"]  |
| Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1; .NET CLR 3.0.04506.30)                                             | ["IE","7","0",""]            | ["Windows XP","","","",""]                         | ["Other","<empty>","<empty>"]  |
| Mozilla/4.0 (Windows; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)                                                         | ["IE","7","0",""]            | ["Windows XP","","","",""]                         | ["Other","<empty>","<empty>"]  |
| Mozilla/4.0 (compatible; Crawler; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)                                             | ["Crawler","","",""]         | ["Windows XP","","","",""]                         | ["Spider","Spider","Desktop"]  |
| Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)                                                                 | ["IE","10","0",""]           | ["Windows 7","","","",""]                          | ["Other","<empty>","<empty>"]  |
+----------------------------------------------------------------------------------------------------------------------------------+------------------------------+----------------------------------------------------+--------------------------------+--+
10 rows selected (1.119 seconds)
```

From this point, you can extract fields of interest in your reports following a variation of this basic pattern:

```sql
0: jdbc:hive2://example.com:> SELECT agent AS logged_agent,
. . . . . . . . . . . . . . >   browser[0] as browser_family,
. . . . . . . . . . . . . . >   browser[1] as browser_major,
. . . . . . . . . . . . . . >   browser[2] as browser_minor,
. . . . . . . . . . . . . . >   browser[3] as browser_patch,
. . . . . . . . . . . . . . >   os[0] as os_family,
. . . . . . . . . . . . . . >   os[1] as os_major,
. . . . . . . . . . . . . . >   os[2] as os_minor,
. . . . . . . . . . . . . . >   os[3] as os_patch,
. . . . . . . . . . . . . . >   os[4] as os_patch_minor,
. . . . . . . . . . . . . . >   device[0] as device_family,
. . . . . . . . . . . . . . >   device[1] as device_brand,
. . . . . . . . . . . . . . >   device[2] as device_model
. . . . . . . . . . . . . . > FROM processed_useragent LIMIT 10;
+----------------------------------------------------------------------------------------------------------------------------------+-----------------+----------------+----------------+----------------+----------------+-----------+-----------+-----------+-----------------+----------------+---------------+---------------+--+
|                                                           logged_agent                                                           | browser_family  | browser_major  | browser_minor  | browser_patch  |   os_family    | os_major  | os_minor  | os_patch  | os_patch_minor  | device_family  | device_brand  | device_model  |
+----------------------------------------------------------------------------------------------------------------------------------+-----------------+----------------+----------------+----------------+----------------+-----------+-----------+-----------+-----------------+----------------+---------------+---------------+--+
| AppleWebKit/531.0 (KHTML, like Gecko) Chrome/1111100111 Safari/531.0                                                             | Safari          |                |                |                | Other          | <empty>   | <empty>   | <empty>   | <empty>         | Other          | <empty>       | <empty>       |
| Chrome/15.0.860.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/15.0.860.0               | Chrome          | 15             | 0              | 860            | Windows Vista  |           |           |           |                 | Other          | <empty>       | <empty>       |
| Iron/2.0.168.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/530.1 (KHTML, like Gecko)                                         | Iron            | 2              | 0              | 168            | Windows 7      |           |           |           |                 | Other          | <empty>       | <empty>       |
| MMozilla/5.0 (Windows NT 6.0) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30 ChromePlus/1.6.3.0alpha4  | Chrome          | 12             | 0              | 742            | Windows Vista  |           |           |           |                 | Other          | <empty>       | <empty>       |
| Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)                                                                                | IE              | 10             | 0              |                | Windows 3.1    |           |           |           |                 | Other          | <empty>       | <empty>       |
| Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1)                                                                    | IE              | 7              | 0              |                | Windows XP     |           |           |           |                 | Other          | <empty>       | <empty>       |
| Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1; .NET CLR 3.0.04506.30)                                             | IE              | 7              | 0              |                | Windows XP     |           |           |           |                 | Other          | <empty>       | <empty>       |
| Mozilla/4.0 (Windows; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)                                                         | IE              | 7              | 0              |                | Windows XP     |           |           |           |                 | Other          | <empty>       | <empty>       |
| Mozilla/4.0 (compatible; Crawler; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)                                             | Crawler         |                |                |                | Windows XP     |           |           |           |                 | Spider         | Spider        | Desktop       |
| Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)                                                                 | IE              | 10             | 0              |                | Windows 7      |           |           |           |                 | Other          | <empty>       | <empty>       |
+----------------------------------------------------------------------------------------------------------------------------------+-----------------+----------------+----------------+----------------+----------------+-----------+-----------+-----------+-----------------+----------------+---------------+---------------+--+
10 rows selected (1.475 seconds)
```

## Future / Enhancements

What's up next:

1. Re-implement UDFs as GenericUDFs;
2. Add memoization and/or LRU caching after various performance tests;

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
