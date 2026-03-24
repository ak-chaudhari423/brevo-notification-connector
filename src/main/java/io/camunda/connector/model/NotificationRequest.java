package io.camunda.connector.model;

import io.camunda.connector.generator.java.annotation.TemplateProperty;
import jakarta.validation.constraints.NotEmpty;

public record NotificationRequest(

        @NotEmpty
        @TemplateProperty(
                group = "authentication",
                label = "API Key",
                description = "Brevo API Key"
        )
        String apiKey,

        @TemplateProperty(
                group = "notification",
                label = "Notification Type",
                description = "Select notification type",
                choices = {
                        @TemplateProperty.DropdownPropertyChoice(label = "Email", value = "email"),
                        @TemplateProperty.DropdownPropertyChoice(label = "SMS", value = "sms"),
                        @TemplateProperty.DropdownPropertyChoice(label = "WhatsApp", value = "whatsapp")
                }
        )
        String notificationType,

        @NotEmpty
        @TemplateProperty(
                group = "notification",
                label = "Sender",
                description = "Sender email or number"
        )
        String sender,

        @NotEmpty
        @TemplateProperty(
                group = "notification",
                label = "Receiver",
                description = "Receiver email or phone number"
        )
        String receiver,

        @NotEmpty
        @TemplateProperty(
                group = "notification",
                label = "Message Body",
                description = "Content of the message"
        )
        String messageBody

) {}