package com.example.components;

import lombok.Data;
import processing.core.PVector;

public class SimStep {

    public float joyRotation;
    public float camRotation;

    public float springMomentum;
    public float contactForceSize;
    public float joyArmMomentum;

    public PVector joyArmWithRotation;
    public PVector collision;
    public PVector collisionWs;

    public PVector springAnchorFixed;
    public PVector springInitialLength;
    public PVector springAnchorWithRotation;
    public PVector contactForceDirection;
    public PVector springLength;
    public PVector contactArmToCamPivot;
    public PVector contactForceVector;
}
