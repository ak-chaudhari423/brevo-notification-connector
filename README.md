# Brevo Notification Connector (Camunda 8)

## Overview

The **Brevo Notification Connector** is a custom outbound connector for Camunda 8 that enables workflows to send notifications via **Email, SMS, and WhatsApp** using the Brevo API.

This connector allows process automation to trigger real-time communication with users based on workflow events.

---

## Features

* Send Emails
* Send SMS
* Send WhatsApp Messages

---

## Connector UI

<p align="center">
  <img width="974" height="549" alt="Connector UI" src="https://github.com/user-attachments/assets/7a646457-66f1-45f7-9a8a-ba9c24c9b188" />
</p>

---

## Compatibility

* Works with Camunda SaaS
* Works with Self-Managed Camunda

---

## Setup Guide

### Setting Up Camunda SaaS

1. Navigate to **Camunda SaaS**
2. Create a cluster using the latest version
3. Open your cluster → go to **API** section
4. Click **Create New Client**
5. Select the **Zeebe** checkbox
6. Click **Create**
7. Copy configuration from **Spring Boot tab**
8. Paste into your `application.properties`

---

### Setting Up Self-Managed Environment

1. Setup Camunda Self-Managed using Docker:
   https://docs.camunda.io/docs/self-managed/setup/deploy/local/docker-compose/

2. Use the following endpoints:

```properties
camunda.client.zeebe.grpc-address=http://localhost:26500
camunda.client.zeebe.rest-address=http://localhost:8088
```

3. Uncomment these properties in your **test resources**

---

## Tools Required

* Download Desktop Modeler (if needed):
  https://camunda.com/download/modeler/

---

## Launching Your Connector

1. Start your connector runtime:

```
io.camunda.connector.brevo.LocalConnectorRuntime
```

2. Open:

   * Web Modeler OR Desktop Modeler

3. Create a new project

4. Upload connector template:
   https://github.com/ak-chaudhari423/brevo-notification-connector/blob/main/element-templates/brevo-template.json

---

## Using in Desktop Modeler

1. Go to:

```
Camunda Modeler → resources → element-templates
```

2. Paste the downloaded **Brevo Template JSON**

3. Restart Modeler

---

## Create BPMN Process

1. Create a new BPMN diagram

2. Add a **Service Task**

3. Select **Brevo Notification Connector**

4. Configure:

   * API Key
   * Notification Type
   * Sender
   * Receiver
   * Message

5. Deploy and run the process

---

## Notes

* Ensure correct API key is used
* Validate email/phone formats before execution
* Use secrets instead of hardcoding sensitive values

---

