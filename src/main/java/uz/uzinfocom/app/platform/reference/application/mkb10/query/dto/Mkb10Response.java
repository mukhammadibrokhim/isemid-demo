package uz.uzinfocom.app.platform.reference.application.mkb10.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed MKB-10 classifier node response.")
public record Mkb10Response(

        @Schema(description = "External classifier id, as assigned by the source WHO ICD-10 dataset.", example = "1500")
        Long id,

        @Schema(description = "Legacy/alternate numeric identifier carried over from a prior classification system.",
                example = "1500")
        Long secondaryId,

        @Schema(description = "Parent node's external id. Null for top-level (chapter) nodes.", example = "12")
        Long parentId,

        @Schema(description = "Unique ICD-10 code.", example = "A15")
        String code,

        @Schema(description = "Depth of this node in the classifier hierarchy, starting from 0.", example = "3")
        int level,

        @Schema(description = "Whether this node is a leaf (an assignable diagnosis code) rather than a category heading.",
                example = "true")
        boolean lastLevel,

        @Schema(description = "Diagnosis name in Uzbek Latin.", example = "Tuberkulyoz")
        String nameUz,

        @Schema(description = "Diagnosis name in Uzbek Cyrillic.", example = "Туберкулёз")
        String nameUzCyril,

        @Schema(description = "Diagnosis name in Russian.", example = "Туберкулёз")
        String nameRu,

        @Schema(description = "Diagnosis name in Karakalpak.", example = "Túberkulez")
        String nameKaa,

        @Schema(description = "Free-form remark about this classifier node.")
        String comment,

        @Schema(description = "How many times this code may be used/selected.", example = "1")
        Integer usageLimit,

        @Schema(description = "Number of direct child nodes under this node.", example = "5")
        long childrenCount,

        @Schema(description = "Soft-delete flag.", example = "false")
        Boolean deleted
) {
}
