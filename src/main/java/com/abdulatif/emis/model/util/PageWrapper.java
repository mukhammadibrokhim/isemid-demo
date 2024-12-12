package com.abdulatif.emis.model.util;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PageWrapper {
    List<?> list;
    long total;
}
