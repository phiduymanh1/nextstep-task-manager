package org.example.nextstepbackend.services.Mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String from;

  @Async("mailExecutor")
  @Retryable(retryFor = MailException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
  public void sendMail(String to, String subject, String content) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(content);

    mailSender.send(message);
    log.info("Mail sent to {}", to);
  }

  @Recover
  public void recover(MailException e, String to, String subject, String content) {
    log.error("Send mail FAILED to {}", to, e);
  }
}
