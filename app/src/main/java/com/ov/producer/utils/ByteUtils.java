package com.ov.producer.utils;

public class ByteUtils {
    public static byte[] rtrimBytes(byte[] src) {

        int i = src.length - 1;
        for (; i >= 0; i--) {
            if (src[i] != 0) {
                break;
            }
        }
        if (i == src.length - 1) {
            return src;
        }
        if (i == -1) {
            return new byte[0];
        }
        byte[] tmp = new byte[i + 1];
        System.arraycopy(src, 0, tmp, 0, i + 1);
        return tmp;
    }

    public static byte[] ltrimBytes(byte[] src) {

        int i = 0;
        for (; i < src.length; i++) {
            if (src[i] != 0) {
                break;
            }
        }
        if (i == src.length) {
            return new byte[0];
        }
        ;

        if (i == 0) {
            return src;
        }
        ;

        byte[] tmp = new byte[src.length - i];
        System.arraycopy(src, i, tmp, 0, src.length - i);
        return tmp;
    }

    /**
     * void
     * 2019年10月24日
     */
    public static void rfillBytes(byte[] src, int len, byte[] bytes, int offset) {
        if (src.length == len) {
            System.arraycopy(src, 0, bytes, offset, len);
        } else if (src.length > len) {
            System.arraycopy(src, 0, bytes, offset, len);
        } else {
            System.arraycopy(src, 0, bytes, offset, src.length);
            for (int i = offset + src.length; i < offset + len; i++) {
                bytes[i] = 0;
            }
        }
    }


    public static byte[] intToBytes(int i) {
        byte abyte0[] = new byte[2];
        abyte0[1] = (byte) (0xff & i);
        abyte0[0] = (byte) ((0xff00 & i) >> 8);
        return abyte0;
    }

    public static byte intToByte(int i) {
        return (byte) i;
    }

    /**
     * 8 bytes to long
     *
     * @param abyte0
     * @return
     */
    public static long Bytes8ToLong(byte abyte0[]) {
        long ret = 0;

        ret = (0xffL & abyte0[0]) << 56 | (0xffL & abyte0[1]) << 48 | (0xffL & abyte0[2]) << 40
                | (0xffL & abyte0[3]) << 32 | (0xffL & abyte0[4]) << 24 | (0xffL & abyte0[5]) << 16
                | (0xffL & abyte0[6]) << 8 | 0xffL & abyte0[7];

        return ret;
    }

    /**
     * Long占8个字节
     *
     * @param i
     * @return
     */
    public static byte[] longToBytes8(long i) {
        byte abyte0[] = new byte[8];
        abyte0[7] = (byte) (0xffL & i);
        abyte0[6] = (byte) ((0xff00L & i) >> 8);
        abyte0[5] = (byte) ((0xff0000L & i) >> 16);
        abyte0[4] = (byte) ((0xff000000L & i) >> 24);
        abyte0[3] = (byte) ((0xff00000000L & i) >> 32);
        abyte0[2] = (byte) ((0xff0000000000L & i) >> 40);
        abyte0[1] = (byte) ((0xff000000000000L & i) >> 48);
        abyte0[0] = (byte) ((0xff00000000000000L & i) >> 56);
        return abyte0;
    }

    public static short byte2ToShort(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }

    public static short byte2short(byte b[], int offset) {
        return (short) ((b[offset + 1] & 0xff) | (b[offset + 0] & 0xff) << 8);
    }

    public static short byte2short(byte b[]) {
        return (short) ((b[1] & 0xff) | (b[0] & 0xff) << 8);
    }

    public static int byte2int(byte b[], int offset) {
        return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8 | (b[offset + 1] & 0xff) << 16
                | (b[offset] & 0xff) << 24;
    }

    public static int byte2int(byte b[]) {
        return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
    }

    public static long byte2long(byte b[]) {
        return (long) b[7] & (long) 255 | ((long) b[6] & (long) 255) << 8 | ((long) b[5] & (long) 255) << 16
                | ((long) b[4] & (long) 255) << 24 | ((long) b[3] & (long) 255) << 32 | ((long) b[2] & (long) 255) << 40
                | ((long) b[1] & (long) 255) << 48 | (long) b[0] << 56;
    }

    public static long byte2long(byte b[], int offset) {
        return (long) b[offset + 7] & (long) 255 | ((long) b[offset + 6] & (long) 255) << 8
                | ((long) b[offset + 5] & (long) 255) << 16 | ((long) b[offset + 4] & (long) 255) << 24
                | ((long) b[offset + 3] & (long) 255) << 32 | ((long) b[offset + 2] & (long) 255) << 40
                | ((long) b[offset + 1] & (long) 255) << 48 | (long) b[offset] << 56;
    }

    public static byte[] int2byte(int n) {
        byte b[] = new byte[4];
        b[0] = (byte) (n >> 24);
        b[1] = (byte) (n >> 16);
        b[2] = (byte) (n >> 8);
        b[3] = (byte) n;
        return b;
    }

