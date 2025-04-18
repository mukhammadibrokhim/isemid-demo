package uz.uzinfocom.isemid.features.patient.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressRequest {
    @NotNull(message = "Тип адреса (type) не должен быть пустым")
    private AddressType type;

    @NotBlank(message = "Код области (stateCode) не должен быть пустым")
    private String stateCode;

    @NotBlank(message = "Код города (cityCode) не должен быть пустым")
    private String cityCode;

    @NotBlank(message = "Махалля (neighborhood) не должна быть пустой")
    private String neighborhood;

    @NotBlank(message = "Улица (streetAddress) не должна быть пустой")
    private String streetAddress;

    @NotBlank(message = "Номер дома (houseNumber) не должен быть пустым")
    private String houseNumber;

    @NotBlank(message = "Номер квартиры (apartmentNumber) не должен быть пустым")
    private String apartmentNumber;
}
