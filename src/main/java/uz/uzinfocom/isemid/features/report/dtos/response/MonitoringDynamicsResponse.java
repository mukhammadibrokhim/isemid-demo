package uz.uzinfocom.isemid.features.report.dtos.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MonitoringDynamicsResponse {
    private LocalDate toDate;
    private List<Item> report = new ArrayList<>();

    @Getter @Setter
    public static class Item {
        private String period;
        private String locationCode;

        private int totalCases;
        private int confirmed;
        private int unconfirmed;

        private int male;
        private int female;

        private AgeGroupCount ageGroups = new AgeGroupCount();

        private Map<String, Integer> diseaseType = new LinkedHashMap<>();

    }

    @Getter @Setter
    public static class AgeGroupCount {
        private int age_0_3;
        private int age_3_6;
        private int age_6_10;
        private int age_10_17;
        private int age_18_25;
        private int age_25_45;
        private int age_45_65;
        private int age_65_plus;
    }
}
