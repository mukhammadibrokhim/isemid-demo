package com.abdulatif.emis.utility;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PageWrapper {
    List<?> list;
    long total;
}
