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

    /**
     * Loyihada sahifalash 1 dan boshlanadi (bkz. PaginationConstants.DEFAULT_PAGE_INT,
     * PageableUtils) - 1 birinchi sahifani bildiradi.
     */
    @Min(1)
    private int page = 1;

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