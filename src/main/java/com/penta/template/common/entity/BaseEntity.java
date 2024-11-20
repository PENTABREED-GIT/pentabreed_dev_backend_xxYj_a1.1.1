package com.penta.template.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@EntityListeners({AuditingEntityListener.class})
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    // TODO 칼럼명

    @CreatedBy
    @Column(updatable = false, nullable = false)
    private String regId;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date regDate;

    @LastModifiedBy
    private String modId;

    @LastModifiedDate
    private Date modDate;


}
