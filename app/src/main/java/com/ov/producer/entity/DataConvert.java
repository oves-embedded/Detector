package com.ov.producer.entity;

import android.text.TextUtils;

import com.ov.producer.utils.ByteUtil;
import com.ov.producer.utils.ByteUtils;

import java.nio.charset.StandardCharsets;

public class DataConvert {

    public static Object convert2Obj(byte[] b, int valType) {
        if (b != null || b.length > 0) {
            switch (valType) {
                case 0:
                    return ByteUtils.byte2int(new byte[]{0x00, 0x00, b[1], b[0]});
                case 1:
                    return ByteUtils.byte2short(ByteUtils.reversalBytes(b));
                case 2:
                case 3:
                    return ByteUtils.byte2int(ByteUtils.reversalBytes(b));
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
                    byte[] bytes = ByteUtils.short2byte(Integer.valueOf(value));
                    return ByteUtils.reversalBytes(bytes);
                case 1:
                    return ByteUtils.reversalBytes(ByteUtils.short2byte(Integer.valueOf(value)));
                case 2:
                case 3:
                    return ByteUtils.reversalBytes(ByteUtils.int2byte(Integer.valueOf(value)));
                case 4:
                    break;
                case 5:
                    return value.getBytes(StandardCharsets.US_ASCII);
            }
        }
        return null;
    }
}
