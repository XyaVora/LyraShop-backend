package com.lyrashop.order.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lyrashop.order.dto.AdminVoucherRequest;
import com.lyrashop.order.dto.AdminVoucherResponse;

import com.lyrashop.cart.service.StorePricing;
import com.lyrashop.order.dto.VoucherQuoteResponse;
import com.lyrashop.order.entity.Voucher;
import com.lyrashop.order.entity.VoucherRedemption;
import com.lyrashop.order.repository.VoucherRedemptionRepository;
import com.lyrashop.order.repository.VoucherRepository;

@Service
public class VoucherService {
    private final VoucherRepository vouchers;
    private final VoucherRedemptionRepository redemptions;

    public VoucherService(VoucherRepository vouchers, VoucherRedemptionRepository redemptions) {
        this.vouchers = vouchers;
        this.redemptions = redemptions;
    }

    public List<VoucherQuoteResponse> available(UUID userId, BigDecimal merchandiseTotal) {
        return vouchers.findCurrentlyActive(Instant.now()).stream()
                .map(voucher -> describe(voucher, userId, merchandiseTotal)).toList();
    }

    public VoucherQuoteResponse quote(UUID userId, String rawCode, BigDecimal merchandiseTotal) {
        Voucher voucher = vouchers.findActiveByCode(canonicalCode(rawCode), Instant.now())
                .orElseThrow(InvalidVoucherException::new);
        return requireEligible(voucher, userId, merchandiseTotal);
    }

    public VoucherQuoteResponse quoteForOrder(UUID userId, String rawCode, BigDecimal merchandiseTotal) {
        Voucher voucher = vouchers.findActiveByCodeForUpdate(canonicalCode(rawCode), Instant.now())
                .orElseThrow(InvalidVoucherException::new);
        return requireEligible(voucher, userId, merchandiseTotal);
    }

    public void recordRedemption(UUID userId, String code, UUID orderId) {
        if (code == null) return;
        Voucher voucher = vouchers.findActiveByCodeForUpdate(canonicalCode(code), Instant.now())
                .orElseThrow(InvalidVoucherException::new);
        redemptions.save(VoucherRedemption.create(voucher.getId(), userId, orderId));
    }

    public void release(UUID orderId) {
        redemptions.deleteByOrderId(orderId);
    }

    @Transactional(readOnly=true) public List<AdminVoucherResponse> adminList(){return vouchers.findAllByOrderByCodeAsc().stream().map(v->AdminVoucherResponse.from(v,redemptions.countByVoucherId(v.getId()))).toList();}
    @Transactional public AdminVoucherResponse adminCreate(AdminVoucherRequest r){String code=canonicalCode(r.code());if(vouchers.existsByCode(code))throw new InvalidVoucherException();validateAdminRequest(r);Voucher v=vouchers.saveAndFlush(Voucher.create(code,r.label().strip(),r.type(),r.discountType(),r.discountValue(),r.maxDiscountAmount(),r.minimumOrderAmount(),r.startsAt(),r.endsAt(),r.totalUsageLimit(),r.perUserLimit(),r.active()));return AdminVoucherResponse.from(v,0);}
    @Transactional public AdminVoucherResponse adminUpdate(UUID id,AdminVoucherRequest r){Voucher v=vouchers.findById(id).orElseThrow(InvalidVoucherException::new);validateAdminRequest(r);v.update(r.label().strip(),r.type(),r.discountType(),r.discountValue(),r.maxDiscountAmount(),r.minimumOrderAmount(),r.startsAt(),r.endsAt(),r.totalUsageLimit(),r.perUserLimit(),r.active());return AdminVoucherResponse.from(vouchers.saveAndFlush(v),redemptions.countByVoucherId(id));}
    @Transactional public void adminDeactivate(UUID id){Voucher v=vouchers.findById(id).orElseThrow(InvalidVoucherException::new);v.deactivate();vouchers.saveAndFlush(v);}
    private static void validateAdminRequest(AdminVoucherRequest r){if(!r.endsAt().isAfter(r.startsAt()))throw new InvalidVoucherException();if("PERCENT".equals(r.discountType())&&r.discountValue().compareTo(new BigDecimal("100"))>0)throw new InvalidVoucherException();if("FREESHIP".equals(r.discountType())&&r.discountValue().signum()!=0)throw new InvalidVoucherException();}

    public VoucherQuoteResponse noVoucher(BigDecimal merchandiseTotal) {
        BigDecimal shipping = StorePricing.shippingFee(merchandiseTotal);
        return new VoucherQuoteResponse(null, null, BigDecimal.ZERO.setScale(2), shipping,
                merchandiseTotal.add(shipping), null, null, BigDecimal.ZERO.setScale(2), true, null);
    }

    private VoucherQuoteResponse describe(Voucher voucher, UUID userId, BigDecimal merchandiseTotal) {
        try {
            return requireEligible(voucher, userId, merchandiseTotal);
        } catch (InvalidVoucherException exception) {
            BigDecimal amount = merchandiseTotal == null ? BigDecimal.ZERO.setScale(2) : merchandiseTotal;
            BigDecimal shipping = StorePricing.shippingFee(amount);
            return response(voucher, BigDecimal.ZERO.setScale(2), shipping, amount.add(shipping), false);
        }
    }

    private VoucherQuoteResponse requireEligible(Voucher voucher, UUID userId, BigDecimal merchandiseTotal) {
        if (merchandiseTotal == null || merchandiseTotal.signum() <= 0
                || merchandiseTotal.compareTo(voucher.getMinimumOrderAmount()) < 0 || limitReached(voucher, userId)) {
            throw new InvalidVoucherException();
        }
        BigDecimal shipping = StorePricing.shippingFee(merchandiseTotal);
        BigDecimal discount = switch (voucher.getDiscountType()) {
            case "PERCENT" -> merchandiseTotal.multiply(voucher.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            case "FIXED" -> voucher.getDiscountValue();
            case "FREESHIP" -> BigDecimal.ZERO.setScale(2);
            default -> throw new InvalidVoucherException();
        };
        if (voucher.getMaxDiscountAmount() != null) discount = discount.min(voucher.getMaxDiscountAmount());
        discount = discount.min(merchandiseTotal);
        if ("FREESHIP".equals(voucher.getDiscountType())) shipping = BigDecimal.ZERO.setScale(2);
        return response(voucher, discount, shipping, merchandiseTotal.subtract(discount).add(shipping), true);
    }

    private boolean limitReached(Voucher voucher, UUID userId) {
        return (voucher.getTotalUsageLimit() != null
                && redemptions.countByVoucherId(voucher.getId()) >= voucher.getTotalUsageLimit())
                || redemptions.countByVoucherIdAndUserId(voucher.getId(), userId) >= voucher.getPerUserLimit();
    }

    private VoucherQuoteResponse response(Voucher voucher, BigDecimal discount, BigDecimal shipping,
            BigDecimal total, boolean eligible) {
        String discountText = switch (voucher.getDiscountType()) {
            case "PERCENT" -> voucher.getDiscountValue().stripTrailingZeros().toPlainString() + "%";
            case "FIXED" -> voucher.getDiscountValue().divide(new BigDecimal("1000"), 0, RoundingMode.DOWN).toPlainString() + "K";
            default -> "Freeship";
        };
        return new VoucherQuoteResponse(voucher.getCode(), voucher.getLabel(), discount, shipping, total,
                voucher.getType(), discountText, voucher.getMinimumOrderAmount(), eligible, voucher.getEndsAt());
    }

    private static String canonicalCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) throw new InvalidVoucherException();
        return rawCode.strip().toUpperCase(Locale.ROOT);
    }
}
