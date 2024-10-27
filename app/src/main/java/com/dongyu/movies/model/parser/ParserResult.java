package com.dongyu.movies.model.parser;


public class ParserResult<T> {

    private boolean isSuccess;

    private String msg;

    private T data;

    public boolean isOk() {
        return isSuccess && data != null;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ParserResult(boolean isSuccess, String msg, T data) {
        this.isSuccess = isSuccess;
        this.msg = msg;
        this.data = data;
    }

    public static<T> ParserResult<T> success(T data) {
        return success(data, null);
    }

    public static<T> ParserResult<T> success(T data, String msg) {
        return new ParserResult<>(true, msg, data);
    }

    public static<T> ParserResult<T> error(String msg) {
        return new ParserResult<>(false, msg, null);
    }

    public static<T> ParserResult<T> error() {
        return error("error");
    }

}
