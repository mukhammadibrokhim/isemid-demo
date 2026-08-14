package uz.uzinfocom.isemid.features.act.models;

import java.util.Map;

public interface LisDocumentAttachable {
  void setLisActResponse(Map<String, Object> body);
  void setLisProtocolResponse(Map<String, Object> body);
  Map<String,Object> getLisActResponse();
  Map<String,Object> getLisProtocolResponse();
  void concludeProtocol(String message);
}
