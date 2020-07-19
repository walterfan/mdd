package com.github.walterfan.potato.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

/**
 * @Author: Walter Fan
 * @Date: 19/7/2020, Sun
 **/
@Data
@Entity
@Table(name="tomato")
public class TomatoEntity {
    @Id
    @GeneratedValue
    @Nullable
    @Type(type="uuid-char")
    private UUID id;

    @Column(unique = true)
    private String name;

    private String content;

    private String tags;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date createTime;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date updateTime;

    @Type(type="uuid-char")
    private UUID potatoId;

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }
}
