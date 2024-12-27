package com.eridanimelo.user_api.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

}
