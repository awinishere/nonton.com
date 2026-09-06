package com.awn.tn.shared.email;

import lombok.Builder;

import java.util.Map;
import java.util.Objects;

@Builder
public record EmailRequest(
        String to,
        String subject,
        String templateName,
        Map<String, Object> model
) {
    public EmailRequest {
        Objects.requireNonNull(to, "Recipient 'to' email cannot be null");
        Objects.requireNonNull(subject, "Email 'subject' cannot be null");
        Objects.requireNonNull(templateName, "Email 'templateName' cannot be null");

        model = (model == null) ? Map.of() : Map.copyOf(model);
    }
}
