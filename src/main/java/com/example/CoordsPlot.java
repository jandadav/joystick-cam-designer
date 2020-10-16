package com.example;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class CoordsPlot extends Polygon {

    public int spacing = 50;

    public CoordsPlot(PApplet it) {
        super(it);
    }

    @Override
    public void display() {
        super.display();
        it.stroke(0);

        for (int i = -7; i < 7; i++) {
            it.line(spacing * i, 0, spacing * i, 700);
        }
        for (int i = -7; i < 7; i++) {
            it.line(0, spacing * i, 700, spacing * i);
        }

        it.stroke(color(255, 255, 0));
        it.strokeWeight(10);
        it.line(0, 0, 0, 500);
        it.line(0, 0, 0, 500);

        PVector vec1 = new PVector(0, 500);

        it.stroke(color(130, 20, 160));
        ArrayList<PVector> points = new ArrayList();

        for (int i = 0; i <= 6; i++) {
            points.add(new PVector(vec1.x, vec1.y));
            if (i > 0) {
                vec1.rotate(PI/20);
                it.line(points.get(i).x, points.get(i).y, points.get(i - 1).x, points.get(i - 1).y);
            }
        }

        vec1 = new PVector(0, 500);
        PVector vec2 = new PVector(0, 50);
        it.stroke(color(25, 180, 12));
        points = new ArrayList();

        Range<Float> range = new Range<>(0.0f, 1.14f, 5);
        for (int i = 0; i <= range.getSteps(); range.increment()) {

            points.add(PVector.add(vec1, vec2));
            System.out.println(points.get(i));
            if (i > 0) {
                vec1.rotate(range.getStep());
                vec2.rotate(range.getStep());
                it.line(points.get(i).x, points.get(i).y, points.get(i - 1).x, points.get(i - 1).y);
            }
        }
    }
}
