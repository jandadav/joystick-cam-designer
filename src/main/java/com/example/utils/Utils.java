package com.example.utils;

import processing.core.PVector;

public class Utils {

    public static PVector rotateAround(PVector position, PVector pivot, float angle) {
        PVector arm = PVector.sub(position, pivot);
        arm.rotate(angle);
        return PVector.add(pivot, arm);
    }

}
