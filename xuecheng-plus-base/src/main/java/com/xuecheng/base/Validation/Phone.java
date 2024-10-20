package com.xuecheng.base.Validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented// 元注解
@Target({ElementType.FIELD})// 元注解
@Retention(RetentionPolicy.RUNTIME)// 元注解
@Constraint(validatedBy = {PhoneValidation.class}) // 指定提供校验规则的类
public @interface Phone {

    String message() default "号码必须为11位数字";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
