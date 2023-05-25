package co.istad.mbanking.api.auth.web;

import co.istad.mbanking.api.user.validator.email.EmailUnique;
import co.istad.mbanking.api.user.validator.password.Password;
import co.istad.mbanking.api.user.validator.password.PasswordMatch;
import co.istad.mbanking.api.user.validator.role.RoleIdConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@PasswordMatch(password = "password", confirmedPassword = "confirmedPassword")
public record RegisterDto(
        @NotBlank(message = "Email is required")
        @EmailUnique
        @Email
        String email,
        @NotBlank(message = "Password is required")
        @Password
        String password,
        @NotBlank(message = "ConfirmedPassword is required")
        @Password
        String confirmedPassword,
        @NotNull(message = "The field roles is required.")
        @RoleIdConstraint
        List<Integer> roleIds) {
}
