package cn.szz.netty.http.util;

import io.netty.buffer.ByteBuf;

/**
 * 公共方法
 *
 * @author Shi Zezhu
 * @date 2019年8月7日 下午6:59:15
 */
public final class CommUtils {

    private CommUtils() {
    }

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static boolean notNull(Object obj) {
        return !isNull(obj);
    }

    public static <T> T notNull(T obj, String msg) {
        if (isNull(obj))
            throw new NullPointerException(msg);
        return obj;
    }

    public static <T> T ifIsNullGet(T obj1, T obj2) {
        return notNull(obj1) ? obj1 : obj2;
    }

    public static boolean isEmpty(String text) {
        int strLen;
        if (text == null || (strLen = text.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean notEmpty(String text) {
        return !isEmpty(text);
    }

    public static String notEmpty(String text, String msg) {
        if (isEmpty(text))
            throw new IllegalArgumentException(msg);
        return text.trim();
    }

    public static String ifIsEmptyGet(String text1, String text2) {
        return notEmpty(text1) ? text1.trim() : notEmpty(text2) ? text2.trim() : text2;
    }

    public static boolean isInt(String value) {
        if (isEmpty(value)) return false;
        value = value.trim();
        int result = 0;
        int i = 0, len = value.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;
        if (len <= 0) return false;
        char firstChar = value.charAt(0);
        if (firstChar < '0') {
            if (firstChar == '-') {
                limit = Integer.MIN_VALUE;
            } else if (firstChar != '+') return false;
            if (len == 1) return false;
            i++;
        }
        multmin = limit / 10;
        while (i < len) {
            digit = Character.digit(value.charAt(i++), 10);
            if (digit < 0) return false;
            if (result < multmin) return false;
            result *= 10;
            if (result < limit + digit) return false;
            result -= digit;
        }
        return true;
    }

    public static boolean notInt(String value) {
        return !isInt(value);
    }

    public static int notInt(String value, String msg) {
        if (notInt(value))
            throw new NumberFormatException(msg);
        return Integer.parseInt(value);
    }

    public static byte[] byteBufToBytes(ByteBuf byteBuf) {
        if (isNull(byteBuf))
            return new byte[0];
        if (byteBuf.hasArray()) {
            return byteBuf.array();
        } else {
            byte[] dst = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(), dst);
            return dst;
        }
    }
}
