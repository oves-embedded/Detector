package com.ov.producer.constants;


/**
 * @author xch
 * @title: ResultEnum
 * @projectName wdsacloud
 * @description:
 * @date 2021/6/318:31
 */
public enum ResultEnum {
    SUCCESS("1", "请求成功"),
    FAIL("0", "失败");

    // -------------------失败状态码----------------------

    private String code;
    private String message;

    ResultEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
