package com.abdulatif.emis.model.util;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pagination<T> {
    private Integer page;
    private Integer limit;
    @Getter
    @JsonIgnore
    private String order;
    @Getter
    @JsonIgnore
    private String type;
    @Getter
    private T search;
    @Getter
    @JsonIgnore
    private List<String> sortBy;
    @Getter
    @JsonIgnore
    private List<Boolean> sortDesc;

    public Integer getPage() {
        return page < 0 ? 1 : page;
    }
    public Integer getLimit() {
        return limit <= 0 ? 10 : limit;
    }
}
