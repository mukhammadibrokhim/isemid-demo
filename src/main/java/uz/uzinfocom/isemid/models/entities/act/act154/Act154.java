package uz.uzinfocom.isemid.models.entities.act.act154;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.act.Act;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
   private LocalDate documentSendDate;
   private Integer tin;
   private String nameOfObject;
   private String objectAddress;
   private LocalDateTime hospitalizationDate;
   private String samplerFullName;
   private String position;
   private String additionalInfo;

   @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
   @JoinTable(
           name = "act154_detail_mapping",
           joinColumns = @JoinColumn(name = "act154_id"),
           inverseJoinColumns = @JoinColumn(name = "act154_detail_id")
   )
   @JsonIgnore
   private Set<Act154SampleDetails> act154Details = new HashSet<>();
}
