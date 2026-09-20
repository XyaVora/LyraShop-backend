package com.lyrashop.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.lyrashop.order.entity.Voucher;
import com.lyrashop.order.repository.VoucherRedemptionRepository;
import com.lyrashop.order.repository.VoucherRepository;

class VoucherServiceTests {
    private static final UUID USER_ID = UUID.randomUUID();
    private final VoucherRepository repository = mock(VoucherRepository.class);
    private final VoucherRedemptionRepository redemptions = mock(VoucherRedemptionRepository.class);
    private final VoucherService vouchers = new VoucherService(repository, redemptions);

    @Test void appliesPercentageDiscountAndKeepsShippingRule() {
        stub("LYRA10", voucher("LYRA10", "discount", "PERCENT", "10", "0"));
        var quote = vouchers.quote(USER_ID, " lyra10 ", new BigDecimal("400000.00"));
        assertThat(quote.discountAmount()).isEqualByComparingTo("40000.00");
        assertThat(quote.shippingFee()).isEqualByComparingTo("30000.00");
        assertThat(quote.totalAmount()).isEqualByComparingTo("390000.00");
    }

    @Test void freesShippingWithoutChangingMerchandiseTotal() {
        stub("FREESHIP", voucher("FREESHIP", "shipping", "FREESHIP", "0", "0"));
        var quote = vouchers.quote(USER_ID, "FREESHIP", new BigDecimal("200000.00"));
        assertThat(quote.shippingFee()).isZero();
        assertThat(quote.totalAmount()).isEqualByComparingTo("200000.00");
    }

    @Test void rejectsFixedDiscountBelowMinimumSpend() {
        stub("LYRA50K", voucher("LYRA50K", "discount", "FIXED", "50000", "800000"));
        assertThatThrownBy(() -> vouchers.quote(USER_ID, "LYRA50K", new BigDecimal("799999.00")))
                .isInstanceOf(InvalidVoucherException.class);
    }

    private void stub(String code, Voucher voucher) {
        when(repository.findActiveByCode(eq(code), any(Instant.class))).thenReturn(Optional.of(voucher));
    }

    private Voucher voucher(String code, String type, String discountType, String value, String minimum) {
        Voucher voucher = mock(Voucher.class);
        when(voucher.getId()).thenReturn(UUID.randomUUID());
        when(voucher.getCode()).thenReturn(code);
        when(voucher.getLabel()).thenReturn(code);
        when(voucher.getType()).thenReturn(type);
        when(voucher.getDiscountType()).thenReturn(discountType);
        when(voucher.getDiscountValue()).thenReturn(new BigDecimal(value));
        when(voucher.getMinimumOrderAmount()).thenReturn(new BigDecimal(minimum));
        when(voucher.getPerUserLimit()).thenReturn(1);
        when(voucher.getTotalUsageLimit()).thenReturn(null);
        when(voucher.getEndsAt()).thenReturn(Instant.parse("2030-12-31T23:59:59Z"));
        return voucher;
    }
}
