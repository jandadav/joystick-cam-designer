package com.example.components;

import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

public class SimStep {

    public float joyRotation;
    public float camRotation;

    public float springMomentum;
    public float contactForceSize;
    public float joyArmMomentum;
    public PVector springForce;

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

    public List<String> messages = new ArrayList<>();
}
