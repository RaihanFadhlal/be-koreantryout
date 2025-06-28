package com.enigma.tekor.service;

import com.enigma.tekor.entity.Role;

public interface RoleService {
    Role getOrSave(String roleName);
}
