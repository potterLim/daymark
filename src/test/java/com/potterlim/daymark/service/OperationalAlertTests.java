package com.potterlim.daymark.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationalAlertTests {

    @Test
    void constructorShouldExposeTypedAlertMessage() {
        OperationalAlert operationalAlert = new OperationalAlert(
            EOperationalAlertType.WEEKLY_OPERATIONS_SUMMARY_FAILED,
            OperationalAlertMessage.create("weekStart=2026-06-08")
        );

        assertThat(operationalAlert.getAlertType())
            .isEqualTo(EOperationalAlertType.WEEKLY_OPERATIONS_SUMMARY_FAILED);
        assertThat(operationalAlert.getMessage()).isEqualTo("weekStart=2026-06-08");
    }

    @Test
    void messageShouldTreatNullValueAsEmptyMessage() {
        OperationalAlertMessage alertMessage = OperationalAlertMessage.create(null);

        assertThat(alertMessage.getValue()).isEmpty();
    }

    @Test
    void constructorShouldRejectNullMessage() {
        assertThatThrownBy(() -> new OperationalAlert(
            EOperationalAlertType.WEEKLY_OPERATIONS_SUMMARY_FAILED,
            null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("message must not be null.");
    }
}
