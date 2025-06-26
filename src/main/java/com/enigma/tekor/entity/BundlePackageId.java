package com.enigma.tekor.entity;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class BundlePackageId implements Serializable{
    private UUID bundle;
    private UUID testPackage;
    
}
