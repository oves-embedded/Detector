package com.ov.producer.entity;

import android.text.TextUtils;

import com.ov.producer.utils.ByteUtil;

import java.nio.charset.StandardCharsets;

public class DataConvert {

    public static Object convert2Obj(byte[] b, int valType) {
        if (b != null || b.length > 0) {
            switch (valType) {
                case 0:
                    return ByteUtil.byte2int(new byte[]{0x00, 0x00, b[1], b[0]});
                case 1:
                    return ByteUtil.byte2short(ByteUtil.reverse(b));
                case 2:
                case 3:
                    return ByteUtil.byte2int(ByteUtil.reverse(b));
                case 4:
                    break;
                case 5:
                    return new String(b, StandardCharsets.US_ASCII).trim();
            }
        }
        return null;
    }

    public static byte[] convert2Arr(String value, int valType) {
        if (!TextUtils.isEmpty(value)) {
            switch (valType) {
                case 0:
                    byte[] bytes = ByteUtil.short2byte(Integer.valueOf(value));
                    return ByteUtil.reverse(bytes);
                case 1:
                    return ByteUtil.reverse(ByteUtil.short2byte(Integer.valueOf(value)));
                case 2:
                case 3:
                    return ByteUtil.reverse(ByteUtil.int2byte(Integer.valueOf(value)));
                case 4:
                    break;
                case 5:
                    return value.getBytes(StandardCharsets.US_ASCII);
            }
        }
        return null;
    }
}
