package com.lyrashop.user.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.order.entity.ShopOrder;
import com.lyrashop.order.repository.ShopOrderRepository;
import com.lyrashop.user.entity.LoyaltyAccount;
import com.lyrashop.user.entity.LoyaltyTransaction;
import com.lyrashop.user.repository.LoyaltyAccountRepository;
import com.lyrashop.user.repository.LoyaltyTransactionRepository;

@Service
public class LoyaltyService {
    public static final int MAX_REDEMPTION_PERCENT = 20;
    public static final long COINS_PER_THOUSAND_VND = 1;
    public static final long CHECK_IN_REWARD = 100;
    public static final BigDecimal GOLD_THRESHOLD = new BigDecimal("2000000");
    public static final BigDecimal DIAMOND_THRESHOLD = new BigDecimal("5000000");
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");

    private final LoyaltyAccountRepository accounts;
    private final LoyaltyTransactionRepository transactions;
    private final ShopOrderRepository orders;

    public LoyaltyService(LoyaltyAccountRepository accounts, LoyaltyTransactionRepository transactions,
            ShopOrderRepository orders) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.orders = orders;
    }

    @Transactional
    public Map<String, Object> get(UUID userId) {
        return response(userId, account(userId));
    }

    @Transactional
    public Map<String, Object> checkIn(UUID userId) {
        LoyaltyAccount account = lockedAccount(userId);
        try {
            account.checkIn(LocalDate.now(VIETNAM), CHECK_IN_REWARD);
        } catch (IllegalStateException ignored) {
            return response(userId, account);
        }
        accounts.save(account);
        transactions.save(LoyaltyTransaction.checkIn(userId, CHECK_IN_REWARD));
        return response(userId, account);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> history(UUID userId) {
        return transactions.findAllByUserIdOrderByCreatedAtDesc(userId).stream().map(transaction -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("amount", transaction.getAmount());
            result.put("type", transaction.getType());
            result.put("description", transaction.getDescription());
            result.put("orderId", transaction.getOrderId());
            result.put("createdAt", transaction.getCreatedAt());
            return result;
        }).toList();
    }

    @Transactional
    public void spendForOrder(UUID userId, ShopOrder order, long requestedCoins, BigDecimal payableBeforeCoins) {
        if (requestedCoins == 0) return;
        LoyaltyAccount account = lockedAccount(userId);
        if (requestedCoins > maximumRedeemable(payableBeforeCoins)
                || requestedCoins > account.getCoinBalance()) {
            throw new InvalidLoyaltyRedemptionException();
        }
        account.debit(requestedCoins);
        accounts.save(account);
        transactions.save(LoyaltyTransaction.orderSpend(userId, order.getId(), requestedCoins));
    }

    @Transactional
    public void awardDeliveredOrder(ShopOrder order) {
        if (transactions.existsByTypeAndOrderId("ORDER_EARN", order.getId())) return;
        long amount = order.getTotalAmount().divide(new BigDecimal("1000"), 0, RoundingMode.DOWN).longValue();
        if (amount < 1) return;
        LoyaltyAccount account = lockedAccount(order.getUserId());
        account.credit(amount);
        accounts.save(account);
        transactions.save(LoyaltyTransaction.orderEarn(order.getUserId(), order.getId(), amount));
    }

    @Transactional
    public void refundCancelledOrder(ShopOrder order) {
        long amount = order.getLoyaltyCoinsUsed();
        if (amount < 1 || transactions.existsByTypeAndOrderId("ORDER_REFUND", order.getId())) return;
        LoyaltyAccount account = lockedAccount(order.getUserId());
        account.credit(amount);
        accounts.save(account);
        transactions.save(LoyaltyTransaction.orderRefund(order.getUserId(), order.getId(), amount));
    }

    public static long maximumRedeemable(BigDecimal payableBeforeCoins) {
        if (payableBeforeCoins == null || payableBeforeCoins.signum() <= 0) return 0;
        return payableBeforeCoins.multiply(BigDecimal.valueOf(MAX_REDEMPTION_PERCENT))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).longValue();
    }

    private LoyaltyAccount account(UUID userId) {
        return accounts.findById(userId).orElseGet(() -> accounts.save(LoyaltyAccount.create(userId)));
    }

    private LoyaltyAccount lockedAccount(UUID userId) {
        return accounts.findForUpdate(userId)
                .orElseGet(() -> accounts.saveAndFlush(LoyaltyAccount.create(userId)));
    }

    private Map<String, Object> response(UUID userId, LoyaltyAccount account) {
        BigDecimal spend = orders.sumPaidTotalByUserId(userId);
        String tier = spend.compareTo(DIAMOND_THRESHOLD) >= 0 ? "DIAMOND"
                : spend.compareTo(GOLD_THRESHOLD) >= 0 ? "GOLD" : "MEMBER";
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tier", tier);
        result.put("coinBalance", account.getCoinBalance());
        result.put("totalSpend", spend);
        result.put("checkedInToday", LocalDate.now(VIETNAM).equals(account.getLastCheckIn()));
        result.put("nextTierSpend", tier.equals("DIAMOND") ? BigDecimal.ZERO
                : tier.equals("GOLD") ? DIAMOND_THRESHOLD : GOLD_THRESHOLD);
        result.put("coinValueVnd", 1);
        result.put("maxRedemptionPercent", MAX_REDEMPTION_PERCENT);
        result.put("coinsPerThousandVnd", COINS_PER_THOUSAND_VND);
        result.put("checkInReward", CHECK_IN_REWARD);
        result.put("goldThreshold", GOLD_THRESHOLD);
        result.put("diamondThreshold", DIAMOND_THRESHOLD);
        return result;
    }
}
