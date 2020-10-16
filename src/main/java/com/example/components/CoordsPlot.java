package com.example.components;

import com.example.utils.Range;
import lombok.extern.slf4j.Slf4j;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

@Slf4j
public class CoordsPlot extends Polygon {

    public int spacing = 50;

    public CoordsPlot(PApplet it) {
        super(it);
    }

    @Override
    public void display() {
        super.display();
        drawGrid(100, 800, 1);

        drawGeometry();
    }

    private void drawGrid(int spacing, int size, int weight) {
        it.stroke(0);
        it.strokeWeight(weight);

        it.fill(255, 80);
        it.rect(-size/2, -size/2 , size, size);

        for (int i = -(size / 2 / spacing); i < (size / 2 / spacing)+1; i++) {
            it.line(spacing * i, -size / 2, spacing * i, size / 2);
        }
        for (int i = -(size / 2 / spacing); i < (size / 2 / spacing)+1; i++) {
            it.line(-size / 2, spacing * i, size / 2, spacing * i);
        }
    }

    private void drawGeometry() {
        it.stroke(color(255, 255, 0));
        it.strokeWeight(5);
        it.line(0, 0, 0, 500);
        it.line(0, 0, 0, 500);

        ArrayList<PVector> points1 = new ArrayList();
        ArrayList<PVector> points2 = new ArrayList();


        PVector vec1 = new PVector(0, 200);
        PVector vec2 = new PVector(0, 60);

        int curveSteps = 100;
        Range<Float> range1 = new Range<>(0.0f, 20*PI/180, curveSteps);
        Range<Float> range2 = new Range<>(0.0f, 80*PI/180, curveSteps);

        while (range1.hasNext()) {
            points1.add(new PVector(vec1.x, vec1.y));
            points2.add(PVector.add(vec1, vec2));
            //log.info("{} :: {}", points1.get(range1.getIteration()), points2.get(range1.getIteration()));
            if (range1.getIteration() > 0) {
                vec1.rotate(range1.getIncrement());
                vec2.rotate(range2.getIncrement());

                it.stroke(color(130, 20, 160));
                it.line(points1.get(range1.getIteration() - 1).x, points1.get(range1.getIteration() - 1).y, points1.get(range1.getIteration()).x, points1.get(range1.getIteration()).y);

                it.stroke(color(25, 180, 12));
                it.line(points2.get(range1.getIteration() - 1).x, points2.get(range1.getIteration() - 1).y, points2.get(range1.getIteration()).x, points2.get(range1.getIteration()).y);
            }
            range1.next();
            range2.next();
        }
    }
}
