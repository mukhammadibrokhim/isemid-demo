package uz.uzinfocom.isemid.features.organization.mappers;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidConverter {

    @Named("toUuid")
    public  UUID toUuid(String uuid) {
        return uuid != null ? UUID.fromString(uuid) : null;
    }

    @Named("toString")
    public String toString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}
