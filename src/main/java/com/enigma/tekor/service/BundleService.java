package com.enigma.tekor.service;

import com.enigma.tekor.dto.request.BundleRequest;
import com.enigma.tekor.dto.response.BundleResponse;

import java.util.List;
import java.util.UUID;

public interface BundleService {
    BundleResponse create(BundleRequest request);
    List<BundleResponse> getAll();
    BundleResponse getById(UUID id);
}
