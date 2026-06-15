package com.potterlim.daymark.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccountIdTests {

    @Test
    void ofShouldCreateUserAccountIdFromPositiveValue() {
        UserAccountId userAccountId = UserAccountId.of(7L);

        assertThat(userAccountId.getValue()).isEqualTo(7L);
    }

    @Test
    void ofShouldRejectNonPositiveValue() {
        assertThatThrownBy(() -> UserAccountId.of(0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("value must be positive.");
    }

    @Test
    void fromShouldRejectNullValue() {
        assertThatThrownBy(() -> UserAccountId.from(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("valueOrNull must not be null.");
    }
}
