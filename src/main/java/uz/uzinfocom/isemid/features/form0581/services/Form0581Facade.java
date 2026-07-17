package uz.uzinfocom.isemid.features.form0581.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581AssignCardRequest;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.integrations.dmed.DmedForm0581Client;
import uz.uzinfocom.isemid.features.integrations.dmed.dto.Form058ChangeEvent;
import uz.uzinfocom.isemid.features.user.models.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class Form0581Facade {

  public static final String FORM_ORIGIN_SOURCE = "DMED";

  /**
   * DMED integration is currently DISABLED for form058-1 (product decision).
   * Once the DMED side is ready, simply flip this to {@code true} - the push
   * call below and {@code DmedForm0581Client} are already in place.
   */
  private static final boolean DMED_INTEGRATION_ENABLED = false;

  private final Form0581Service form0581Service;
  private final DmedForm0581Client dmedForm0581Client;
  private final TransactionTemplate transactionTemplate;

  public Form0581 assignCard(Form0581AssignCardRequest request, User currentUser) {
    CardStatusChangeResult result = transactionTemplate.execute(status -> {
      Form0581 form0581 = form0581Service.assignCard(request, currentUser);
      return new CardStatusChangeResult(
          form0581,
          new Form058ChangeEvent(
              form0581.getUuid(),
              form0581.getStatus(),
              currentUser.getCurrentOrganization().getName(),
              currentUser.getFullName()
          )
      );
    });

    assert result != null;
    pushToDmedIfEnabled(result);
    return result.form();
  }

  public Form0581 cancel(Long formId, String reason, User currentUser) {
    CardStatusChangeResult result = transactionTemplate.execute(status -> {
      Form0581 form0581 = form0581Service.cancel(formId, reason, currentUser);

      return new CardStatusChangeResult(
          form0581,
          new Form058ChangeEvent(
              form0581.getUuid(),
              form0581.getStatus(),
              currentUser.getCurrentOrganization().getName(),
              currentUser.getFullName()
          )
      );
    });

    assert result != null;
    pushToDmedIfEnabled(result);
    return result.form();
  }

  private void pushToDmedIfEnabled(CardStatusChangeResult result) {
    if (!DMED_INTEGRATION_ENABLED) {
      return;
    }

    if (result.form().getSource().equalsIgnoreCase(FORM_ORIGIN_SOURCE)) {
      dmedForm0581Client.pushFormStatusChangeEvent(
          result.form().getId(),
          result.event()
      );
    }
  }

  private record CardStatusChangeResult(Form0581 form, Form058ChangeEvent event) {}

}
