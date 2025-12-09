package uz.uzinfocom.isemid.features.act.dtos.responses.act155;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Request object for Act155")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act155Response extends ActResponse {
    private Long id;
    private String title;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private Date selectedDate;
    private String samplerFullName;
    private String samplerPosition;
    private String objectRepresentativeFullName;
    private String objectRepresentativePosition;
    private String additionalInfo;
    private Set<Act155DetailResponse> act155Details;
}
