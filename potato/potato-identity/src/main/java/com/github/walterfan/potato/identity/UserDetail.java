package com.github.walterfan.potato.identity;

import lombok.Data;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.Date;
import javax.persistence.*;
@Entity
@Table(name="user_detail")
@Data
public class UserDetail extends AbstractPersistable<Integer> {

    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private String userId;

    @Column(name = "first_name", nullable = false, unique = true, length = 250)
    private String firstName;

    @Column(name = "last_name", nullable = false, unique = true, length = 250)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 250)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true, length = 250)
    private String phoneNumber;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time", columnDefinition = "TIMESTAMP DEFAULT NULL")
    private Date updateTime;
}
