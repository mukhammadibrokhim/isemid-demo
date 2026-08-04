package uz.uzinfocom.isemid.features.report.job;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.form058.specifications.Form058SpecificationFactory;
import uz.uzinfocom.isemid.features.report.utils.Form058ExcelWriter;
import uz.uzinfocom.isemid.features.user.models.User;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * {@code form058ExportJobExecutor} (bitta thread) da fon jarayonida ishlaydi.
 * Alohida bean sifatida mavjud, chunki {@code @Async} faqat Spring proxy chegarasi
 * orqali chaqirilganda ishlaydi (self-invocation ishlamaydi).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Form058ExportJobWorker {
    private final Form058Repository form058Repository;
    private final Form058SpecificationFactory specFactory;
    private final Form058ExcelWriter excelWriter;

    /**
     * {@code @Transactional} shart — {@code streamAll} o'zi ham alohida
     * {@code @Transactional} bo'lgani uchun, agar bu yerda tashqi tranzaksiya
     * bo'lmasa, u faqat stream ochilganda yopilib ketadi va keyinchalik
     * (yozish paytida) stream'ni o'qishga urinish xatolikka olib keladi.
     */
    @Async("form058ExportJobExecutor")
    @Transactional
    public void generate(Form058ExportJob job, Path exportBaseDir, FilterType filterType,
                         LocalDate fromDate, LocalDate toDate, String stateCode, String cityCode, User user) {
        try {
            Path dir = exportBaseDir.resolve(filterType.name());
            Files.createDirectories(dir);

            Path target = dir.resolve("journal_060_" + job.getKey() + ".xlsx");
            System.out.println("start stream -------------------"+ LocalDateTime.now());
            try (Stream<Form058> formStream = form058Repository.streamAll(
                    specFactory.build(fromDate, toDate, stateCode, cityCode, user, filterType))) {
                String orgName = user.getCurrentOrganization() != null ? user.getCurrentOrganization().getName() : null;
                excelWriter.writeV3(formStream, fromDate, toDate, orgName, target);
            }

            System.out.println("end stream -------------------"+ LocalDateTime.now());
            job.setFileName("journal_060_" + LocalDate.now() + ".xlsx");
            job.setFilePath(target);
            job.setCompletedAt(Instant.now());
            job.setStatus(Form058ExportJobStatus.COMPLETED);
            log.info("[Form058ExportV3] tayyor: key={}, file={}", job.getKey(), target);
        } catch (Exception e) {
            log.error("[Form058ExportV3] xatolik: key={}", job.getKey(), e);
            job.setErrorMessage(e.getMessage());
            job.setStatus(Form058ExportJobStatus.FAILED);
        }
    }
}
