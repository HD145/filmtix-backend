package com.driver.bookMyShow.Config;

import com.driver.bookMyShow.Models.UserEntity;
import com.driver.bookMyShow.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsImpl implements UserDetailsService {

    @Autowired
    public UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.print(email);
        UserEntity user = userRepository.findByEmailId(email);
        if (user != null) {
            return User.withUsername(user.getEmailId())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build();
        }

        throw new UsernameNotFoundException("Unknown user " + email);
    }
}
