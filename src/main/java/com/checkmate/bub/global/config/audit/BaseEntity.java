package com.checkmate.bub.global.config.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(value = {AuditingEntityListener.class}) // Auditing을 적용하기 위함
@MappedSuperclass // 공통 매핑 정보가 필요할 때 사용하고, 부모 클래스를 상속 받는 자식 클래스에 매핑 정보만 제공
@Getter
public abstract class BaseEntity extends BaseTimeEntity {

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;
}
