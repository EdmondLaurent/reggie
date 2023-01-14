package com.edmond.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
//  所有在类名上含有 Controller 和 RestController 的方法都会被捕获
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理异常的方法（通过反射获取异常信息）
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception) {
        log.error(exception.getMessage());
        //  1、处理 捕获到的sql唯一约束异常并进行统一处理
        if (exception.getMessage().contains("Duplicate entry")) {
            String[] split = exception.getMessage().split(" ");
            String exMsg = "当前系统中已存在：" + split[2];
            return R.error(exMsg);
        }
        //  2、若不是因为这个异常则抛出未知的错误
        return R.error("运行时出现了未知的错误...");
    }

}
