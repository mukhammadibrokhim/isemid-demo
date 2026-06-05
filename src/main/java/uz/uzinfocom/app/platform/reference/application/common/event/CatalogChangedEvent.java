package uz.uzinfocom.app.platform.reference.application.common.event;

import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;

public record CatalogChangedEvent(CatalogType type) {
}
