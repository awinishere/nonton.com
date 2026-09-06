package com.awn.tn.shared.email;

import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:dummy@gmail.com}")
    private String senderEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendEmail(EmailRequest request) {
        try {
            String htmlContent = renderTemplate(request.templateName(), request.model());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(senderEmail);
            helper.setTo(request.to());
            helper.setSubject(request.subject());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email successfully sent to: {}", request.to());

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", request.to(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String renderTemplate(String templateName, Object model) {
        try {
            TemplateConfiguration config = new TemplateConfiguration();
            MarkupTemplateEngine engine = new MarkupTemplateEngine(config);

            String templatePath = "/templates/" + templateName + ".groovy";

            try (Reader reader = new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream(templatePath), "Template not found at: " + templatePath),
                    StandardCharsets.UTF_8
            )) {
                StringWriter writer = new StringWriter();
                engine.createTemplate(reader)
                        .make(model != null ? (java.util.Map<?, ?>) model : Collections.emptyMap())
                        .writeTo(writer);
                return writer.toString();
            }

        } catch (Exception e) {
            log.error("Failed to render Groovy template: {}", templateName, e);
            throw new RuntimeException("Failed to render email template", e);
        }
    }
}