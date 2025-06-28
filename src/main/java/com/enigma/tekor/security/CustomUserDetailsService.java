package com.enigma.tekor.security;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.enigma.tekor.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
     private final UserRepository userRepository;

    // @Override
    // public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    //     return userRepository.findByUsername(username)
    //             .map(CustomUserDetails::new)
    //             .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    // }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        
        try {
            UUID userId = UUID.fromString(identifier);
            return userRepository.findById(userId)
                    .map(CustomUserDetails::new)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + identifier));
        } catch (IllegalArgumentException e) {
            return userRepository.findByUsername(identifier)
                    .map(CustomUserDetails::new)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + identifier));
        }
    }
}
