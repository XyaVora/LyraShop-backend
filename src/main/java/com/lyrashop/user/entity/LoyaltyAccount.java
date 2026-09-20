package com.lyrashop.user.entity;
import java.time.LocalDate;import java.util.UUID;import org.hibernate.annotations.JdbcTypeCode;import org.hibernate.type.SqlTypes;import jakarta.persistence.*;
@Entity @Table(name="loyalty_accounts") public class LoyaltyAccount{
 @Id @JdbcTypeCode(SqlTypes.BINARY) @Column(name="user_id",length=16) private UUID userId;
 @Column(name="coin_balance",nullable=false) private long coinBalance; @Column(name="last_check_in") private LocalDate lastCheckIn;
 protected LoyaltyAccount(){} private LoyaltyAccount(UUID id){userId=id;} public static LoyaltyAccount create(UUID id){return new LoyaltyAccount(id);}
 public long getCoinBalance(){return coinBalance;} public LocalDate getLastCheckIn(){return lastCheckIn;}
 public void checkIn(LocalDate today){if(today.equals(lastCheckIn))throw new IllegalStateException();lastCheckIn=today;coinBalance+=100;}
}
