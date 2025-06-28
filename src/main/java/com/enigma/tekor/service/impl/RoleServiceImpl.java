package com.enigma.tekor.service.impl;

import org.springframework.stereotype.Service;

import com.enigma.tekor.entity.Role;
import com.enigma.tekor.repository.RoleRepository;
import com.enigma.tekor.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public Role getOrSave(String roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName);
            return roleRepository.save(role);
        });
    }

    
}
