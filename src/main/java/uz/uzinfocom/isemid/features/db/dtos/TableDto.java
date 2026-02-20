package uz.uzinfocom.isemid.features.db.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableDto {
    private List<String> tables;
}
