package uz.uzinfocom.isemid.models.entities.act.act155;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.act.Act;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act155 extends Act {
    private String title;
    private LocalDate documentSendDate;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private Date selectedDate;
    private String samplerFullName;
    private String samplerPosition;
    private String objectRepresentativeFullName;
    private String objectRepresentativePosition;
    private String additionalInfo;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "act155_detail_mapping",
            joinColumns = @JoinColumn(name = "act155_id"),
            inverseJoinColumns = @JoinColumn(name = "act155_detail_id")
    )
    @JsonIgnore
    private Set<Act155SampleDetails> act155SampleDetails = new HashSet<>();
}
