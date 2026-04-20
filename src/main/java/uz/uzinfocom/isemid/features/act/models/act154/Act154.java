package uz.uzinfocom.isemid.features.act.models.act154;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act154 extends Act {
   private Integer tin;
   private String institutionName;
   private String institutionAddress;
   private String activityTypeCode;

   private String title;
   private Long serialNumber;
   private LocalDateTime sampleTakenDateTime;
   private LocalDateTime deliveredDateTime;

   private String samplerFullName;
   private Integer samplerPositionId;
   private String samplerPositionUz;
   private String samplerPositionRu;
   private String participantFullName;
   private Integer participantPositionId;
   private String participantPositionUz;
   private String participantPositionRu;

   @Column(columnDefinition = "TEXT")
   private String additionalInfo;

   @OneToMany(mappedBy = "act154", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   @JsonManagedReference
   private Set<Act154Detail> act154Details = new HashSet<>();
}
