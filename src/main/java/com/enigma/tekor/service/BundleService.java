package com.enigma.tekor.service;

import java.util.List;
import java.util.UUID;

import com.enigma.tekor.dto.request.BundleRequest;
import com.enigma.tekor.dto.request.BundleUpdateRequest;
import com.enigma.tekor.dto.response.BundleResponse;
import com.enigma.tekor.entity.Bundle;

public interface BundleService {
    BundleResponse create(BundleRequest request);
    List<BundleResponse> getAll();
    BundleResponse getById(UUID id);
    Bundle getBundleById(UUID id);
    BundleResponse update(UUID id, BundleUpdateRequest request);
    void delete(UUID id);
}
