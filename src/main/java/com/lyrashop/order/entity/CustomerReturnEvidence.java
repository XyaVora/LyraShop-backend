package com.lyrashop.order.entity;

import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;

@Entity @Table(name="customer_return_evidence")
public class CustomerReturnEvidence {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name="return_request_id",length=16,nullable=false) private UUID returnRequestId;
    @Column(nullable=false,length=2048) private String url;
    protected CustomerReturnEvidence(){}
    private CustomerReturnEvidence(UUID requestId,String url){this.returnRequestId=requestId;this.url=url;}
    public static CustomerReturnEvidence create(UUID requestId,String url){return new CustomerReturnEvidence(requestId,url);}
    public String getUrl(){return url;}
}
