package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.enums.AddressType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqAddress {
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
