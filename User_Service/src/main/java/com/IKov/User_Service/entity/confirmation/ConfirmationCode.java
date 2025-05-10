package com.IKov.User_Service.entity.confirmation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ConfirmationCode {

    @Email
    private String email;
    @Pattern(regexp = "[0-9]{6}")
    private Integer confirmationCode;

}
