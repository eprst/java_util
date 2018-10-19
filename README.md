[![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](http://unlicense.org/)

Fast Murmur3 hash implementation for Java, mostly a fork of
[yonik](https://github.com/yonik/java_util) work with
String-optimized 128-bit implementation added.

String optimization is based on avoiding
[quite expensive](http://www.evanjones.ca/software/java-string-encoding-internals.html) call to
`String.getBytes` and decoding small chunks on the go instead.

# Performance
Here are results on my laptop, run `./gradlew jmh` to reproduce.

ACSCII
```
Benchmark                            Mode  Cnt      Score      Error  Units
BenchString128.guavaAscii           thrpt   10  33578.136 ± 1278.306  ops/s
BenchString128.murmurAsciiBytes     thrpt   10  71952.571 ± 2704.746  ops/s
BenchString128.murmurAsciiString    thrpt   10  74314.292 ± 2875.555  ops/s
```

Unicode
```
Benchmark                            Mode  Cnt      Score      Error  Units
BenchString128.guavaUnicode         thrpt   10  18568.165 ±  866.071  ops/s
BenchString128.murmurUnicodeBytes   thrpt   10  28325.267 ±  754.416  ops/s
BenchString128.murmurUnicodeString  thrpt   10  35280.266 ±  576.442  ops/s
```