package com.abdulatif.emis.model.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.util.Date;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable<U> {
    @CreatedBy
    @JsonIgnore
    @Column(name = "created_who")
    private String createdWho;

    @CreatedDate
    @JsonIgnore
    @Column(name = "created_date")
    private Date createdDate;

    @LastModifiedBy
    @JsonIgnore
    @Column(name = "last_modified_who")
    private String lastModifiedWho;

    @LastModifiedDate
    @JsonIgnore
    @Column(name = "last_modified_date")
    private Date updatedDate;
}
