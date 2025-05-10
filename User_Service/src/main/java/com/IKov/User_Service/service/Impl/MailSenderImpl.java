package com.IKov.User_Service.service.Impl;

import com.IKov.User_Service.service.MailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailSenderImpl implements MailSender {

    private final JavaMailSender mailSender;

    @Override
    public void sendAuthConfirmation(String email, Integer confirmationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Auth Confirmation");
        message.setText("Your auth confirmation code is: " + confirmationCode);
        mailSender.send(message);
    }

    @Override
    public void sendNotification(String email, String notificationMessage) {

    }

}
