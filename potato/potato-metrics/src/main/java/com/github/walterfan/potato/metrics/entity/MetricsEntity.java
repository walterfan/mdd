package com.github.walterfan.potato.metrics.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
public class MetricsEntity extends AbstractPersistable<UUID> {

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

    private UUID userId;
    

    public void setId(UUID id) {
        super.setId(id);
    }

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
        MetricsEntity that = (MetricsEntity) o;
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
