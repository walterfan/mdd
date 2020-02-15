package com.github.walterfan.potato.server.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@MappedSuperclass
public abstract class AbstractEntity implements Persistable<UUID> {
    private static final long serialVersionUID = -5554308939380869754L;
    @Id
    @GeneratedValue
    @Nullable
    @Type(type="uuid-char")
    private UUID id;

    @Transient
    public boolean isNew() {
        return null == this.getId();
    }
}