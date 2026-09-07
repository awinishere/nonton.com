package com.awn.tn.shared;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "senderEmail", "noreply@movieticket.com");
    }

    @Test
    @DisplayName("Should send email successfully when template exists and inputs are valid")
    void sendEmail_Success() {
        EmailRequest request = new EmailRequest(
                "user@example.com",
                "OTP Verification Code",
                "otp-verification",
                Map.of("userName", "Awn", "otpCode", "123456")
        );

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        assertDoesNotThrow(() -> emailService.sendEmail(request));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should throw RuntimeException when template file is not found")
    void sendEmail_TemplateNotFound_ThrowsException() {
        EmailRequest request = new EmailRequest(
                "user@example.com",
                "Test Subject",
                "non-existent-template",
                Map.of()
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> emailService.sendEmail(request));

        assertEquals("Failed to render email template", exception.getMessage());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}