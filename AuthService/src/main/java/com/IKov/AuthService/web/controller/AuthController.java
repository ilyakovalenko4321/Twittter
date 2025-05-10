package com.IKov.AuthService.web.controller;


import com.IKov.AuthService.entity.exceptions.TagNotPresentException;
import com.IKov.AuthService.service.Impl.AuthService;
import com.IKov.AuthService.service.Impl.JwtServiceImpl;
import com.IKov.AuthService.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestParam String token, @RequestBody Map<String,Object> body){

        String tag;
        if (body.containsKey("tag")) {
            tag = (String) body.get("tag");
        } else if (body.containsKey("userTag")) {
            tag = (String) body.get("userTag");
        } else {
            throw new TagNotPresentException("Tag not present in the object");
        }

        boolean isVerified = authService.validate(token, tag);
        if(isVerified){
            return ResponseEntity.ok().build();
        }else{
            jwtService.logout(token, "");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("access validation failed");
        }
    }

}
