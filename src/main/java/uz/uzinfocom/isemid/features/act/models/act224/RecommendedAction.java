//package uz.uzinfocom.isemid.features.act.models.act224;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import lombok.*;
//import uz.uzinfocom.isemid.commons.models.BaseEntity;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Entity(name = "act224_recommended_actions")
//public class RecommendedAction extends BaseEntity {
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "act224_id")
//    private Act224 act224;
//
//    private String recommendedActivity; // Taklif etilayotgan chora-tadbir
//    private LocalDateTime executionPeriod; // Amalga oshirish muddati
//}
