package co.istad.mbanking.api.user.validator.password;

import co.istad.mbanking.api.user.validator.email.EmailUniqueConstraintsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordConstrainValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Password {

    String message() default "Your password is so weak!";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
