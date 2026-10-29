package uz.uzinfocom.app.modules.act.web.dto.request.validation;

import java.util.List;

/** Request row carrying one or more {@link PersonIdentifier} pairs checked by {@link ValidPersonIdentifiers}. */
public interface HasPersonIdentifiers {

    List<PersonIdentifier> personIdentifiers();
}
