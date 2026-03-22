package com.sample.marketplace.security;

import com.sample.marketplace.models.User;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MarketplaceUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MarketplaceUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] parts = username.split(":", 2);
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Username must be in ROLE:email format");
        }

        Role role;
        try {
            role = Role.valueOf(parts[0]);
        } catch (IllegalArgumentException ex) {
            throw new UsernameNotFoundException("Invalid role supplied");
        }

        return loadUserByEmailAndRole(parts[1], role);
    }

    public AuthenticatedUser loadUserByEmailAndRole(String email, Role role) {
        User user = userRepository.findByEmailAndRole(email, role)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new AuthenticatedUser(user);
    }
}
