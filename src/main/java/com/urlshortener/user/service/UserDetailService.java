package com.urlshortener.user.service;

import com.urlshortener.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class UserDetailService implements UserDetailsService {

    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (username == null) {
            throw new UsernameNotFoundException("campo vazio");
        }

        return repository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
