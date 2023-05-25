package co.istad.mbanking.api.user.validator.email;

import co.istad.mbanking.api.user.UserMapper;
import co.istad.mbanking.api.user.validator.email.EmailUnique;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmailUniqueConstraintsValidator implements ConstraintValidator<EmailUnique, String> {
     private final UserMapper userMapper;
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return !userMapper.existsByEmail(email);
    }
}
