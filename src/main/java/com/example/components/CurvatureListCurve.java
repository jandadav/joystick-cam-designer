package com.example.components;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import processing.core.PConstants;
import processing.core.PVector;

import java.util.ArrayList;
import static java.lang.Math.*;

public class CurvatureListCurve {
    private PVector origin;
    private PVector direction;
    private float step;

    @Getter
    ArrayList<PVector> points = new ArrayList<>();
    @Getter
    ArrayList<PVector> inflexPoints = new ArrayList<>();
    @Getter
    ArrayList<PVector> controlPoints = new ArrayList<>();

    @Data
    @RequiredArgsConstructor
    public static class CurveArc{
        private final float curvature;
        private final float length;
    }

    public CurvatureListCurve(PVector origin, PVector direction, float step, CurveArc... arcs) {
        this.origin = origin;
        this.direction = direction;
        this.step = step;


        for (CurveArc arc : arcs) {
            points.add(origin);
            inflexPoints.add(origin);

            PVector rForward = new PVector(direction.x, direction.y).rotate(PConstants.HALF_PI).setMag(arc.curvature);
            PVector rBackward = new PVector(-rForward.x, -rForward.y);


            PVector arcCenter = PVector.add(origin, rForward);
            controlPoints.add(arcCenter);

            int numSteps = (int) (arc.length / step);
            float lastStep = arc.length - (numSteps * step);

            float stepAlpha = (float) (2 * asin(step / (2 * arc.curvature)));
            float lastStepAlpha = (float) (2 * asin(lastStep / (2 * arc.curvature)));

            /*System.out.println("numSteps: " + numSteps);
            System.out.println("lastStep: " + lastStep);
            System.out.println("stepAlpha: " + stepAlpha);
            System.out.println("lastStepAlpha: " + lastStepAlpha);*/

            for (int i=0; i<numSteps; i++) {
                PVector v = PVector.add(arcCenter, rBackward.rotate(stepAlpha));
                points.add(v);
            }

            if (lastStep != 0.0f) {
                PVector v = PVector.add(arcCenter, rBackward.rotate(lastStepAlpha));
                points.add(v);
            }

            origin = points.get(points.size() - 1);
            if (arc.curvature >= 0) {
                direction = new PVector(rBackward.x, rBackward.y).rotate(PConstants.HALF_PI);
            } else {
                direction = new PVector(rBackward.x, rBackward.y).rotate(-PConstants.HALF_PI);
            }

        }






    }
}