package uz.uzinfocom.isemid.commons.configs.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.commons.utils.RSAKeyUtils.privateKeyInternal;


@Component
public class JwtUtil {
    public static final long EXPIRATION_TIME = 3 * 60 * 60;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.RSA256(null, privateKeyInternal());

            Map<String, Object> headerClaims = new HashMap<>();
            headerClaims.put("typ", "JWT");
            headerClaims.put("alg", "RS256");
            headerClaims.put("jti", UUID.randomUUID().toString());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", user.getFullName());
            userInfo.put("practitioner", user.getUuid().toString());
            userInfo.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));

            List<Map<String, Object>> organizationList = new ArrayList<>();
            for (Organization org : user.getOrganizations()) {
                Map<String, Object> orgMap = new HashMap<>();
                orgMap.put("uuid", org.getUuid().toString());
                orgMap.put("name", org.getName());
                orgMap.put("active", org.isActive());

                List<Map<String, String>> practitionerRoles = user.getRoles().stream()
                        .map(role -> Map.of("uuid", role.getUuid().toString(), "code", role.getUuid().toString()))
                        .collect(Collectors.toList());

                orgMap.put("practitioner_role", practitionerRoles);
                organizationList.add(orgMap);
            }
            userInfo.put("organization", organizationList);

            Instant now = Instant.now();
            return JWT.create()
                    .withHeader(headerClaims)
                    .withAudience("94f83670-f841-4ec5-a593-72d2873f054b")
                    .withJWTId(UUID.randomUUID().toString())
                    .withIssuedAt(Date.from(now))
                    .withNotBefore(Date.from(now))
                    .withExpiresAt(Date.from(now.plusSeconds(EXPIRATION_TIME)))
                    .withSubject(user.getId().toString())
                    .withClaim("scopes", Collections.emptyList())
                    .withClaim("user_info", userInfo)
                    .sign(algorithm);

        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT", e);
        }
    }
}
