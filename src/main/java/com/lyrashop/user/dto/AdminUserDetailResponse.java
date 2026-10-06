package com.lyrashop.user.dto;

import java.math.BigDecimal;
import java.util.List;
import com.lyrashop.order.dto.OrderResponse;
import com.lyrashop.address.dto.AddressResponse;

public record AdminUserDetailResponse(
        AdminUserResponse user,
        long loyaltyCoinBalance,
        long loyaltyCoinDebt,
        BigDecimal netPaidTotal,
        List<OrderResponse> orders,
        List<AddressResponse> addresses,
        List<CustomerSupportNoteResponse> supportNotes
) { }
