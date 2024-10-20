package com.xuecheng.base.Validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneValidation implements ConstraintValidator<Phone, String> {
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        return phone != null && phone.length() == 11;
    }
}
