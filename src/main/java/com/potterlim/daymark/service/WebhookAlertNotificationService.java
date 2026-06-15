package com.potterlim.daymark.service;

import java.util.Map;
import com.potterlim.daymark.config.DaymarkApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${daymark.operations.alert-webhook-url:}')")
public class WebhookAlertNotificationService implements IAlertNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookAlertNotificationService.class);

    private final RestClient mRestClient;
    private final DaymarkApplicationProperties mDaymarkApplicationProperties;

    public WebhookAlertNotificationService(DaymarkApplicationProperties daymarkApplicationProperties) {
        mRestClient = RestClient.create();
        mDaymarkApplicationProperties = daymarkApplicationProperties;
    }

    @Override
    public void sendOperationalAlert(OperationalAlert operationalAlert) {
        try {
            mRestClient.post()
                .uri(mDaymarkApplicationProperties.getOperations().getAlertWebhookUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                    "application", "daymark",
                    "alertType", operationalAlert.getAlertType().getCode(),
                    "message", operationalAlert.getMessage()
                ))
                .retrieve()
                .toBodilessEntity();
        } catch (RuntimeException runtimeException) {
            LOGGER.warn(
                "Operational alert delivery failed. alertType={}, message={}",
                operationalAlert.getAlertType().getCode(),
                operationalAlert.getMessage(),
                runtimeException
            );
        }
    }
}
