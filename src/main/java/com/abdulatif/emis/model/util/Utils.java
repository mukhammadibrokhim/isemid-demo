package com.abdulatif.emis.model.util;


import com.abdulatif.emis.model.user.User;
import com.abdulatif.emis.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.SecureRandom;
import java.util.*;

@Slf4j
public class Utils {

    private static final SecureRandom random = new SecureRandom();
    private static final String NUMERIC = "0123456789";

    public static User currentUser(UserService userService) {
        return userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public static PageRequest pagination(Pagination<?> pagination) {
        return PageRequest.of(pagination.getPage(), pagination.getLimit());
    }

    public static Pageable getPageableFromPagination(Pagination<?> pagination) {
        Pageable paging;
        if (pagination.getSortBy() == null || pagination.getSortBy().isEmpty())
            paging = PageRequest.of(pagination.getPage(), pagination.getLimit(), Sort.by("id").descending());
        else {
            List<Sort.Order> orders = new ArrayList<Sort.Order>();
            List<String> sortBy = pagination.getSortBy();
            List<Boolean> sortDesc = pagination.getSortDesc();
            for (int i = 0; i < sortBy.size(); i++) {
                Sort.Direction direction;
                if (sortDesc.get(i))
                    direction = Sort.Direction.DESC;
                else direction = Sort.Direction.ASC;
                Sort.Order order = new Sort.Order(direction, sortBy.get(i));
                orders.add(order);
            }
            paging = PageRequest.of(pagination.getPage(), pagination.getLimit(), Sort.by(orders));
        }
        return paging;
    }

    public static String removeLeadingZeros(String str) {
        String regex = "^0+(?!$)";
        str = str.replaceAll(regex, "");
        return str;
    }
}
