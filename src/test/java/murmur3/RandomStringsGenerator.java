package murmur3;

import java.util.Random;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public class RandomStringsGenerator {
  private final Random random = new Random();

  public String randomAscii(int length) {
    return random.ints(48, 122)
        .filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97))
        .mapToObj(i -> (char) i)
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
        .toString();
  }

  public String randomUnicode(int length) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int codePoint;
      do {
        int max = 0;
        switch (random.nextInt() & 0x3) {
          case 0:
            max = 0x80;
            break;   // 1 UTF8 bytes
          case 1:
            max = 0x800;
            break;  // up to 2 bytes
          case 2:
            max = 0xffff + 1;
            break; // up to 3 bytes
          case 3:
            max = Character.MAX_CODE_POINT + 1; // up to 4 bytes
        }

        codePoint = random.nextInt(max);
      } while (codePoint < 0xffff &&
               (Character.isHighSurrogate((char) codePoint) || Character.isLowSurrogate((char) codePoint)));

      sb.appendCodePoint(codePoint);
    }
    return sb.toString();
  }
}
