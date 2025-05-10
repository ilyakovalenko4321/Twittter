package com.IKov.User_Service.web.dto;

import com.IKov.User_Service.entity.exception.PasswordConfirmationMismatchException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProfileDto {

    @NotNull(groups = {OnReceiving.class, OnSending.class})
    private String name;

    @Email(groups = {OnReceiving.class, OnSending.class})
    private String email;

    @Pattern(regexp = "^[a-zA-Z0-9@_-]{5,12}$", groups = {OnReceiving.class, OnSending.class})
    private String tag;

    @NotNull(groups = {OnReceiving.class})
    private String password;

    @NotNull(groups = {OnReceiving.class})
    private String passwordConfirmation;

    private String description;

    public ProfileDto(@NotNull String name, @Email String email, @Pattern(regexp = "^[a-zA-Z0-9@_-]{5,12}$") String tag, @NotNull String password, @NotNull String passwordConfirmation, String description) {
        this.name = name;
        this.email = email;
        this.tag = tag;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
        if(password.equals(passwordConfirmation)){
            throw new PasswordConfirmationMismatchException("Password and Password confirmation don't match");
        }
        this.description = description;
    }

    public ProfileDto() {
    }
}
