package com.example.utils;

import processing.core.PVector;

import static java.lang.Math.sqrt;
import static processing.core.PConstants.PI;

public class Utils {

    public static PVector rotateAround(PVector position, PVector pivot, float angle) {
        PVector arm = PVector.sub(position, pivot);
        arm.rotate(angle);
        return PVector.add(pivot, arm);
    }

    public static float radians(float degrees) {
        return degrees * PI / 180;
    }
    public static float degrees(float radians) {
        return radians / PI * 180;
    }

    public static float quadratic(float min, float max, float i) {
        return (float) (min + (i * sqrt(max-min) * i *  sqrt(max-min)));
    }
}
