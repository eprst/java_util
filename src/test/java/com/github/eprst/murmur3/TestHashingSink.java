package com.github.eprst.murmur3;

import com.google.common.base.Charsets;
import com.google.common.hash.*;
import junit.framework.TestCase;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
@SuppressWarnings("UnstableApiUsage")
public class TestHashingSink extends TestCase {
  public void testByte() {
    test(new HashableByte((byte) 42));
  }

  public void testManyBytes() {
    Hashable[] bytes = new Hashable[]{
        new HashableByte((byte) 23),
        new HashableByte((byte) 43),
        new HashableByte((byte) 23),
        new HashableByte((byte) 25),
        new HashableByte((byte) 21),
        new HashableByte((byte) 27),
        new HashableByte((byte) 23),
        new HashableByte((byte) 24),
        new HashableByte((byte) 2),
        new HashableByte((byte) 29),
        new HashableByte((byte) 83),
        new HashableByte((byte) 23),
        new HashableByte((byte) 3),
        new HashableByte((byte) 20),
        new HashableByte((byte) 28),
        new HashableByte((byte) 48),
        new HashableByte((byte) 22),
        new HashableByte((byte) 21),
    };
    test(bytes);
  }

  public void testShort() {
    test(new HashableShort((short) 32000));
  }

  public void testInt() {
    test(new HashableInt(Integer.MAX_VALUE));
    test(new HashableInt(Integer.MIN_VALUE));
    test(new HashableInt(Integer.MAX_VALUE - 42));
    test(new HashableInt(Integer.MIN_VALUE + 42));
    test(new HashableInt(0));
    test(new HashableInt(1));
  }

  public void testLong() {
    test(new HashableLong(Long.MAX_VALUE));
    test(new HashableLong(Long.MIN_VALUE));
    test(new HashableLong(Long.MAX_VALUE - 42));
    test(new HashableLong(Long.MIN_VALUE + 42));
    test(new HashableLong(0L));
    test(new HashableLong(1L));
  }

  public void testFloat() {
    test(new HashableFloat(Float.MIN_VALUE));
    test(new HashableFloat(Float.MAX_VALUE));
    test(new HashableFloat(Float.NaN));
    test(new HashableFloat(Float.NEGATIVE_INFINITY));
    test(new HashableFloat(0f));
    test(new HashableFloat(1f));
  }

  public void testDouble() {
    test(new HashableDouble(Double.MIN_VALUE));
    test(new HashableDouble(Double.MAX_VALUE));
    test(new HashableDouble(Double.NaN));
    test(new HashableDouble(Double.NEGATIVE_INFINITY));
    test(new HashableDouble(0f));
    test(new HashableDouble(1f));
  }

  public void testChar() {
    test(new HashableChar((char) 0));
    test(new HashableChar('a'));
    test(new HashableChar('\u0444'));
  }

  public void testBytes() {
    test(new HashableBytes(new byte[]{0, 1, 2}));
    test(new HashableBytes(new byte[]{0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2}));
    test(
        new HashableBytes(new byte[]{0, 1, 2, 0}),
        new HashableBytes(new byte[]{1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2})
    );
  }

  public void testString() {
    test(new HashableString("hello, world!", Charsets.UTF_8));
    test(new HashableString("hello, world!", Charsets.UTF_16));
    test(new HashableString(
        "\u041f\u0440\u043e\u043b\u0435\u0442\u0430\u0440\u0438\u0438 \u0432\u0441\u0435 " +
        "\u0441\u0442\u0440\u0430\u043d, \u0441\u043e\u0435\u0434\u0438\u043d\u044f\u0439\u0442\u0435\u0441\u044c!",
        Charsets.UTF_8
    ));
    test(new HashableString("\u04F9\u001D\uD97D\uDC18\u07E9\u07FF\u01CD\uD92E\uDF3C", Charsets.UTF_8));
  }

  public void testAsciiString() {
    test(new HashableUtf8AsciiString("hello, world!"));
    test(new HashableUtf8AsciiString("A smell of petroleum prevails throughout"));
  }

  public void testSingleRandom() {
    RandomHashableGenerator g = new RandomHashableGenerator();

    for (int j = 0; j < 100; j++) {
      Hashable h = g.randomHashable(200);
      test(h);
    }

    for (int j = 0; j < 10; j++) {
      Hashable h = g.randomHashable(100000);
      test(h);
    }
  }

  public void testManyRandom() {
    int num = 10;
    RandomHashableGenerator g = new RandomHashableGenerator();
    Hashable[] hashables = new Hashable[num];

    for (int j = 0; j < 100; j++) {
      for (int i = 0; i < num; i++) {
        hashables[i] = g.randomHashable(200);
      }
      test(hashables);
    }

    for (int j = 0; j < 10; j++) {
      for (int i = 0; i < num; i++) {
        hashables[i] = g.randomHashable(100000);
      }
      test(hashables);
    }
  }

