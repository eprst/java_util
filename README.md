[![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](http://unlicense.org/)
[![Build Status](https://travis-ci.com/eprst/murmur3.svg?branch=master)](https://travis-ci.com/eprst/murmur3)

Fast Murmur3 hash implementation for Java, mostly a fork of
[yonik](https://github.com/yonik/java_util) work with
String-optimized 128-bit implementation added. See [blog post](http://yonik.com/murmurhash3-for-java/)
about the original implementation.

String optimization assumes UTF-8 strings and is based on avoiding
[quite expensive](http://www.evanjones.ca/software/java-string-encoding-internals.html) call to
`String.getBytes` and decoding small chunks on the go instead.

There is also an ASCII-optimized `murmurhash3_x64_128_ascii` version which expects ASCII-only UTF-8 strings
and gives big performance boost.

# Performance
Here are results for 128-bit hash on my laptop, run `./gradlew jmh` to reproduce.

ACSCII (1-128 characters strings)
```
Benchmark                                   Mode  Cnt       Score        Error  Units
BenchString128.guavaAscii                  thrpt  100   38820.061  ±   767.128  ops/s
BenchString128.murmurAsciiBytes            thrpt  100   79370.216  ±  1014.949  ops/s
BenchString128.murmurAsciiString           thrpt  100   83249.135  ±   521.336  ops/s
BenchString128.murmurAsciiOptimizedString  thrpt  100  155105.622  ±  1984.475  ops/s
```

Unicode (1-64 character strings)
```
Benchmark                            Mode  Cnt      Score      Error  Units
BenchString128.guavaUnicode         thrpt  100  22044.362 ±  228.743  ops/s
BenchString128.murmurUnicodeBytes   thrpt  100  32272.569 ±  635.764  ops/s
BenchString128.murmurUnicodeString  thrpt  100  36704.326 ±  237.645  ops/s
```