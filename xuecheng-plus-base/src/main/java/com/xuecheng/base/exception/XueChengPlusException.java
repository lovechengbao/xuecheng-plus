package com.xuecheng.base.exception;

public class XueChengPlusException extends RuntimeException{

    private String errMessage;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public static void cast(String message){
        throw new XueChengPlusException(message);
    }

    public static void cast(CommonError error){
        throw new XueChengPlusException(error.getErrMessage());
    }

}
