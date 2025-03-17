package uz.uzinfocom.isemid.models.entities.act.act154;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

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

   @ManyToOne
   @JoinColumn(referencedColumnName = "code")
   @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
   private Catalog actClassificator;

   @OneToMany(mappedBy = "act154", cascade = CascadeType.ALL, orphanRemoval = true)
   @JsonIgnoreProperties({"act154"})
   @OrderBy("id DESC")
   @Builder.Default
   private List<Act154SampleDetails> act154SampleDetailsList = new ArrayList<>();
}
