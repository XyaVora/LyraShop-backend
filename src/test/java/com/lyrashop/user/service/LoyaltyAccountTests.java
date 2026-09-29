package com.lyrashop.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class LoyaltyAccountTests {
    @Test
    void recordsDebtWhenReturnedOrderCoinsWereAlreadySpent() {
        LoyaltyAccount account = LoyaltyAccount.create(UUID.randomUUID());
        account.credit(100);
        account.debit(80);
        account.reverseCredit(100);
        assertThat(account.getCoinBalance()).isZero();
        assertThat(account.getCoinDebt()).isEqualTo(80);
        account.credit(50);
        assertThat(account.getCoinDebt()).isEqualTo(30);
        assertThat(account.getCoinBalance()).isZero();
        account.credit(40);
        assertThat(account.getCoinDebt()).isZero();
        assertThat(account.getCoinBalance()).isEqualTo(10);
    }

    @Test
    void dailyCheckInRepaysDebtBeforeIncreasingBalance() {
        LoyaltyAccount account = LoyaltyAccount.create(UUID.randomUUID());
        account.credit(20);
        account.reverseCredit(50);
        account.checkIn(LocalDate.of(2026, 9, 29), 100);
        assertThat(account.getCoinDebt()).isZero();
        assertThat(account.getCoinBalance()).isEqualTo(70);
    }
}
