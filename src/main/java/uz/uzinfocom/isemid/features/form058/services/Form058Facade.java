package uz.uzinfocom.isemid.features.form058.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import uz.uzinfocom.isemid.features.form058.dtos.requests.Form058AssignCardRequest;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.integrations.dmed.DmedClient;
import uz.uzinfocom.isemid.features.integrations.dmed.dto.Form058ChangeEvent;
import uz.uzinfocom.isemid.features.user.models.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class Form058Facade {

  public static final String FORM_ORIGIN_SOURCE = "DMED";
  private final Form058Service form058Service;
  private final DmedClient dmedClient;
  private final TransactionTemplate transactionTemplate;

  public Form058 assignCard(Form058AssignCardRequest request, User currentUser) {
    CardStatusChangeResult result = transactionTemplate.execute(status -> {
      Form058 form058 = form058Service.assignCard(request, currentUser);
      return new CardStatusChangeResult(
          form058,
          new Form058ChangeEvent(
              form058.getUuid(),
              form058.getStatus(),
                  null,
              currentUser.getCurrentOrganization().getName(),
              currentUser.getFullName()
          )
      );
    });

    assert result != null;
    pushToDmedIfEnabled(result);
    return result.form();
  }

  public Form058 cancel(Long formId, String reason, User currentUser) {
    CardStatusChangeResult result = transactionTemplate.execute(status -> {
      Form058 form058 = form058Service.cancel(formId,reason,currentUser);

      return new CardStatusChangeResult(
          form058,
          new Form058ChangeEvent(
              form058.getUuid(),
              form058.getStatus(),
                  reason,
              currentUser.getCurrentOrganization().getName(),
              currentUser.getFullName()
          )
      );
    });

    assert result != null;
    pushToDmedIfEnabled(result);

    return result.form();
  }

  /**
   * Pushes the status-change event to DMED when the form originated there. This is a
   * best-effort side notification: the card assignment/cancel has already committed by
   * this point, so a DMED-side failure (e.g. the form is unknown to DMED, network issue)
   * must not turn an already-successful operation into a reported failure for the caller.
   */
  private void pushToDmedIfEnabled(CardStatusChangeResult result) {
    if (!result.form().getSource().equalsIgnoreCase(FORM_ORIGIN_SOURCE)) {
      return;
    }

    try {
      dmedClient.pushFormStatusChangeEvent(result.form().getId(), result.event());
    } catch (Exception e) {
      log.error("form058.dmedPush.fail formId={} reason={}", result.form().getId(), e.getMessage(), e);
    }
  }

  private record CardStatusChangeResult(Form058 form, Form058ChangeEvent event) {}

}
