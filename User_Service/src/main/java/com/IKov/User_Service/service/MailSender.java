package com.IKov.User_Service.service;

public interface MailSender {

    void sendAuthConfirmation(String email, Integer confirmationCode);

    void sendNotification(String email, String notificationMessage);

}
