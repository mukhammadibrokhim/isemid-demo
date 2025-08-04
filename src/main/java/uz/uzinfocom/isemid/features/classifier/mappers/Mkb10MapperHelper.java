package uz.uzinfocom.isemid.features.classifier.mappers;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.Mkb10Response;
import uz.uzinfocom.isemid.features.classifier.repositories.Mkb10Repository;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Mkb10MapperHelper {

    private final Mkb10Repository mkb10Repository;
    private final Mkb10Mapper mkb10Mapper;

    @Named("mapMkb10Codes")
    public List<Mkb10Response> mapMkb10Codes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) return List.of();

        return mkb10Repository.findAllByCodeIn(codes).stream()
                .map(mkb10Mapper::toResponse)
                .toList();
    }
}
