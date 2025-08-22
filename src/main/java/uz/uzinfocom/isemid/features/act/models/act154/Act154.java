package uz.uzinfocom.isemid.features.act.models.act154;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act154 extends Act {
   private String title;
   private Long serialNumber;
   private LocalDate documentSendDate;
   private Integer tin;
   private String nameOfObject;
   private String objectAddress;
   private LocalDateTime hospitalizationDate;
   private String samplerFullName;
   private String position;
   private String additionalInfo;

   @OneToMany(mappedBy = "act154", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   private List<Act154Detail> act154Details = new ArrayList<>();
}
