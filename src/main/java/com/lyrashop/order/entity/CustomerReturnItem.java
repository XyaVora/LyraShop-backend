package com.lyrashop.order.entity;

import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;

@Entity @Table(name="customer_return_items")
public class CustomerReturnItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name="return_request_id",length=16,nullable=false) private UUID returnRequestId;
    @Column(name="order_item_id",nullable=false) private Long orderItemId;
    @Column(nullable=false) private int quantity;
    protected CustomerReturnItem(){}
    private CustomerReturnItem(UUID requestId,Long itemId,int quantity){this.returnRequestId=requestId;this.orderItemId=itemId;this.quantity=quantity;}
    public static CustomerReturnItem create(UUID requestId,Long itemId,int quantity){return new CustomerReturnItem(requestId,itemId,quantity);}
    public Long getOrderItemId(){return orderItemId;} public int getQuantity(){return quantity;}
}
