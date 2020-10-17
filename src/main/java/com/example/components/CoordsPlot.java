package com.example.components;

import com.example.utils.Range;
import com.example.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PShape;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static com.example.utils.Utils.*;

@Slf4j
public class CoordsPlot extends Polygon {

    private PVector center;
    private PVector arm = new PVector(0, 200);
    private PVector bearing = new PVector(0, 60);

    private PFont font = it.createFont("Arial", rescale(20), false);
    private final List<PShape> geometry = computeGeometry();

    public CoordsPlot(PApplet it) {
        super(it);
    }

    @Override
    public void display() {
        super.display();
        drawGrid(100, 800, 2);

        drawGeometryVertex();

        it.shape(pivot);
    }

    private void drawGeometryVertex() {

        float rotation = it.mouseX * 0.0005f > 20 * PI/180 ? 20 * PI/180 : it.mouseX * 0.0005f;

        it.ellipseMode(CENTER);

        PVector armTilt = new PVector(arm.x, arm.y).rotate(rotation);
        it.ellipse(armTilt.x, armTilt.y, 120f, 120f);

        PVector bearingTilt = PVector.add(new PVector(bearing.x, bearing.y).rotate(4 * rotation), armTilt);
        it.line(armTilt.x, armTilt.y, bearingTilt.x, bearingTilt.y);


        it.shape(geometry.get(0));
        it.shape(geometry.get(1));

        it.ellipse(center.x, center.y, 20f, 20f);

        it.pushMatrix();
        it.translate(center.x, center.y);
        it.rotate(-(1.5f*rotation));
        it.translate(-center.x, -center.y);
        it.shape(geometry.get(2));
        it.popMatrix();


        it.fill(color(20,50,219));
        PVector springCenter = new PVector(250, 400);
        PVector springCenterMirror = new PVector(-springCenter.x, springCenter.y);
        PVector springCenterWithRotation = rotateAround(springCenter, center, -1.5f * rotation);
        it.ellipse(springCenterWithRotation.x, springCenterWithRotation.y, 20f, 20f);
        it.ellipse(springCenterMirror.x, springCenterMirror.y, 20f, 20f);

        it.stroke(color(200,0,0));
        it.strokeWeight(rescale(3));
        it.line(springCenterMirror.x, springCenterMirror.y, springCenterWithRotation.x, springCenterWithRotation.y);
        PVector springVector = PVector.sub(springCenterWithRotation, springCenterMirror);



        it.scale(1, -1);
        it.fill(255);
        it.textFont(font);
        it.text("Spring Delta: " + springVector.mag(), -150, -300);

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
        _in.strokeWeight(rescale(3));

        _out.beginShape();
        _out.fill(0, 0);
        _out.stroke(color(128, 90, 200));
        _out.strokeWeight(rescale(3));

        _curve.beginShape();
        _curve.fill(0, 0);
        _curve.stroke(color(30, 220, 20));
        _curve.strokeWeight(rescale(3));

        PVector vec1 = new PVector(0, 200);
        PVector vec2 = new PVector(0, 60);

        int curveSteps = 20;
        Range<Float> range1 = new Range<>(0.0f, 20*PI/180, curveSteps);
        Range<Float> range2 = new Range<>(0.0f, 80*PI/180, curveSteps);
        Range<Float> range3 = new Range<>(0.0f, 30*PI/180, curveSteps);

        while (range1.hasNext()) {
            _in.vertex(vec1.x, vec1.y);

            PVector outer = PVector.add(vec1, vec2);
            _out.vertex(outer.x, outer.y);

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


        return results;
    }

    private void drawGrid(int spacing, int size, int weight) {
        it.stroke(0);
        it.strokeWeight(rescale(weight));

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
