package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

//@ControllerAdvice
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //处理自定义异常异常
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order(0)
    public RestErrorResponse customException(XueChengPlusException e){
        //记录日志
        log.error("系统异常：{}",e.getMessage(),e);
        //解析异常信息
        String message = e.getMessage();
        return new RestErrorResponse(message);
    }


    //处理系统异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e){
        //记录日志
        log.error("系统异常：{}",e.getMessage(),e);
        //解析异常信息
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    //处理参数异常MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodArgValidException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        //存放错误信息
        List<String> errors = new ArrayList<>();
        bindingResult.getFieldErrors().forEach(item -> errors.add(item.getDefaultMessage()));
        String error = StringUtils.join(errors, ",");

        //记录日志
        log.error("系统异常：{}",e.getMessage(),e);
        //解析异常信息
        String message = e.getMessage();
        return new RestErrorResponse(error);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order(1)
    public RestErrorResponse sqlException(SQLIntegrityConstraintViolationException e){
        //记录日志
        log.error("系统异常：{}",e.getMessage(),e);
        String message = e.getMessage();
        if (message.contains("course_teacher.courseid_teacherId_unique")){
            return new RestErrorResponse(CommonError.DUPLICATE_KEY.getErrMessage());
        }
        //解析异常信息
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }
}

