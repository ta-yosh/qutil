package jp.co.ascsystem.util;

public class halfnum {

  private static final byte HyphenBytes[] =
         { 0x22, 0x12, 0x00, 0x2D, 0x20, 0x10, 0x30, (byte)0xFC, (byte)
           0xFF, 0x0D };

  public static String convert(String bfrRec) {

    String Str, HyphenStr;
    char Chr;
    StringBuffer StrBuff = new StringBuffer();
    byte b[];
    int i, j, HyphenLen;
    try {
      HyphenStr = new String(HyphenBytes, "UTF-16BE");
      HyphenLen = HyphenStr.length();

      for (i = 0; i < bfrRec.length(); i ++) {

        Chr = bfrRec.charAt(i);
        Str = Character.toString(Chr);

        if (Str.matches("[[£Á-£Ú][£á-£ú][£°-£¹]]")) {
          b = Str.getBytes("UTF-16BE");
          b[0] = 0;
          b[1] += (byte)0x20;
          StrBuff.append(new String(b, "UTF-16BE"));
        }
        else {
          for (j = 0; j < HyphenLen; j ++)
          if (HyphenStr.charAt(j) == Chr) break;
          if (j == HyphenLen) StrBuff.append(Chr);
          else StrBuff.append("-");
        }
      }
      return StrBuff.toString();

    } catch (Exception e) {
      return bfrRec;     
    }

  }
}
