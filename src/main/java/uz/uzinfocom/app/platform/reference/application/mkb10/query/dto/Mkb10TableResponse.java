package uz.uzinfocom.app.platform.reference.application.mkb10.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "MKB-10 classifier row for paginated table responses.")
public record Mkb10TableResponse(
        @Schema(description = "External classifier id.", example = "1500")
        Long id,
        @Schema(description = "Parent node's external id. Null for top-level (chapter) nodes.", example = "12")
        Long parentId,
        @Schema(description = "Unique ICD-10 code.", example = "A15")
        String code,
        @Schema(description = "Depth of this node in the classifier hierarchy, starting from 0.", example = "3")
        int level,
        @Schema(description = "Whether this node is a leaf (an assignable diagnosis code).", example = "true")
        boolean lastLevel,
        @Schema(description = "Diagnosis name in Uzbek Latin.", example = "Tuberkulyoz")
        String nameUz,
        @Schema(description = "Diagnosis name in Uzbek Cyrillic.", example = "Туберкулёз")
        String nameUzCyril,
        @Schema(description = "Diagnosis name in Russian.", example = "Туберкулёз")
        String nameRu,
        @Schema(description = "Diagnosis name in Karakalpak.", example = "Túberkulez")
        String nameKaa,
        @Schema(description = "Soft-delete flag.", example = "false")
        Boolean deleted
) {
}
