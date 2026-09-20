package com.lyrashop.user.entity;
import java.time.Instant;import java.util.UUID;import org.hibernate.annotations.CreationTimestamp;import org.hibernate.annotations.JdbcTypeCode;import org.hibernate.annotations.SourceType;import org.hibernate.type.SqlTypes;import jakarta.persistence.*;
@Entity @Table(name="loyalty_transactions") public class LoyaltyTransaction{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @JdbcTypeCode(SqlTypes.BINARY) @Column(name="user_id",length=16) private UUID userId;
 private long amount; @Column(name="transaction_type") private String type; private String description; @CreationTimestamp(source=SourceType.DB) @Column(name="created_at") private Instant createdAt;
 protected LoyaltyTransaction(){} private LoyaltyTransaction(UUID u,long a,String t,String d){userId=u;amount=a;type=t;description=d;} public static LoyaltyTransaction checkIn(UUID u){return new LoyaltyTransaction(u,100,"CHECK_IN","Điểm danh hằng ngày");}
 public long getAmount(){return amount;}public String getType(){return type;}public String getDescription(){return description;}public Instant getCreatedAt(){return createdAt;}
}
