package uz.uzinfocom.isemid.features.report.service.impls;

import com.opencsv.CSVWriter;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.report.service.Form058ExportService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class Form058ExportServiceImpl implements Form058ExportService {

    private final Form058Repository form058Repository;
    private final OrganizationRepository organizationRepository;

    @Override
    public ResponseEntity<?> allExport(User user) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

            String[] header = {"ID", "ФИО пациента", "Дата отправки", "От организации", "Статус"};
            csvWriter.writeNext(header);

            List<Form058> forms = form058Repository.findAll(getFilteredSpecification(user, null));

            for (Form058 form : forms) {

                String fullName = form.getPatient().getFirstName() + " " +
                        form.getPatient().getLastName() + " " +
                        form.getPatient().getMiddleName();

                String organizationName = organizationRepository
                        .findById(form.getSenderOrganizationId())
                        .map(Organization::getName)
                        .orElse("");
                csvWriter.writeNext(new String[]{
                        form.getUuid().toString(),
                        fullName,
                        form.getCreatedAt().toString(),
                        organizationName,
                        form.getStatus().getRussian()
                });
            }


            csvWriter.close();

            String filename = "form_058_" + LocalDate.now() + ".csv";
            HttpHeaders headersHttp = new HttpHeaders();
            headersHttp.add("Content-Disposition", "attachment; filename=" + filename);
            headersHttp.add("Content-Type", "text/csv; charset=UTF-8");
            headersHttp.add("Content-Transfer-Encoding", "binary");
            return ResponseEntity.ok()
                    .headers(headersHttp)
                    .body(out.toByteArray());


        } catch (Exception e) {
            log.error("Ошибка при экспорте в CSV: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<?> receivedExport(User user) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

            String[] header = {"ID", "ФИО пациента", "Дата отправки", "От организации", "Статус"};
            csvWriter.writeNext(header);

            List<Form058> forms = form058Repository.findAll(getFilteredSpecification(user, FilterType.RECEIVED));

            for (Form058 form : forms) {

                String fullName = form.getPatient().getFirstName() + " " +
                        form.getPatient().getLastName() + " " +
                        form.getPatient().getMiddleName();

                String organizationName = organizationRepository
                        .findById(form.getSenderOrganizationId())
                        .map(Organization::getName)
                        .orElse("");
                csvWriter.writeNext(new String[]{
                        form.getUuid().toString(),
                        fullName,
                        form.getCreatedAt().toString(),
                        organizationName,
                        form.getStatus().getRussian()
                });
            }


            csvWriter.close();

            String filename = "form_058_" + LocalDate.now() + ".csv";
            HttpHeaders headersHttp = new HttpHeaders();
            headersHttp.add("Content-Disposition", "attachment; filename=" + filename);
            headersHttp.add("Content-Type", "text/csv; charset=UTF-8");
            headersHttp.add("Content-Transfer-Encoding", "binary");
            return ResponseEntity.ok()
                    .headers(headersHttp)
                    .body(out.toByteArray());


        } catch (Exception e) {
            log.error("Ошибка при экспорте в CSV: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    public static Specification<Form058> getFilteredSpecification(
            User currentUser,
            FilterType filterType
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (currentUser.getCurrentOrganization() != null) {
                UUID orgId = currentUser.getCurrentOrganization().getUuid();

                if (filterType == FilterType.RECEIVED) {
                    predicates.add(criteriaBuilder.equal(root.get("receiverOrganizationId"), currentUser.getCurrentOrganization().getId()));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("createdOrgUuid"), orgId));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
