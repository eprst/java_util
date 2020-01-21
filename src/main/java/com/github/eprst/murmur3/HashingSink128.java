package com.github.eprst.murmur3;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.github.eprst.murmur3.MurmurHash3.*;

/**
 * Guava Sink-like class for 128-bit Murmur3 hashing. Not thread-safe.
 *
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public final class HashingSink128 {
  private final long seed;
  private long h1;
  private long h2;

  private final byte[] buffer = new byte[19];
  private int bufferOffset = 0;
  private int totalBytesHashed = 0;

  public HashingSink128(int seed) {
    this.seed = seed;
    reset();
  }

  /** Aborts current hash computation and resets to the initial state */
  public HashingSink128 reset() {
    h1 = seed & 0x00000000FFFFFFFFL;
    h2 = seed & 0x00000000FFFFFFFFL;
    return this;
  }

  public HashingSink128 putBytes(byte[] sourceBytes, int sourceOffset, int sourceLength) {
    int bufferBytesLeft = 16 - bufferOffset;

    if (bufferBytesLeft > sourceLength) { // still not enough to fill the buffer
      System.arraycopy(sourceBytes, sourceOffset, buffer, bufferOffset, sourceLength);
      bufferOffset += sourceLength;
    } else {
      System.arraycopy(sourceBytes, sourceOffset, buffer, bufferOffset, bufferBytesLeft);
      sourceOffset += bufferBytesLeft;
      int sourceBytesLeft = sourceLength - bufferBytesLeft;
      bufferOffset = 0;

      while (true) {
        munch();

        if (sourceBytesLeft < 16) {
          System.arraycopy(sourceBytes, sourceOffset, buffer, 0, sourceBytesLeft);
          bufferOffset = sourceBytesLeft;
          break;
        } else {
          System.arraycopy(sourceBytes, sourceOffset, buffer, 0, 16);
          sourceBytesLeft -= 16;
          sourceOffset += 16;
        }
      }
    }
    return this;
  }

  public HashingSink128 putBytes(byte[] bytes) {
    return putBytes(bytes, 0, bytes.length);
  }

  public HashingSink128 putMurmurHash3(MurmurHash3.HashCode128 hash) {
    putLong(hash.val1);
    putLong(hash.val2);
    return this;
  }

  private void munch() {
    long k1 = MurmurHash3.getLongLittleEndian(buffer, 0);
    long k2 = MurmurHash3.getLongLittleEndian(buffer, 8);
    k1 *= c1;
    k1 = Long.rotateLeft(k1, 31);
    k1 *= c2;
    h1 ^= k1;
    h1 = Long.rotateLeft(h1, 27);
    h1 += h2;
    h1 = h1 * 5 + 0x52dce729;
    k2 *= c2;
    k2 = Long.rotateLeft(k2, 33);
    k2 *= c1;
    h2 ^= k2;
    h2 = Long.rotateLeft(h2, 31);
    h2 += h1;
    h2 = h2 * 5 + 0x38495ab5;
    totalBytesHashed += 16;
  }

  public HashingSink128 putByte(byte b) {
    buffer[bufferOffset] = b;
    if (bufferOffset < 15) {
      bufferOffset++;
    } else {
      munch();
      bufferOffset = 0;
    }
    return this;
  }

  public HashingSink128 putShort(short s) {
    putByte((byte) s);
    putByte((byte) (s >>> 8));
    return this;
  }

  public HashingSink128 putInt(int i) {
    putByte((byte) i);
    putByte((byte) (i >>> 8));
    putByte((byte) (i >>> 16));
    putByte((byte) (i >>> 24));
    return this;
  }

  public HashingSink128 putLong(long l) {
    putByte((byte) l);
    putByte((byte) (l >>> 8));
    putByte((byte) (l >>> 16));
    putByte((byte) (l >>> 24));
    putByte((byte) (l >>> 32));
    putByte((byte) (l >>> 40));
    putByte((byte) (l >>> 48));
    putByte((byte) (l >>> 56));
    return this;
  }

  public HashingSink128 putFloat(float f) {
    putInt(Float.floatToIntBits(f));
    return this;
  }

  public HashingSink128 putDouble(double d) {
    putLong(Double.doubleToRawLongBits(d));
    return this;
  }

  public HashingSink128 putChar(char c) {
    putByte((byte) c);
    putByte((byte) (c >>> 8));
    return this;
  }

  public HashingSink128 putUnencodedChars(CharSequence charSequence) {
    for (int i = 0, len = charSequence.length(); i < len; i++) {
      putChar(charSequence.charAt(i));
    }
    return this;
  }

  public HashingSink128 putString(CharSequence charSequence, Charset charset) {
    if (charset.equals(StandardCharsets.UTF_8)) {
      return putUtf8String(charSequence, 0, charSequence.length());
    } else {
      return putBytes(charSequence.toString().getBytes(charset));
    }
  }

  /**
   * Optimized version of {@code putString} which assumes that {@code str} is an UTF-8 encoded string.
   */
  public HashingSink128 putUtf8String(CharSequence data, int offset, int len) {
    int pos = offset;
    int end = offset + len;

    while (true) {
      // decode at least 16 bytes
      while (bufferOffset < 16 && pos < end) {
        char code = data.charAt(pos++);

        if (code < 0x80) {
          buffer[bufferOffset++] = (byte) code;
        } else if (code < 0x800) {
          buffer[bufferOffset++] = (byte) (0xc0 | code >> 6);
          buffer[bufferOffset++] = (byte) (0x80 | (code & 0x3f));
        } else if (code < 0xD800 || code > 0xDFFF || pos >= end) {
          // we check for pos>=end to encode an unpaired surrogate as 3 bytes.
          buffer[bufferOffset++] = (byte) (0xe0 | ((code >> 12)));
          buffer[bufferOffset++] = (byte) (0x80 | ((code >> 6) & 0x3f));
          buffer[bufferOffset++] = (byte) (0x80 | (code & 0x3f));
        } else {
          // surrogate pair
          int utf32 = (int) data.charAt(pos++);
          utf32 = ((code - 0xD7C0) << 10) + (utf32 & 0x3FF);
          buffer[bufferOffset++] = (byte) (0xf0 | ((utf32 >> 18)));
          buffer[bufferOffset++] = (byte) (0x80 | ((utf32 >> 12) & 0x3f));
          buffer[bufferOffset++] = (byte) (0x80 | ((utf32 >> 6) & 0x3f));
          buffer[bufferOffset++] = (byte) (0x80 | (utf32 & 0x3f));
        }
      }

      if (bufferOffset > 15) {
        munch();
        bufferOffset -= 16;
        switch (bufferOffset) {
          case 3:
            buffer[2] = buffer[18];
          case 2:
            buffer[1] = buffer[17];
          case 1:
            buffer[0] = buffer[16];
        }
      } else {
        break;
      }
    } // inner

    return this;
  }

  /**
   * Optimized version of {@code putString} which assumes that {@code str} is an UTF-8 encoded string and
   * contains only ASCII characters.
   */
  public HashingSink128 putUtf8AsciiString(CharSequence data, int offset, int len) {
    int pos = offset;
    int end = offset + len;

    while (true) {
      // decode at least 16 bytes
      while (bufferOffset < 16 && pos < end) {
        buffer[bufferOffset++] = (byte) (data.charAt(pos++) & 0xff);
      }

      if (bufferOffset > 15) {
        munch();
        bufferOffset -= 16;
      } else {
        break;
      }
    } // inner

    return this;
  }


  /**
   * Finalizes hash computation and returns the result. Instance is reset to the initial state and can
   * be reused.
   *
   * @return resulting hash
   */
  public MurmurHash3.HashCode128 finish() {
    MurmurHash3.HashCode128 result = new HashCode128();
    finish(result);
    return result;
  }

  /**
   * Finalizes hash computation and returns the result. Instance is reset to the initial state and can
   * be reused.
   *
   * @param result result holder
   */
  public void finish(MurmurHash3.HashCode128 result) {
    long k1 = 0;
    long k2 = 0;

    switch (bufferOffset) {
      case 15:
        k2 = (buffer[14] & 0xffL) << 48;
      case 14:
        k2 |= (buffer[13] & 0xffL) << 40;
      case 13:
        k2 |= (buffer[12] & 0xffL) << 32;
      case 12:
        k2 |= (buffer[11] & 0xffL) << 24;
      case 11:
        k2 |= (buffer[10] & 0xffL) << 16;
      case 10:
        k2 |= (buffer[9] & 0xffL) << 8;
      case 9:
        k2 |= buffer[8] & 0xffL;
        k2 *= c2;
        k2 = Long.rotateLeft(k2, 33);
        k2 *= c1;
        h2 ^= k2;
      case 8:
        k1 = (long) buffer[7] << 56;
      case 7:
        k1 |= (buffer[6] & 0xffL) << 48;
      case 6:
        k1 |= (buffer[5] & 0xffL) << 40;
      case 5:
        k1 |= (buffer[4] & 0xffL) << 32;
      case 4:
        k1 |= (buffer[3] & 0xffL) << 24;
      case 3:
        k1 |= (buffer[2] & 0xffL) << 16;
      case 2:
        k1 |= (buffer[1] & 0xffL) << 8;
      case 1:
        k1 |= buffer[0] & 0xffL;
        k1 *= c1;
        k1 = Long.rotateLeft(k1, 31);
        k1 *= c2;
        h1 ^= k1;
    }

    totalBytesHashed += bufferOffset;

    h1 ^= totalBytesHashed;
    h2 ^= totalBytesHashed;

    h1 += h2;
    h2 += h1;

    h1 = fmix64(h1);
    h2 = fmix64(h2);

    h1 += h2;
    h2 += h1;

    result.val1 = h1;
    result.val2 = h2;

    reset();
  }
}
