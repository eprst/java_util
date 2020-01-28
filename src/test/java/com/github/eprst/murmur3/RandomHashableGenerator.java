package com.github.eprst.murmur3;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class RandomHashableGenerator {
  private Random r = new Random();
  private RandomStringsGenerator rsg = new RandomStringsGenerator();

  public TestHashingSink.Hashable randomHashable(int size) {
//        return new TestHashingSink.HashableString(rsg.randomUnicode(200), StandardCharsets.UTF_8);
    switch (r.nextInt(10)) {
      case 0:
        return new TestHashingSink.HashableBytes(randomBytes());
      case 1:
        return new TestHashingSink.HashableByte((byte) r.nextInt(1 << 8));
      case 2:
        return new TestHashingSink.HashableShort((short) r.nextInt(1 << 16));
      case 3:
        return new TestHashingSink.HashableInt(r.nextInt());
      case 4:
        return new TestHashingSink.HashableLong(r.nextLong());
      case 5:
        return new TestHashingSink.HashableFloat(r.nextFloat());
      case 6:
        return new TestHashingSink.HashableDouble(r.nextDouble());
      case 7:
        return new TestHashingSink.HashableChar((char) r.nextInt());
      case 8:
        return new TestHashingSink.HashableString(rsg.randomUnicode(size), StandardCharsets.UTF_8);
      case 9:
        return new TestHashingSink.HashableUtf8AsciiString(rsg.randomAscii(size));
      default:
        throw new RuntimeException();
    }
  }

  private byte[] randomBytes() {
    byte[] res = new byte[r.nextInt(300)];
    r.nextBytes(res);
    return res;
  }
}
