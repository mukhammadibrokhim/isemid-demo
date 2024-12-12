package com.abdulatif.emis.security.jwt;

import com.abdulatif.emis.dto.sso.OrganizationDTO;
import com.abdulatif.emis.dto.sso.PractitionerDTO;
import com.abdulatif.emis.dto.sso.UserDTO;
import com.abdulatif.emis.exception.BadRequestException;
import com.abdulatif.emis.exception.ServiceUnavailableException;
import com.abdulatif.emis.exception.RecordNotFoundException;
import com.abdulatif.emis.model.organization.Organization;
import com.abdulatif.emis.model.user.Permission;
import com.abdulatif.emis.model.user.Role;
import com.abdulatif.emis.model.user.User;
import com.abdulatif.emis.model.enums.Status;
import com.abdulatif.emis.repository.organization.OrganizationRepository;
import com.abdulatif.emis.repository.user.RoleRepository;
import com.abdulatif.emis.repository.user.UserRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.*;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Util class that provides methods for validation, etc. of JWT token.
 */

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("jwt.token.secret")
    private String secret;

    @Value("${mis.practitioner.info.url}")
    private String misPractitionerInfoUrl;

    @Value("${mis.pinfl.code}")
    private String misPinflCode;

    @Value("${jwt.public.key}")
    private String publicKey;

    @Qualifier("jwtUserDetailsService")
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;


    public PublicKey publicKey() throws Exception {
        publicKey = publicKey.replaceAll("\\n", "").replaceAll("\\r", "");
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(keySpec);
    }

    @PostConstruct
    protected void init() {
        secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    public Authentication getAuthentication(String token) throws Exception {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    @Transactional
    public String getUsername(String token) throws Exception {
        UserDTO userInfo = getUserInfoFromToken(token);
        List<String> roles = userInfo.getRoles();
        List<String> lisRoles = roles.stream().filter((r) -> r.startsWith("emis_")).toList();
        if (lisRoles.isEmpty())
            throw new RecordNotFoundException("Access is denied");
        List<OrganizationDTO> organizationDTOs = userInfo.getOrganization();
        String practitioner = userInfo.getPractitioner();

        PractitionerDTO practitionerDTO = getUserInfoFromMis(token, practitioner);
        String pinfl = null;
        for (PractitionerDTO.IdentifierDto identifierDto : practitionerDTO.getIdentifier()) {
            for (PractitionerDTO.IdentifierDto.TypeDto.CodingDto codingDto : identifierDto.getType().getCoding()) {
                if (codingDto.getCode().equals(misPinflCode)) {
                    pinfl = identifierDto.getValue();
                    break;
                }
            }
        }

        User user = null;
        if (pinfl != null)
            user = userRepository.findByUsername(pinfl);
        else
            throw new RecordNotFoundException("PINFL not found from user");

        if (user != null) {
            return updateUserInfo(user, practitionerDTO, roles, organizationDTOs);
        } else
            return createNewUser(pinfl, practitionerDTO, roles, organizationDTOs);
    }

    private String updateUserInfo(User user, PractitionerDTO practitionerDTO, List<String> roles, List<OrganizationDTO> organizationDTOs) throws ParseException {
        if (practitionerDTO.getName() != null) {
            user.setFirstName(practitionerDTO.getName().getFirstName());
            user.setMiddleName(practitionerDTO.getName().getPatronymic());
            user.setLastName(practitionerDTO.getName().getLastName());
        }
        if (practitionerDTO.getBirthDate() != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(practitionerDTO.getBirthDate());
            user.setDateOfBirth(date);
        }
        if (practitionerDTO.getTelecom() != null && !practitionerDTO.getTelecom().isEmpty()) {
            for (PractitionerDTO.TelecomDto telecomDto : practitionerDTO.getTelecom()) {
                if (telecomDto.getValue() != null) {
                    user.setMobile(telecomDto.getValue());
                    break;
                }
            }
        }
        attachRolesToUser(user, roles);
        createOrganizationAndAttachToUser(user, organizationDTOs);
        return userRepository.save(user).getUsername();
    }

    private String createNewUser(String pinfl, PractitionerDTO practitionerDTO, List<String> roles, List<OrganizationDTO> organizationDTOS) throws ParseException {
        User user = new User();
        user.setUsername(pinfl);
        user.setStatus(Status.ACTIVE);
        if (practitionerDTO.getName() != null) {
            user.setFirstName(practitionerDTO.getName().getFirstName());
            user.setMiddleName(practitionerDTO.getName().getPatronymic());
            user.setLastName(practitionerDTO.getName().getLastName());
        }
        if (practitionerDTO.getBirthDate() != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(practitionerDTO.getBirthDate());
            user.setDateOfBirth(date);
        }
        if (practitionerDTO.getTelecom() != null && !practitionerDTO.getTelecom().isEmpty()) {
            for (PractitionerDTO.TelecomDto telecomDto : practitionerDTO.getTelecom()) {
                if (telecomDto.getValue() != null) {
                    user.setMobile(telecomDto.getValue());
                    break;
                }
            }
        }
        user.setPnfl(pinfl);
        user.setUuid(practitionerDTO.getId());
        attachRolesToUser(user, roles);
        createOrganizationAndAttachToUser(user, organizationDTOS);
        User savedUser = userRepository.save(user);
        return savedUser.getUsername();
    }

    private void createOrganizationAndAttachToUser(User user, List<OrganizationDTO> organizationDTOS) {
        Set<Organization> organizations = new HashSet<>();
        for (OrganizationDTO organizationDTO : organizationDTOS) {
            Organization organization = organizationRepository.findByUuid(organizationDTO.getUuid());
            if (organization != null) {
                organizations.add(organization);
            } else {
                Organization newOrganization = new Organization();
                newOrganization.setUuid(organizationDTO.getUuid());
                newOrganization.setName(organizationDTO.getName());
                newOrganization.setStatus(Status.ACTIVE);
                Organization save = organizationRepository.save(newOrganization);
                save.setHierarchy(save.getId().toString());
                Organization saved = organizationRepository.save(save);
                organizations.add(saved);
            }
        }
        user.setParentOrganizations(organizations);
    }

    private void attachRolesToUser(User user, List<String> roles) {
        List<Role> rolesList = new ArrayList<>();
        for (String role : roles) {
            Role roleDB = roleRepository.findByName(role);
            if (roleDB != null) {
                rolesList.add(roleDB);
            }
        }
        Set<Role> userRoles = user.getRoles();
        userRoles.addAll(rolesList);
        user.setRoles(userRoles);
    }

    private UserDTO getUserInfoFromToken(String token) {
        UserDTO userInfo;
        try {
            Claims body = Jwts.parser()
                    .setSigningKey(publicKey())
                    .parseClaimsJws(token)
                    .getBody();
            userInfo = objectMapper.convertValue(body.get("user_info"), UserDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Unsupported Bearer Token");
        }
        return userInfo;
    }

    private PractitionerDTO getUserInfoFromMis(String accessToken, String practitioner) {
        PractitionerDTO practitionerDTO = new PractitionerDTO();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(misPractitionerInfoUrl + practitioner, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                practitionerDTO = objectMapper.readValue(response.getBody(), PractitionerDTO.class);
            } else {
                log.warn("MIS service is unavailable");
                throw new ServiceUnavailableException("Service unavailable");
            }
        } catch (Exception e) {
            log.warn("ERROR fetching practitioner data from MIS service {}", e.getMessage());
            throw new ServiceUnavailableException("Service unavailable");
        }
        return practitionerDTO;
    }

    public Organization parentOrganization(Organization organization) {
        while (organization.getParent() != null && organization.getHierarchy() != null) {
            organization = organization.getParent();

        }
        return organization;
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token, HttpServletRequest httpServletRequest) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(publicKey())
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (SignatureException ex) {
            log.warn("Invalid JWT Signature");
        } catch (MalformedJwtException ex) {
            log.warn("Invalid JWT token");
        } catch (ExpiredJwtException | AuthenticationException ex) {
            log.warn("Expired JWT token");
            httpServletRequest.setAttribute("expired", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT exception");
        } catch (IllegalArgumentException ex) {
            log.warn("Jwt claims string is empty");
        } catch (Exception e) {
            log.warn("Ex");
        }
        return false;
    }

    private List<String> getPermissionNames(List<Permission> permissions) {
        List<String> result = new ArrayList<>();
        permissions.forEach(permission -> result.add(permission.getName()));
        return result;
    }
}
