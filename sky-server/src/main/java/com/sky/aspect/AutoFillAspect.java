package com.sky.aspect;

import com.sky.annotaion.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotaion.AutoFill)")
    public void autoFillPointCut() {}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得注解中的value属性

        //获取SQL方法中的所有参数
        Object[] args = joinPoint.getArgs();
        //判断是否成功获取到了参数，若为空则直接返回，中断此方法执行
        if(args == null || args.length == 0){
            return;
        }
        //将参数中的第一个获取出来，默认第一个为主要的存储数据的参数
        Object entity = args[0];

        //获取当前时间，之后要使用
        LocalDateTime now = LocalDateTime.now();
        //在ThreadLocal中获取此时进行操作的人的id
        Long currentId = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT){
            //如果为INSERT语句需要插入四条数据
            try {
                //获取类中的四个set方法，不直接调用方法是因为这里的entity为Object类型，无法直接调用，需要用反射
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //调用set方法进行赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (operationType == OperationType.UPDATE) {
            //如果为UPDATE语句需要插入两条数据
            try {
                //获取类中的四个set方法，不直接调用方法是因为这里的entity为Object类型，无法直接调用，需要用反射
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //调用set方法进行赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}





















