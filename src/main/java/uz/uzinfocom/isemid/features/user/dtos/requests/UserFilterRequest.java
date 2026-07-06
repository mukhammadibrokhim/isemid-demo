package uz.uzinfocom.isemid.features.user.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserFilterRequest {

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 20;

    private String nnuzb;

    private String firstName;

    private String lastName;

    private String middleName;

    private String phoneNumber;

    /**
     * Viloyat kodi.
     * User.organizationStateCode bo‘yicha filter.
     */
    private String organizationStateCode;

    /**
     * Tuman yoki shahar kodi.
     * User.organizationCityCode bo‘yicha filter.
     */
    private String organizationCityCode;

    /**
     * User.userOrgMedicalType bo‘yicha filter.
     * Bir yoki bir nechta MedicalType yuborish mumkin.
     */
    private Set<MedicalType> userOrgMedicalTypes = new HashSet<>();

    /**
     * User.roles bo‘yicha filter.
     */
    private Set<Long> roleIds = new HashSet<>();

    /**
     * true  - faqat aktivlar
     * false - faqat noaktivlar
     * null  - barcha userlar
     */
    private Boolean active;
}