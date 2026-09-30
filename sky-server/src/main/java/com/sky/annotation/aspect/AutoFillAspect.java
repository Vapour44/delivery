package com.sky.annotation.aspect;


import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class AutoFillAspect {

        @Around("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")

        public void around(ProceedingJoinPoint joinPoint) throws Throwable {
                // 获取方法签名
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                OperationType operationType = signature.getMethod().getAnnotation(com.sky.annotation.AutoFill.class).value();

                Object[] args = joinPoint.getArgs();
                Object entity = args[0];

                if(operationType == OperationType.INSERT){
                        Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                        Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                        Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                        Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                        setCreateTime.invoke(entity, LocalDateTime.now());
                        setUpdateTime.invoke(entity, LocalDateTime.now());
                        setCreateUser.invoke(entity, BaseContext.getCurrentId());
                        setUpdateUser.invoke(entity, BaseContext.getCurrentId());
                }

                if(operationType == OperationType.UPDATE){
                        Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                        Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                        setUpdateTime.invoke(entity, LocalDateTime.now());
                        setUpdateUser.invoke(entity, BaseContext.getCurrentId());
                }

                joinPoint.proceed();
        }
}
