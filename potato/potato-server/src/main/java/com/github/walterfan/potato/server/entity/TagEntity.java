package com.github.walterfan.potato.server.entity;

import lombok.Data;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.lang.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * @Author: Walter Fan
 **/
@Data
@Entity
@Table(name = "tag")
public class TagEntity  {
    @Id
    @GeneratedValue
    @Nullable
    @Type(type="uuid-char")
    private UUID id;

    @Column(unique=true)
    @NaturalId
    private String name;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "tags")
    private Set<PotatoEntity> potatoes = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TagEntity tag = (TagEntity) o;
        return Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "TagEntity: id=" + getId() + ", name=" + name;
    }
}