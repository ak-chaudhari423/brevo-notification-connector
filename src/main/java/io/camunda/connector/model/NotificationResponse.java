package io.camunda.connector.model;


import java.util.Map;

public record NotificationResponse(
        Map<String, Object> response
) {}