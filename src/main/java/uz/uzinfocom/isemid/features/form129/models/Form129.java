package uz.uzinfocom.isemid.features.form129.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.AbsEntity;
import uz.uzinfocom.isemid.features.patient.models.Patient;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "form129",
        indexes = {
                @Index(name = "idx_form129_patient_id", columnList = "patient_id"),
                @Index(name = "idx_form129_sender_org_id", columnList = "sender_organization_id"),
                @Index(name = "idx_form129_receiver_org_id", columnList = "receiver_organization_id"),
                @Index(name = "idx_form129_created_at", columnList = "created_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Form129 extends AbsEntity {

    @Column(name = "med_record_id")
    private String medRecordId; // DE.4

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient; // DE.5-27

    @Column(name = "sender_organization_id")
    private Long senderOrganizationId; // DE.3 (yuboruvchi tashkilot)

    @Column(name = "receiver_organization_id")
    private Long receiverOrganizationId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "rw_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "rw_result_detail", length = 1000))
    })
    private LabTestResult rw; // DE.29-32

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "rpr_vdrl_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "rpr_vdrl_result_detail", length = 1000))
    })
    private LabTestResult rprVdrl; // DE.33-36

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "rpga_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "rpga_result_detail", length = 1000))
    })
    private LabTestResult rpga; // DE.37-40

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "elisa_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "elisa_result_detail", length = 1000))
    })
    private LabTestResult elisa; // DE.41-44

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "tpha_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "tpha_result_detail", length = 1000))
    })
    private LabTestResult tpha; // DE.45-48

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "western_blot_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "western_blot_result_detail", length = 1000))
    })
    private LabTestResult westernBlot; // DE.49-52

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "hbsag_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "hbsag_result_detail", length = 1000))
    })
    private LabTestResult hbsAg; // DE.53-56

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "hbeag_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "hbeag_result_detail", length = 1000))
    })
    private LabTestResult hbeAg; // DE.57-60

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "anti_hbc_igg_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "anti_hbc_igg_result_detail", length = 1000))
    })
    private LabTestResult antiHbcIgG; // DE.61-64

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "anti_hbc_igm_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "anti_hbc_igm_result_detail", length = 1000))
    })
    private LabTestResult antiHbcIgM; // DE.65-68

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "anti_hbe_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "anti_hbe_result_detail", length = 1000))
    })
    private LabTestResult antiHbe; // DE.69-72

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "anti_hbs_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "anti_hbs_result_detail", length = 1000))
    })
    private LabTestResult antiHbs; // DE.73-76

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "pcr_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "pcr_result_detail", length = 1000))
    })
    private LabTestResult pcr; // DE.77-80

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resultCode", column = @Column(name = "wright_heddelson_result_code")),
            @AttributeOverride(name = "resultDetail", column = @Column(name = "wright_heddelson_result_detail", length = 1000))
    })
    private LabTestResult wrightHeddelson; // DE.81-85 (brutsellyoz, uch xil natija: manfiy/shubhali/musbat)

    @Column(name = "notifier_full_name")
    private String notifierFullName; // DE.86

    @Column(name = "receiver_full_name")
    private String receiverFullName; // DE.87

    @Column(name = "initial_report_date_time")
    private LocalDateTime initialReportDateTime; // DE.88
}
