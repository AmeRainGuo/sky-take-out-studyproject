package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
* 自定义注解，用于标识需要自动填充的方法
*/
@Target(ElementType.METHOD)// 方法上使用
@Retention(RetentionPolicy.RUNTIME)// 运行时
public @interface AutoFill {
    OperationType value();// 操作类型  update insert

}
