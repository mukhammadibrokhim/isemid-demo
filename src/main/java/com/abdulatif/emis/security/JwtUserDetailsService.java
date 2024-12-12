package com.abdulatif.emis.security;

import com.abdulatif.emis.exception.RecordForbiddenException;
import com.abdulatif.emis.model.user.User;
import com.abdulatif.emis.repository.user.UserRepository;
import com.abdulatif.emis.security.jwt.JwtUser;
import com.abdulatif.emis.security.jwt.JwtUserFactory;
import com.abdulatif.emis.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * Implementation of {@link UserDetailsService} interface for {@link JwtUser}.
 */

@Service
@Slf4j
public class JwtUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public JwtUserDetailsService(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new RecordForbiddenException("User with username: " + username + " not found");
        JwtUser jwtUser = JwtUserFactory.create(user);
        log.info("IN loadUserByUsername - user with username: {} successfully loaded", username);
        return jwtUser;
    }
}
