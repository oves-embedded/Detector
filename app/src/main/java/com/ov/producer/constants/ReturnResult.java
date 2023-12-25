package com.ov.producer.constants;


import java.io.Serializable;

public class ReturnResult<T> implements Serializable {

    private static final long serialVersionUID = 4955324051940979035L;
    // 请求返回状态，1表示成功，0表示失败
//	@ApiModelProperty(value="请求返回状态，1表示成功，0表示失败")
    private Integer code;
    // 请求失败返回异常码
//	@ApiModelProperty(value="请求失败返回异常码")
    private String exceptionCode;
    // 请求失败返回异常信息
//	@ApiModelProperty(value="请求失败返回异常信息")
    private String exceptionMsg;
    // 请求返回的数据对象
//	@ApiModelProperty(value="返回数据")
    private T data;

    public boolean ok() {
        return this.code.intValue() == CommonConstant.REQ_SUCCESS.intValue();
    }


    public ReturnResult() {
    }

    public ReturnResult(Integer code, String exceptionCode, String exceptionMsg, T data) {
        this.code = code;
        this.exceptionCode = exceptionCode;
        this.exceptionMsg = exceptionMsg;
        this.data = data;
    }

    public static <T> ReturnResult<T> success() {
        return new ReturnResult<>(CommonConstant.REQ_SUCCESS, null, null, null);
    }

    public static <T> ReturnResult<T> success(T t) {
        return new ReturnResult<>(CommonConstant.REQ_SUCCESS, null, null, t);
    }

    public static <T> ReturnResult<T> success(T t, String msg) {
        return new ReturnResult<>(CommonConstant.REQ_SUCCESS, null, msg, t);
    }

    public static <T> ReturnResult<T> fail(String msg) {
        return new ReturnResult<>(CommonConstant.REQ_FAIL, null, msg, null);
    }

    public static <T> ReturnResult<T> fail() {
        return new ReturnResult<>(CommonConstant.REQ_FAIL, null, null, null);
    }

    public static <T> ReturnResult<T> fail(T t) {
        return new ReturnResult<>(CommonConstant.REQ_FAIL, null, null, t);
    }

    public static <T> ReturnResult<T> fail(T t, String msg) {
        return new ReturnResult<>(CommonConstant.REQ_FAIL, null, msg, t);
    }

    public static <T> ReturnResult<T> fail(ResultEnum resultEnum) {
        return new ReturnResult<>(CommonConstant.REQ_FAIL, resultEnum.getCode(), resultEnum.getMessage(), null);
    }


    /**
     * 构造方法
     *
     * @param code 请求返回状态
     * @param data 请求返回的数据对象
     */
    public ReturnResult(Integer code, T data) {
        super();
        this.code = code;
        this.data = data;
    }

    /**
     * @param data
     * @return ReturnResult
     * @Description:请求成功
     * @Note
     * @Author:xz
     * @Date:2021年5月24日 下午7:54:40
     * @Version:1.0
     */
    public ReturnResult(T data) {
        this(CommonConstant.REQ_SUCCESS, data);
    }

    public ReturnResult(Integer code, String exceptionMsg) {
        super();
        this.code = code;
        this.exceptionMsg = exceptionMsg;
    }

    public ReturnResult(String exceptionCode, String exceptionMsg) {
        super();
        this.code = CommonConstant.REQ_FAIL;
        this.data = null;
        this.exceptionCode = exceptionCode;
        this.exceptionMsg = exceptionMsg;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(String exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }

    public void setExceptionMsg(String exceptionMsg) {
        this.exceptionMsg = exceptionMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
