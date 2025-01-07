package uz.uzinfocom.isemid.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("audit-log");

    public void logRequest(String user, String ip, String method, String uri, String queryParams, String headers, String requestBody, int status) {
        auditLogger.info("|User: {}  | IP: {} | Method: {} | URI: {} | Query Params: {} | Headers: {} | Request Body: {} | Response Status: {}|",
                user, ip, method, uri, queryParams, headers, requestBody, status);
    }
}
