package uz.uzinfocom.isemid.features.act.models.act154;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act154 extends Act {
   private String title;
   private Long serialNumber;
   private Integer tin;
   private String institutionName;
   private String institutionAddress;
   private String activityTypeCode;

   @Column(name = "delivered_date")
   private LocalDate deliveredDate; // laboratoriyaga yetkazilgan sana;
   private String samplerFullName;
   private Integer positionId;
   private String position;
   private String participantFullName;
   private Integer participantPositionId;
   private String participantPosition;
   private String additionalInfo;

   @OneToMany(mappedBy = "act154", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   @JsonManagedReference
   private Set<Act154Detail> act154Details = new HashSet<>();
}
