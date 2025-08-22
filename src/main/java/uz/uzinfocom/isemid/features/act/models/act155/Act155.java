package uz.uzinfocom.isemid.features.act.models.act155;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

    @OneToMany(mappedBy = "act155", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Act155Detail> act155Details = new HashSet<>();
}
