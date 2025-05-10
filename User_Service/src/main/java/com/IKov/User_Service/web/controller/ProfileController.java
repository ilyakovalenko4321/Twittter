package com.IKov.User_Service.web.controller;

import com.IKov.User_Service.entity.Profile.Profile;
import com.IKov.User_Service.entity.confirmation.ConfirmationCode;
import com.IKov.User_Service.service.ProfileService;
import com.IKov.User_Service.web.dto.ProfileDto;
import com.IKov.User_Service.web.mapping.ProfileMapping;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileMapping profileMapping;

    @PostMapping("/public/create")
    public ResponseEntity<Void> saveProfile(@Valid @RequestBody ProfileDto profileDto) {
        Profile profile = profileMapping.toEntity(profileDto);
        profileService.saveProfile(profile);
        return ResponseEntity
                .status(HttpStatus.CREATED).build();
    }

    @PostMapping("/public/confirm")
    public ResponseEntity<Void> confirmProfile(@Valid @RequestBody ConfirmationCode confirmationCodeEntity) {
        String email = confirmationCodeEntity.getEmail();
        Integer confirmationCode = confirmationCodeEntity.getConfirmationCode();
        profileService.confirmProfile(email, confirmationCode);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
