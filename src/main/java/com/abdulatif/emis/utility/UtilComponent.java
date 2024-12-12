package com.abdulatif.emis.utility;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class UtilComponent {


    public static Integer calculateOffset(Integer page, Integer limit) {
        return (limit * (page + 1)) - limit;
    }

}
