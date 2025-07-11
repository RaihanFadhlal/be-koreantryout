package com.enigma.tekor.specification;

import org.springframework.data.jpa.domain.Specification;

import com.enigma.tekor.entity.User;

import com.enigma.tekor.dto.request.SearchUserRequest;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {
    public static Specification<User> getSpecification(SearchUserRequest request){
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getUsername() != null){
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + request.getUsername() + "%"));
            }
            return query.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
    }
}
