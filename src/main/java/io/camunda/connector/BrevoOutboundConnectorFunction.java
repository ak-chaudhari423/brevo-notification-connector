package io.camunda.connector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.model.NotificationRequest;
import io.camunda.connector.model.NotificationResponse;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

@OutboundConnector(
        name = "Brevo Notification Connector",
        inputVariables = {"apiKey", "notificationType", "sender", "receiver", "messageBody"},
        type = "brevo-notification-connector"
)
@ElementTemplate(
        id = "io.camunda.connector.brevotemplate.v1",
        name = "Brevo Notification Connector",
        version = 1,
        description = "Send Email / SMS / WhatsApp via Brevo",
        inputDataClass = NotificationRequest.class
)
public class BrevoOutboundConnectorFunction implements OutboundConnectorFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrevoOutboundConnectorFunction.class);

    private static final String BASE_URL = "https://api.brevo.com/v3";
    private static final String SUCCESS = "success";
    private static final String EMAIL = "email";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public Object execute(OutboundConnectorContext context) throws Exception {
        NotificationRequest request = context.bindVariables(NotificationRequest.class);

        LOGGER.info("Received request: {}", request);

        return routeNotification(request);
    }

    private Map<String, Object> routeNotification(NotificationRequest req) {

        String type = req.notificationType();

        if (type == null) {
            return Map.of(SUCCESS, false, "message", "notificationType is required");
        }

        return switch (type.toLowerCase()) {
            case EMAIL -> sendEmail(req);
            case "sms" -> sendSms(req);
            case "whatsapp" -> sendWhatsapp(req);
            default -> Map.of(SUCCESS, false, "message", "Invalid notificationType");
        };
    }


    private Map<String, Object> sendEmail(NotificationRequest req) {

        Map<String, Object> payload = Map.of(
                "sender", Map.of(EMAIL, req.sender(), "name", "System"),
                "to", List.of(Map.of(EMAIL, req.receiver())),
                "subject", "Notification",
                "htmlContent", req.messageBody()
        );

        return executePost("/smtp/email", payload, req.apiKey());
    }


    private Map<String, Object> sendSms(NotificationRequest req) {

        Map<String, Object> payload = Map.of(
                "sender", req.sender(),
                "recipient", req.receiver(),
                "content", req.messageBody()
        );

        return executePost("/transactionalSMS/sms", payload, req.apiKey());
    }


    private Map<String, Object> sendWhatsapp(NotificationRequest req) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("senderNumber", req.sender());
        payload.put("contactNumbers", List.of(req.receiver()));
        payload.put("text", req.messageBody());

        return executePost("/whatsapp/sendMessage", payload, req.apiKey());
    }

    private Map<String, Object> executePost(String uri, Map<String, Object> payload, String apiKey) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            String requestBody = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + uri))
                    .header("Content-Type", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String body = response.body();
          //  LOGGER.info("Brevo API Response: {}", body);

            if (status >= 400) {
                LOGGER.error("Brevo API error: {}", body);
                return Map.of(SUCCESS, false, "error", body);
            }

            Map<String, Object> responseMap =
                    mapper.readValue(body, new TypeReference<>() {});

            return Map.of(SUCCESS, true, "response", responseMap);

        } catch (Exception e) {
            LOGGER.error("Error calling Brevo API", e);
            return Map.of(SUCCESS, false, "error", e.getMessage());
        }
    }
}

//@OutboundConnector(
//        name = "Brevo Notification OutBound Connector",
//        inputVariables = {"apiKey", "notificationType","sender","receiver", "messageBody"},
//        type = "io.camunda:template:1")
//@ElementTemplate(
//        id = "brevooutboundtemplate",
//        name = "Brevo Notification OutBound Connector",
//        version = 1,
//        description = "This custom connector links Camunda 8 to IBM MQ, allowing workflows to publish and consume messages for real-time, event-driven process automation.",
//        inputDataClass = NotificationRequest.class)
//public class BrevoOutboundConnectorFunction  implements OutboundConnectorFunction {
//
//    private static final Logger LOGGER =  LoggerFactory.getLogger(BrevoOutboundConnectorFunction.class);
//
//    @Override
//    public Object execute(OutboundConnectorContext context) throws Exception {
//        final var connectorRequest = context.bindVariables(NotificationRequest.class);
//        return executeConnector(connectorRequest);
//    }
//
//    public Map<String, Object> sendEmail(EmailRequest req) {
//        Map<String, Object> payload = Map.of(
//                "sender", Map.of("email", req.getSenderEmail(),
//                        "name", req.getSenderName()),
//                "to", List.of(Map.of("email", req.getRecipientEmail())),
//                "subject", req.getSubject(),
//                "htmlContent", req.getHtmlContent()
//        );
//
//        return executePost("/smtp/email", payload);
//    }
//
//    public Map<String, Object> sendSms(SmsRequest req) {
//        Map<String, Object> payload = Map.of(
//                "sender", req.getSender(),
//                "recipient", req.getRecipient(),
//                "content", req.getContent()
//        );
//
//        return executePost("/transactionalSMS/sms", payload);
//    }
//
//    public Map<String, Object> sendWhatsapp(WhatsappRequest req) {
//        if (req.getRecipientNumber() == null || req.getRecipientNumber().isBlank()) {
//            return Map.of("success", false, "message", "recipientNumber required");
//        }
//
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("senderNumber", req.getSenderNumber());
//        payload.put("contactNumbers", List.of(req.getRecipientNumber()));
//
//       if (req.getMessageText() != null && !req.getMessageText().isBlank()) {
//            payload.put("text", req.getMessageText());
//        } else {
//            return Map.of("success", false, "message", "Either templateId or messageText is required");
//        }
//
//        log.info("Sending WhatsApp message via Brevo API to {}", req.getRecipientNumber());
//
//        return executePost("/whatsapp/sendMessage", payload);
//    }
//
//    private Map<String, Object> executePost(String uri, Map<String, Object> payload) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//
//            String requestBody = mapper.writeValueAsString(payload);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + uri))
//                    .header("Content-Type", "application/json")
//                    .header("api-key", apiKey)
//                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(
//                    request,
//                    HttpResponse.BodyHandlers.ofString()
//            );
//
//            int statusCode = response.statusCode();
//            String body = response.body();
//
//            if (statusCode >= 400) {
//                LOGGER.error("Brevo API error: {}", body);
//                return Map.of("success", false, "error", body);
//            }
//
//            Map<String, Object> responseMap =
//                    mapper.readValue(body, new TypeReference<Map<String, Object>>() {});
//
//            return Map.of("success", true, "response", responseMap);
//
//        } catch (Exception e) {
//            LO.error("Error calling Brevo API: {}", e.getMessage(), e);
//            return Map.of("success", false, "error", e.getMessage());
//        }
//    }
//}