    public static void int2byte(int n, byte buf[], int offset) {
        buf[offset] = (byte) (n >> 24);
        buf[offset + 1] = (byte) (n >> 16);
        buf[offset + 2] = (byte) (n >> 8);
        buf[offset + 3] = (byte) n;
    }

    public static byte[] short2byte(int n) {
        byte b[] = new byte[2];
        b[0] = (byte) (n >> 8);
        b[1] = (byte) n;
        return b;
    }

    public static void short2byte(int n, byte buf[], int offset) {
        buf[offset] = (byte) (n >> 8);
        buf[offset + 1] = (byte) n;
    }

    public static byte[] shortToBytes2(short t) {
        byte[] b = new byte[2];
        b[0] = (byte) (t >> 8 & 0xFF);
        b[1] = (byte) (t & 0xFF);
        return b;
    }

    public static byte[] ensureLength(byte[] array, int minLength, int padding) throws Exception {
        if (minLength < 0 || padding < 0)
            throw new Exception();
        if (array.length == minLength)
            return array;
        return array.length > minLength ? copyOf(array, minLength) : copyOf(array, minLength + padding);
    }

    private static byte[] copyOf(byte[] original, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, length));
        return copy;
    }

    public static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }

    /**
     * @param srcAbyte
     * @param destAbyte
     * @param srcFrom:  srcindex
     * @param srcTo
     * @param destFrom
     */
    public static void bytesCopy(byte srcAbyte[], byte destAbyte[], int srcFrom, int srcTo, int destFrom) {
        // check null
        if (srcAbyte == null || destAbyte == null) {
            return;
        }
        // copy
        int i1 = 0;
        for (int l = srcFrom; l <= srcTo; l++) {
            if (destFrom + i1 >= destAbyte.length) {
                break;
            }
            if (l >= srcAbyte.length) {
                break;
            }
            destAbyte[destFrom + i1] = srcAbyte[l];
            i1++;
        }
    }

    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    /**
     * 16进制的字符串表示转成字节数组
     *
     * @param hexString 16进制格式的字符串
     * @return 转换后的字节数组
     **/
    public static byte[] toByteArray(String hexString) {
        if (hexString == null || hexString.length() <= 0)
            throw new IllegalArgumentException("this hexString must not be empty");

        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {// 因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    /**
     * 字节数组转成16进制表示格式的字符串
     *
     * @param byteArray 需要转换的字节数组
     * @return 16进制表示格式的字符串
     **/
    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException("this byteArray must not be null or empty");

        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) < 0x10)// 0~F前面不零
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return hexString.toString().toLowerCase();
    }


    public static byte[] hex2byte(String hex) {
        String digital = "0123456789ABCDEF";
        String hex1 = hex.replace(" ", "");
        char[] hex2char = hex1.toCharArray();
        byte[] bytes = new byte[hex1.length() / 2];
        byte temp;
        for (int p = 0; p < bytes.length; p++) {
            temp = (byte) (digital.indexOf(hex2char[2 * p]) * 16);
            temp += digital.indexOf(hex2char[2 * p + 1]);
            bytes[p] = (byte) (temp & 0xff);
        }
        return bytes;
    }


    /**
     * 获取数组的抑或值
     *
     * @param datas
     * @return
     */
    public static byte getElseOr(byte[] datas) {
        byte temp = datas[0];
        for (int i = 1; i < datas.length; i++) {
            temp ^= datas[i];
        }
        return temp;
    }






    public static void shortToByte_LH(short shortVal, byte[] b, int offset) {
        b[0 + offset] = (byte) (shortVal & 0xff);
        b[1 + offset] = (byte) (shortVal >> 8 & 0xff);
    }

    public static short byteToShort_HL(byte[] b, int offset)
    {
        short result;
        result = (short)((((b[offset + 1]) << 8) & 0xff00 | b[offset] & 0x00ff));
        return result;
    }

    public static void intToByte_LH(int intVal, byte[] b, int offset) {
        b[0 + offset] = (byte) (intVal & 0xff);
        b[1 + offset] = (byte) (intVal >> 8 & 0xff);
        b[2 + offset] = (byte) (intVal >> 16 & 0xff);
        b[3 + offset] = (byte) (intVal >> 24 & 0xff);
    }

    public static int byteToInt_HL(byte[] b, int offset)
    {
        int result;
        result = (((b[3 + offset] & 0x00ff) << 24) & 0xff000000)
                | (((b[2 + offset] & 0x00ff) << 16) & 0x00ff0000)
                | (((b[1 + offset] & 0x00ff) << 8) & 0x0000ff00)
                | ((b[0 + offset] & 0x00ff));
        return result;
    }


    public static byte[] reversalBytes(byte[] bytes){
        if (bytes.length < 2){
            return bytes;
        }
        byte tmp;
        for (int i = 0; i < bytes.length/2; i++) {
            tmp = bytes[i];
            bytes[i] = bytes[bytes.length-1-i];
            bytes[bytes.length-1-i]=tmp;
        }
        return  bytes;
    }

}
