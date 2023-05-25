package co.istad.mbanking.api.user.validator.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;

import java.lang.annotation.*;


@Constraint(validatedBy = PasswordMatchConstraintValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PasswordMatch {

    String message() default "The password is not match!";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    String password();

    String confirmedPassword();


    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        PasswordMatch[] value();
    }
}










