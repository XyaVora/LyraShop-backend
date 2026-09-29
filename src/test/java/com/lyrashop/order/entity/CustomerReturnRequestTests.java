package com.lyrashop.order.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class CustomerReturnRequestTests {
    @Test
    void enforcesReturnWorkflow() {
        CustomerReturnRequest request = CustomerReturnRequest.create(
                UUID.randomUUID(), UUID.randomUUID(), "Sản phẩm lỗi");

        request.approve();
        request.receive();
        request.recordRefund(false);
        assertThat(request.getStatus()).isEqualTo("PARTIALLY_REFUNDED");
        request.recordRefund(true);
        assertThat(request.getStatus()).isEqualTo("REFUNDED");
        assertThatThrownBy(request::approve).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allowsRejectOnlyWhileRequested() {
        CustomerReturnRequest request = CustomerReturnRequest.create(
                UUID.randomUUID(), UUID.randomUUID(), "Đổi ý");
        request.reject();
        assertThat(request.getStatus()).isEqualTo("REJECTED");
        assertThatThrownBy(request::receive).isInstanceOf(IllegalStateException.class);
    }
}
