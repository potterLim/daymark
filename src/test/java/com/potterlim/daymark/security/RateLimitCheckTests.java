package com.potterlim.daymark.security;

import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimitCheckTests {

    @Test
    void ofShouldPreserveRateLimitPolicy() {
        RateLimitCheck rateLimitCheck = RateLimitCheck.of(
            RateLimitKey.create("login-ip:127.0.0.1"),
            RateLimitRequestLimit.of(10),
            Duration.ofMinutes(10L)
        );

        assertThat(rateLimitCheck.getKey()).isEqualTo("login-ip:127.0.0.1");
        assertThat(rateLimitCheck.getRequestLimit()).isEqualTo(10);
        assertThat(rateLimitCheck.getWindowDuration()).isEqualTo(Duration.ofMinutes(10L));
    }

    @Test
    void ofShouldRejectInvalidPolicyValues() {
        assertThatThrownBy(() -> RateLimitKey.create(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("value must not be blank.");

        assertThatThrownBy(() -> RateLimitRequestLimit.of(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("value must be positive.");

        assertThatThrownBy(() -> RateLimitCheck.of(
            RateLimitKey.create("key"),
            RateLimitRequestLimit.of(1),
            Duration.ZERO
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("windowDuration must be positive.");
    }

    @Test
    void ofShouldRejectNullTypedValues() {
        assertThatThrownBy(() -> RateLimitCheck.of(null, RateLimitRequestLimit.of(1), Duration.ofMinutes(1L)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("key must not be null.");

        assertThatThrownBy(() -> RateLimitCheck.of(RateLimitKey.create("key"), null, Duration.ofMinutes(1L)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("requestLimit must not be null.");
    }
}
