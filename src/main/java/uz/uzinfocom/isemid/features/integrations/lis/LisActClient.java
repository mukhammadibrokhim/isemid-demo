package uz.uzinfocom.isemid.features.integrations.lis;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import uz.uzinfocom.isemid.features.integrations.lis.dto.LisResponse;
import uz.uzinfocom.isemid.features.integrations.lis.dto.CreateLisActRequest;

@HttpExchange(contentType = "application/json", accept = "application/json")
public interface LisActClient {


  /**
   * fixme path variable actType should be changed to laboratoryId
   * @param organizationId
   * @param laboratoryId
   * @param senderActNumber
   * @param force
   * @param request
   * @return
   */
  @PostExchange("${isemid.integration.paths.lis-create-act-path}/{actType}")
  LisResponse<Long> createLisAct(@RequestHeader("Organization-Id") String organizationId,
                           @PathVariable("actType") Long laboratoryId,
                           @RequestParam String senderActNumber,
                           @RequestParam boolean force,
                           @RequestBody CreateLisActRequest request);


  @GetExchange("${isemid.integration.paths.lis-act-code-path}")
  LisResponse<Long> getLisActTemplate(@RequestParam String researchCode);
}
