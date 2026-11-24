package uz.uzinfocom.app.orchestration.devseed;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Real reference rows the dev seeder draws from, so seeded cases land on
 * organizations/territories/diagnoses that actually exist in the dev DB
 * (reports, scope filters and lookups then resolve them like real data).
 */
record DevSeedReferenceData(
        List<Org> organizations,
        Map<String, List<Org>> sesOrganizationsByRegion,
        List<Org> sesOrganizations,
        List<Icd10> icd10Codes,
        List<Icd10> animalBiteIcd10Codes,
        List<String> categoryCodes
) {

    record Org(Long id, UUID uuid, String name, String regionCode, String districtCode, String medicalType) {
    }

    record Icd10(String code, String name) {
    }

    static DevSeedReferenceData load(JdbcTemplate jdbc) {
        List<Org> organizations = jdbc.query("""
                        select id, uuid, coalesce(name_uz, name_uz_cyril, name_ru, name_kaa) as name, region_code, district_code, medical_type
                        from organization
                        where coalesce(active, true) = true
                          and region_code is not null
                          and district_code is not null
                        order by id
                        limit 3000
                        """,
                (rs, i) -> new Org(
                        rs.getLong("id"),
                        rs.getObject("uuid", UUID.class),
                        rs.getString("name"),
                        rs.getString("region_code"),
                        rs.getString("district_code"),
                        rs.getString("medical_type")
                ));

        List<Org> ses = organizations.stream()
                .filter(org -> "SANEPID_SERVICE".equals(org.medicalType()))
                .toList();

        // Prefer the diagnoses the statistical reports (Form 12/13/28.x)
        // actually count, so seeded cases show up there; fall back to any
        // leaf ICD-10 code if the manual-report catalog isn't seeded.
        List<Icd10> icd10 = jdbc.query("""
                        select distinct i.code, coalesce(i.name_uz, i.name_ru, i.code) as name
                        from ref_icd10 i
                        join ref_manual_report_icd10_codes m on m.icd10_code = i.code
                        where i.deleted = false
                        limit 400
                        """,
                (rs, n) -> new Icd10(rs.getString("code"), rs.getString("name")));
        if (icd10.isEmpty()) {
            icd10 = jdbc.query("""
                            select code, coalesce(name_uz, name_ru, code) as name
                            from ref_icd10
                            where deleted = false and is_last_level = true
                            order by code
                            limit 400
                            """,
                    (rs, n) -> new Icd10(rs.getString("code"), rs.getString("name")));
        }

        List<Icd10> animalBite = jdbc.query("""
                        select code, coalesce(name_uz, name_ru, code) as name
                        from ref_icd10
                        where deleted = false
                          and (code like 'W54%' or code like 'W55%' or code like 'A82%' or code like 'T14%')
                        order by code
                        limit 50
                        """,
                (rs, n) -> new Icd10(rs.getString("code"), rs.getString("name")));
        if (animalBite.isEmpty()) {
            animalBite = icd10;
        }

        List<String> categories = jdbc.queryForList(
                "select code from ref_catalog where type = 'CATEGORY' and deleted = false",
                String.class);

        return new DevSeedReferenceData(
                organizations,
                ses.stream().collect(Collectors.groupingBy(Org::regionCode)),
                ses,
                icd10,
                animalBite,
                new ArrayList<>(categories)
        );
    }

    boolean isUsable() {
        return !organizations.isEmpty() && !icd10Codes.isEmpty();
    }

    Org randomOrganization(Random random) {
        return pick(organizations, random);
    }

    /** A sanepid (SES) org in the sender's region when one exists — the realistic receiver of a notification. */
    Org receiverFor(Org sender, Random random) {
        List<Org> sameRegion = sesOrganizationsByRegion.get(sender.regionCode());
        if (sameRegion != null && !sameRegion.isEmpty()) {
            return pick(sameRegion, random);
        }
        if (!sesOrganizations.isEmpty()) {
            return pick(sesOrganizations, random);
        }
        return randomOrganization(random);
    }

    static <T> T pick(List<T> values, Random random) {
        return values.get(random.nextInt(values.size()));
    }
}
