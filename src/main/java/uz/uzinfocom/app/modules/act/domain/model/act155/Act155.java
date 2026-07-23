package uz.uzinfocom.app.modules.act.domain.model.act155;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.act.domain.model.Act;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "act155")
@NoArgsConstructor
@AllArgsConstructor
public class Act155 extends Act {

    @Column(name = "title")
    private String title;

    @Column(name = "tin")
    private Integer tin;

    @Column(name = "institution_name")
    private String institutionName;

    @Column(name = "institution_address")
    private String institutionAddress;

    @Column(name = "activity_type_code")
    private String activityTypeCode;

    @Column(name = "selected_date")
    private LocalDate selectedDate;

    @Column(name = "sampler_full_name")
    private String samplerFullName;

    @Column(name = "sampler_position")
    private String samplerPosition;

    @Column(name = "object_representative_full_name")
    private String objectRepresentativeFullName;

    @Column(name = "object_representative_position")
    private String objectRepresentativePosition;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "act155", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id DESC")
    private List<Act155Detail> act155Details = new ArrayList<>();
}
