package com.example.components;

import com.example.utils.Range;
import com.example.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static com.example.utils.Utils.*;

@Slf4j
public class CoordsPlot extends Polygon {

    private final List<PShape> geometry = computeGeometry();
    private PVector center;

    public CoordsPlot(PApplet it) {
        super(it);
    }

    @Override
    public void display() {
        super.display();
        drawGrid(100, 800, 1);

        drawGeometryVertex();
        it.shape(pivot);
    }

    private List<PShape> computeGeometry() {

        center = new PVector(200, 200);

        // ARCS

        List<PShape> results = new ArrayList<>();
        PShape _in = it.createShape();
        PShape _out = it.createShape();
        PShape _curve = it.createShape();
        results.add(_in);
        results.add(_out);
        results.add(_curve);

        _in.beginShape();
        _in.fill(0, 0);
        _in.stroke(color(255, 255, 0));
        _in.strokeWeight(2);

        _out.beginShape();
        _out.fill(0, 0);
        _out.stroke(color(128, 90, 200));
        _out.strokeWeight(2);

        _curve.beginShape();
        _curve.fill(0, 0);
        _curve.stroke(color(30, 220, 20));
        _curve.strokeWeight(2);

        PVector vec1 = new PVector(0, 200);
        PVector vec2 = new PVector(0, 60);

        int curveSteps = 5;
        Range<Float> range1 = new Range<>(0.0f, 20*PI/180, curveSteps);
        Range<Float> range2 = new Range<>(0.0f, 80*PI/180, curveSteps);
        Range<Float> range3 = new Range<>(0.0f, 30*PI/180, curveSteps);

        while (range1.hasNext()) {
            _in.vertex(vec1.x, vec1.y);

            PVector outer = PVector.add(vec1, vec2);
            _out.vertex(outer.x, outer.y);

            log.info("{}", range3.getValue());
            PVector curve = rotateAround(outer, center, range3.getValue());
            _curve.vertex(curve.x, curve.y);


            vec1.rotate(range1.getIncrement());
            vec2.rotate(range2.getIncrement());


            range1.next();
            range2.next();
            range3.next();
        }

        _in.endShape();
        _out.endShape();
        _curve.endShape();

        // LINE with rotating points
        it.ellipseMode(CENTER);

        return results;
    }

    private void drawGeometryVertex() {
        this.geometry.forEach(pShape -> it.shape(pShape));
        it.ellipse(center.x, center.y, 20f, 20f);
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
}
