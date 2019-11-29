package com.github.walterfan.potato.identity;

import lombok.Data;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="address")
@Data
public class Address extends AbstractPersistable<Integer> {


    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private String userId;

    @Column(name = "city", nullable = false,length = 25)
    private String city;

    @Column(name = "address_line_1", nullable = false,length = 250)
    private String addressLine1;

    @Column(name = "address_line_2", nullable = true,length = 250)
    private String addressLine2;

    @Column(name = "zip_code", nullable = false,length = 6)
    private String zipCode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time", columnDefinition = "TIMESTAMP DEFAULT NULL")
    private Date updateTime;
}
