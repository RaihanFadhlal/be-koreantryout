package com.enigma.tekor.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