  public void testReset() {
    RandomHashableGenerator g = new RandomHashableGenerator();
    Hashable h = g.randomHashable(200);

    int seed = Math.abs(new Random().nextInt());

    HashingSink128 s = new HashingSink128(seed);

    h.sendToHashing(s);
    MurmurHash3.HashCode128 hc1 = s.finish();
    String result1 = hc1.toString();

    h.sendToHashing(s);
    MurmurHash3.HashCode128 hc2 = s.finish();
    String result2 = hc2.toString();

    assertEquals(result1, result2);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  private void test(Hashable... hashables) {
    int seed = Math.abs(new Random().nextInt());
    Hasher g = Hashing.murmur3_128(seed).newHasher();

    HashingSink128 s = new HashingSink128(seed);
    for (final Hashable hashable : hashables) {
      hashable.sendToGuava(g);
      hashable.sendToHashing(s);
    }
    String guavaHash = g.hash().toString();

    MurmurHash3.HashCode128 hc = s.finish();
    String result = hc.toString();

    if (hashables.length == 1) {
      Hashable h = hashables[0];
      if (!guavaHash.equals(result) && h instanceof HashableString) {
        HashableString hs = (HashableString) h;
        if (hs.charset.equals(StandardCharsets.UTF_8)) {
          MurmurHash3.HashCode128 c = new MurmurHash3.HashCode128();
          MurmurHash3.murmurhash3_x64_128(hs.s, 0, hs.s.length(), seed, null, c);
          System.out.println("Murmur3 result: " + c);

          HashFunction guava = Hashing.murmur3_128(seed);
          HashCode gh = guava.hashString(hs.s, Charsets.UTF_8);
          System.out.println("Guava result: " + gh);
        }
      }
      String message = "seed: " + seed+", hashable: " + h;
      assertEquals(message, guavaHash, result);
    } else {
      assertEquals(guavaHash, result);
    }

  }

  @SuppressWarnings("UnstableApiUsage")
  interface Hashable {
    void sendToGuava(PrimitiveSink sink);

    void sendToHashing(HashingSink128 sink);
  }

  static class HashableBytes implements Hashable {
    private final byte[] bytes;

    HashableBytes(final byte[] bytes) {this.bytes = bytes;}

    @Override
    public void sendToGuava(final PrimitiveSink sink) {
      sink.putBytes(bytes);
    }

    @Override
    public void sendToHashing(final HashingSink128 sink) {
      sink.putBytes(bytes);
    }
  }

  static class HashableByte implements Hashable {
    private final byte b;

    HashableByte(final byte b) {this.b = b;}

    @Override
    public void sendToGuava(final PrimitiveSink sink) {
      sink.putByte(b);
    }

    @Override
    public void sendToHashing(final HashingSink128 sink) {
      sink.putByte(b);
    }
  }

  static class HashableShort implements Hashable {
    private final short s;

    HashableShort(final short s) {this.s = s;}

    @Override
    public void sendToGuava(final PrimitiveSink sink) {
      sink.putShort(s);
    }

    @Override
    public void sendToHashing(final HashingSink128 sink) {
      sink.putShort(s);
    }
  }

  static class HashableInt implements Hashable {
    private final int s;

    HashableInt(final int i) {this.s = i;}

    @Override
    public void sendToGuava(final PrimitiveSink sink) {
      sink.putInt(s);
    }

    @Override
    public void sendToHashing(final HashingSink128 sink) {
      sink.putInt(s);
    }
  }

  static class HashableLong implements Hashable {
    private final long l;

    HashableLong(final long l) {this.l = l;}

    @Override
    public void sendToGuava(final PrimitiveSink sink) {
      sink.putLong(l);
    }

    @Override
    public void sendToHashing(final HashingSink128 sink) {
      sink.putLong(l);
    }
  }

  static class HashableFloat implements Hashable {
    private final float f;

    HashableFloat(final float f) {this.f = f;}

    @Override
    public void sendToGuava(final PrimitiveSink sink) {
      sink.putFloat(f);
    }

    @Override
    public void sendToHashing(final HashingSink128 sink) {
      sink.putFloat(f);
    }
  }

  static class HashableDouble implements Hashable {
    private final double d;

    HashableDouble(final double d) {this.d = d;}

    @Override
    public void sendToGuava(final PrimitiveSink sink) {
      sink.putDouble(d);
    }

    @Override
    public void sendToHashing(final HashingSink128 sink) {
      sink.putDouble(d);
    }
  }

  static class HashableChar implements Hashable {
    private final char c;

    HashableChar(final char c) {this.c = c;}

    @Override
    public void sendToGuava(final PrimitiveSink sink) {
      sink.putChar(c);
    }

    @Override
    public void sendToHashing(final HashingSink128 sink) {
      sink.putChar(c);
    }
  }

  static class HashableString implements Hashable {
    private final String s;
    private final Charset charset;

    HashableString(final String s, final Charset charset) {
      this.s = s;
      this.charset = charset;
    }

    @Override
    public void sendToGuava(final PrimitiveSink sink) {
      sink.putString(s, charset);
    }

    @Override
    public void sendToHashing(final HashingSink128 sink) {
      sink.putString(s, charset);
    }

    @Override
    public String toString() {
      return "HashableString{" +
             "s='" + s + '\'' +
             ", charset=" + charset +
             '}';
    }
  }

  static class HashableUtf8AsciiString implements Hashable {
    private final String s;

    HashableUtf8AsciiString(final String s) {
      this.s = s;
    }

    @Override
    public void sendToGuava(final PrimitiveSink sink) {
      sink.putString(s, StandardCharsets.UTF_8);
    }

    @Override
    public void sendToHashing(final HashingSink128 sink) {
      sink.putUtf8AsciiString(s, 0, s.length());
    }
  }
}
