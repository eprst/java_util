package com.github.eprst.murmur3;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.openjdk.jmh.annotations.*;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class BenchSink128 {
  static final int numHashables = 1000;

  @State(Scope.Thread)
  public static class MyState {
    TestHashingSink.Hashable[] hashables;
    final MurmurHash3.HashCode128 hashCode = new MurmurHash3.HashCode128();

    public MyState () {
      hashables = new TestHashingSink.Hashable[numHashables];
      RandomHashableGenerator g = new RandomHashableGenerator();
      for (int i = 0; i < numHashables; i++) {
        hashables[i] = g.randomHashable();
      }
    }
  }

  @Benchmark
  public void guavaSink(MyState state) {
    Hasher g = Hashing.murmur3_128(0).newHasher();
    for (TestHashingSink.Hashable hashable : state.hashables) {
      hashable.sendToGuava(g);
    }
    g.hash();
  }

  @Benchmark
  public void hashingSink(MyState state) {
    HashingSink128 s = new HashingSink128(0);
    for (TestHashingSink.Hashable hashable : state.hashables) {
      hashable.sendToHashing(s);
    }
    s.finish(state.hashCode);
  }
}
