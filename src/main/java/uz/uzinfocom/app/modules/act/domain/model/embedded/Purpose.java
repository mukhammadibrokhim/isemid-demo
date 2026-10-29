package uz.uzinfocom.app.modules.act.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * «Tekshirish yoki namuna olish maqsadi». New acts store only {@link #code}
 * — a {@code ref_catalog} entry of type {@link #CATALOG_TYPE}, whose names
 * are resolved on read. The id/uz/ru/loinc snapshot columns are what
 * legacy-migrated acts carry and are never written for new ones.
 */
@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Purpose {

    public static final String CATALOG_TYPE = "PURPOSE";

    @Column(name = "purpose_code", length = 50)
    private String code;

    @Column(name = "purpose_id")
    private Integer purposeId;

    @Column(name = "sampling_purpose_uz")
    private String samplingPurposeUz;

    @Column(name = "sampling_purpose_ru")
    private String samplingPurposeRu;

    @Column(name = "sampling_purpose_loinc")
    private String samplingPurposeLoinc;

    public static Purpose ofCode(String code) {
        Purpose purpose = new Purpose();
        purpose.setCode(code);
        return purpose;
    }
}
