package com.lyrashop.user.entity;
import java.time.Instant;import java.util.UUID;import org.hibernate.annotations.CreationTimestamp;import org.hibernate.annotations.JdbcTypeCode;import org.hibernate.annotations.SourceType;import org.hibernate.type.SqlTypes;import jakarta.persistence.*;
@Entity @Table(name="loyalty_transactions") public class LoyaltyTransaction{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @JdbcTypeCode(SqlTypes.BINARY) @Column(name="user_id",length=16) private UUID userId;
 @JdbcTypeCode(SqlTypes.BINARY) @Column(name="order_id",length=16) private UUID orderId;
 private long amount; @Column(name="transaction_type") private String type; private String description; @CreationTimestamp(source=SourceType.DB) @Column(name="created_at") private Instant createdAt;
 protected LoyaltyTransaction(){} private LoyaltyTransaction(UUID u,UUID o,long a,String t,String d){userId=u;orderId=o;amount=a;type=t;description=d;}
 public static LoyaltyTransaction checkIn(UUID u,long amount){return new LoyaltyTransaction(u,null,amount,"CHECK_IN","Điểm danh hằng ngày");}
 public static LoyaltyTransaction orderEarn(UUID u,UUID o,long amount){return new LoyaltyTransaction(u,o,amount,"ORDER_EARN","Tích Xu từ đơn hàng");}
 public static LoyaltyTransaction orderSpend(UUID u,UUID o,long amount){return new LoyaltyTransaction(u,o,-amount,"ORDER_SPEND","Dùng Xu cho đơn hàng");}
 public static LoyaltyTransaction orderRefund(UUID u,UUID o,long amount){return new LoyaltyTransaction(u,o,amount,"ORDER_REFUND","Hoàn Xu từ đơn hàng đã hủy");}
 public long getAmount(){return amount;}public String getType(){return type;}public String getDescription(){return description;}public UUID getOrderId(){return orderId;}public Instant getCreatedAt(){return createdAt;}
}
