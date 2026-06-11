package uz.uzinfocom.isemid.features.report.specification;

public class OrganizationLevelSpecification {

  // language=sql
  public static final String REGION_WISE_SPECIFICATION = """
      with base as (SELECT c.code,
        CASE 'uz' WHEN 'uz-Cyrl' THEN c.name_uz_cyril 
          WHEN 'ru' THEN c.name_ru 
          WHEN 'kaa' THEN c.name_kaa 
          ELSE c.name_uz 
        END AS name, 
        f.id, f.status FROM cls_catalog c 
        LEFT JOIN pt_address a 
          ON a.state_code = c.code AND a.type = 'TEMPORARY' 
        LEFT JOIN form058 f 
          ON f.patient_id = a.patient_id
          AND (CAST(:fromDate AS date)     IS NULL OR f.created_at >= :fromDate)
          AND (CAST(:toDate AS date)       IS NULL OR f.created_at <  :toDate)
          AND (CAST(:mkb10Code AS varchar) IS NULL OR f.final_mkb10code = :mkb10Code)  
        LEFT JOIN organization co 
          ON co.uuid = f.created_org_uuid 
          WHERE c.type = 'state' 
      ) 
      SELECT code, name, 
      COUNT(*) FILTER (WHERE id IS NOT NULL AND status = 'APPROVED') AS confirmed, 
      COUNT(*) FILTER (WHERE id IS NOT NULL AND status IS DISTINCT FROM 'APPROVED') AS unconfirmed, 
      GROUPING(code) AS is_total FROM base 
      GROUP BY GROUPING SETS ((code, name), ()) ORDER BY is_total, code
      """;
  // language=sql
  public static final String DISTRICT_WISE_SPECIFICATION = """
      with base as (SELECT c.code,
        CASE 'uz' WHEN 'uz-Cyrl' THEN c.name_uz_cyril 
          WHEN 'ru' THEN c.name_ru 
          WHEN 'kaa' THEN c.name_kaa 
          ELSE c.name_uz 
        END AS name, 
        f.id, f.status FROM cls_catalog c 
        LEFT JOIN pt_address a 
          ON a.city_code = c.code AND a.type = 'TEMPORARY' 
        LEFT JOIN form058 f 
          ON f.patient_id = a.patient_id
          AND (CAST(:fromDate AS date)     IS NULL OR f.created_at >= :fromDate)
          AND (CAST(:toDate AS date)       IS NULL OR f.created_at <  :toDate)
          AND (CAST(:mkb10Code AS varchar) IS NULL OR f.final_mkb10code = :mkb10Code)  
        LEFT JOIN organization co 
          ON co.uuid = f.created_org_uuid 
          WHERE c.type = 'city'
          AND   c.parent_code= :parentCode
      ) 
      SELECT code, name, 
      COUNT(*) FILTER (WHERE id IS NOT NULL AND status = 'APPROVED') AS confirmed, 
      COUNT(*) FILTER (WHERE id IS NOT NULL AND status IS DISTINCT FROM 'APPROVED') AS unconfirmed, 
      GROUPING(code) AS is_total FROM base 
      GROUP BY GROUPING SETS ((code, name), ()) ORDER BY is_total, code
      """;
  // language=sql
  public static final String NEIGHBORHOOD_WISE_SPECIFICATION = """
      with base as (
      SELECT c.code,
        CASE 'uz' WHEN 'uz-Cyrl' THEN c.name_uz_cyril 
          WHEN 'ru' THEN c.name_ru 
          WHEN 'kaa' THEN c.name_kaa 
          ELSE c.name_uz 
        END AS name, 
        f.id, f.status FROM cls_catalog c 
        LEFT JOIN pt_address a 
          ON a.neighborhood_code = c.code AND a.type = 'TEMPORARY' 
        LEFT JOIN form058 f 
          ON f.patient_id = a.patient_id
          AND (CAST(:fromDate AS date)     IS NULL OR f.created_at >= :fromDate)
          AND (CAST(:toDate AS date)       IS NULL OR f.created_at <  :toDate)
          AND (CAST(:mkb10Code AS varchar) IS NULL OR f.final_mkb10code = :mkb10Code)  
        LEFT JOIN organization co 
          ON co.uuid = f.created_org_uuid 
          WHERE c.type = 'neighborhood' 
          AND   c.parent_code= :parentCode
      ) 
      SELECT code, name, 
      COUNT(*) FILTER (WHERE id IS NOT NULL AND status = 'APPROVED') AS confirmed, 
      COUNT(*) FILTER (WHERE id IS NOT NULL AND status IS DISTINCT FROM 'APPROVED') AS unconfirmed, 
      GROUPING(code) AS is_total FROM base 
      GROUP BY GROUPING SETS ((code, name), ()) ORDER BY is_total, code
      """;

  // language=sql
  public static final String OWN_ORGANIZATION_SPECIFICATION = """
      with base as (
      SELECT c.code,
        CASE 'uz' WHEN 'uz-Cyrl' THEN c.name_uz_cyril 
          WHEN 'ru' THEN c.name_ru 
          WHEN 'kaa' THEN c.name_kaa 
          ELSE c.name_uz 
        END AS name, 
        f.id, f.status FROM cls_catalog c 
        LEFT JOIN pt_address a 
          ON a.neighborhood_code = c.code AND a.type = 'TEMPORARY' 
        LEFT JOIN form058 f 
          ON f.patient_id = a.patient_id
          AND (CAST(:fromDate AS date)     IS NULL OR f.created_at >= :fromDate)
          AND (CAST(:toDate AS date)       IS NULL OR f.created_at <  :toDate)
          AND (CAST(:mkb10Code AS varchar) IS NULL OR f.final_mkb10code = :mkb10Code)  
        LEFT JOIN organization co 
          ON co.uuid = f.created_org_uuid 
          WHERE c.type = 'neighborhood' 
          AND   c.parent_code= :parentCode
          AND f.sender_organization_id = :currentOrganizationId
      ) 
      SELECT code, name, 
      COUNT(*) FILTER (WHERE id IS NOT NULL AND status = 'APPROVED') AS confirmed, 
      COUNT(*) FILTER (WHERE id IS NOT NULL AND status IS DISTINCT FROM 'APPROVED') AS unconfirmed, 
      GROUPING(code) AS is_total FROM base 
      GROUP BY GROUPING SETS ((code, name), ()) ORDER BY is_total, code
      """;


}
