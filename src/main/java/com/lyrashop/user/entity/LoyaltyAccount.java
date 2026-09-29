package com.lyrashop.user.entity;
import java.time.LocalDate;import java.util.UUID;import org.hibernate.annotations.JdbcTypeCode;import org.hibernate.type.SqlTypes;import jakarta.persistence.*;
@Entity @Table(name="loyalty_accounts") public class LoyaltyAccount{
 @Id @JdbcTypeCode(SqlTypes.BINARY) @Column(name="user_id",length=16) private UUID userId;
 @Column(name="coin_balance",nullable=false) private long coinBalance; @Column(name="last_check_in") private LocalDate lastCheckIn;
 @Column(name="coin_debt",nullable=false) private long coinDebt;
 protected LoyaltyAccount(){} private LoyaltyAccount(UUID id){userId=id;} public static LoyaltyAccount create(UUID id){return new LoyaltyAccount(id);}
 public long getCoinBalance(){return coinBalance;} public long getCoinDebt(){return coinDebt;} public LocalDate getLastCheckIn(){return lastCheckIn;}
 public void checkIn(LocalDate today,long reward){if(today.equals(lastCheckIn))throw new IllegalStateException();if(reward<1)throw new IllegalArgumentException("reward must be positive");lastCheckIn=today;credit(reward);}
 public void credit(long amount){if(amount<1)throw new IllegalArgumentException("credit must be positive");long debtPayment=Math.min(coinDebt,amount);coinDebt-=debtPayment;coinBalance=Math.addExact(coinBalance,amount-debtPayment);}
 public void debit(long amount){if(amount<1||amount>coinBalance)throw new IllegalArgumentException("insufficient loyalty balance");coinBalance-=amount;}
 public void reverseCredit(long amount){if(amount<1)throw new IllegalArgumentException("amount must be positive");long fromBalance=Math.min(coinBalance,amount);coinBalance-=fromBalance;coinDebt=Math.addExact(coinDebt,amount-fromBalance);}
}
