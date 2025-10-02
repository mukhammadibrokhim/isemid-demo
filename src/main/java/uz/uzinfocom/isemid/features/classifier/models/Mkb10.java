package uz.uzinfocom.isemid.features.classifier.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_id", columnList = "id"),
                @Index(name = "idx_parent_id", columnList = "parent_id"),
                @Index(name = "idx_code", columnList = "code"),
        }, name = "cls_mkb10")
@Entity
public class Mkb10 {
    @Id
    private Long id;

    @UuidGenerator
    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @OrderBy
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Timestamp updatedAt;

    @CreatedBy
    @Column(name = "created_by_id")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by_id")
    private Long updatedBy;

    private Long secondaryId;
    private Long parentId;
    private String code;
    private int level;
    private boolean isLastLevel;
    private String nameUz;
    private String nameRu;
    private String nameUzCyril;
    private String nameKaa;
    private String comment;
    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit = 1;

    @OneToMany(mappedBy = "parentId")
    @JsonIgnore
    private List<Mkb10> children = new ArrayList<>();

    @Transient
    @JsonProperty("childrenValue")
    public int getChildrenValue() {
        return (children != null) ? children.size() : 0;
    }

    @PrePersist
    @JsonIgnore
    public void setDefaultUsageLimit() {
        if (this.usageLimit == null) {
            this.usageLimit = 1;
        }
    }
}
