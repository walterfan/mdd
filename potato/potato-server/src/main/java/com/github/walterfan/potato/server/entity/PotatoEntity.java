package com.github.walterfan.potato.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.lang.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Walter Fan
 **/
@Data
@Entity
@Table(name="potato")
public class PotatoEntity {

    @Id
    @GeneratedValue
    @Nullable
    @Type(type="uuid-char")
    private UUID id;

    @Column(unique = true)
    private String name;

    private Integer priority;

    private String description;

    private Long duration;

    private TimeUnit timeUnit;

    private Date startTime;

    private Date endTime;

    private Date scheduleTime;

    private Date deadline;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date createTime;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date updateTime;

    @Type(type="uuid-char")
    private UUID userId;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            })
    @JoinTable(name = "potato_tag",
            joinColumns = {@JoinColumn(name = "potato_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")})
    private Set<TagEntity> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PotatoEntity that = (PotatoEntity) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(description, that.description) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), name, priority, description, userId);
    }

    @Override
    public String toString() {
        return "PotatoEntity{" +
                "name='" + name + '\'' +
                ", priority=" + priority +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", timeUnit=" + timeUnit +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", scheduleTime=" + scheduleTime +
                ", deadline=" + deadline +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", userId=" + userId +
                '}';
    }
}
