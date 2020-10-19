package com.example;

import com.example.utils.Range;
import com.example.utils.Utils;
import grafica.GPlot;
import grafica.GPointsArray;
import lombok.extern.slf4j.Slf4j;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PShape;
import processing.core.PVector;

import static com.example.utils.Utils.*;

@Slf4j
public class App extends PApplet {

    float joyArmLength = 300f;
    float joyBearingRadius = 50f;
    float contactPointLimitAngle = 80f;
    float camLimitAngle = 30f;
    float joyLimitAngle = 20f;

    private PFont font;
    private final PVector camPivot = new PVector(200,200);
    private final PVector springPivot = new PVector(250, 400);
    private final int curveSteps = 20;
    private PShape _joyArmSweep;
    private PShape _contactSweep;
    private PShape _camCurve;
    private GPlot plot;
    private PVector[] camCurvePoints;


    @Override
    public void settings() {
        super.settings();
        size(900,900, P2D);
        smooth();
    }

    @Override
    public void setup() {
        ellipseMode(CENTER);
        font = createFont("Consolas", 20, true);
        plot = new GPlot(this);

        // Prepare the points for the plot
        int nPoints = 10;
        GPointsArray points = new GPointsArray(nPoints);

        for (int i = 0; i < nPoints; i++) {
            points.add(i, quadratic(20, 50, i / Float.valueOf(nPoints)) );
        }

        // Create a new plot and set its position on the screen
        plot.setPos(430, 10);
        plot.setAllFontProperties("Consolas", 0, 12);

        // Set the plot title and the axis labels
        plot.setTitleText("Contact point rotation curve");
        plot.getXAxis().setAxisLabelText("Joy arm angle");
        plot.getYAxis().setAxisLabelText("Contact point angle");

        // Add the points
        plot.setPoints(points);

    }

    @Override
    public void draw() {

        background(100);
        pushMatrix();
        translate(width/2, height-100);
        scale(1, -1);

        // GRID
        drawGrid(100, 800, 1, 0,300);
        fill(color(255,0,0));
        ellipse(0, 0, 20, 20);

        // INPUT
        //float rotation = lerp(0, 1, mouseX / Float.valueOf(width));
        float rotation = 0;
        //float camRotation = lerp(0, radians(camLimitAngle), rotation);
        float camRotation = quadratic(0, radians(camLimitAngle), rotation);
        float contactRotation = lerp(0, radians(contactPointLimitAngle), rotation);
        float joyRotation = lerp(0, radians(joyLimitAngle), rotation);

        // VECTORS
        fill(color(20,50,219));
        PVector joyArm = new PVector(0, joyArmLength);
        PVector joyArmWithRotation = new PVector(joyArm.x, joyArm.y).rotate(joyRotation);
        PVector joyBearing = new PVector(0, joyBearingRadius);
        PVector joyContact = PVector.add(joyArmWithRotation, new PVector(joyBearing.x, joyBearing.y).rotate(contactRotation));
        PVector springAnchorFixed = new PVector(-springPivot.x, springPivot.y);
        PVector springAnchorWithRotation = rotateAround(springPivot, camPivot, -camRotation);


        // SHAPES
        calculateShapes(joyArm, joyBearing);

        // RENDER
        ellipse(camPivot.x, camPivot.y, 20f, 20f);
        ellipse(springAnchorWithRotation.x, springAnchorWithRotation.y, 20f, 20f);
        ellipse(springAnchorFixed.x, springAnchorFixed.y, 20f, 20f);

        ellipse(joyArmWithRotation.x, joyArmWithRotation.y, 2 * joyBearingRadius, 2 * joyBearingRadius);
        line(0,0, joyArmWithRotation.x, joyArmWithRotation.y);
        line(joyArmWithRotation.x,joyArmWithRotation.y, joyContact.x, joyContact.y);

        shape(_joyArmSweep);
        shape(_contactSweep);

        pushMatrix();
        translate(camPivot.x, camPivot.y);
        rotate(-camRotation);
        translate(-camPivot.x, -camPivot.y);
        shape(_camCurve);

        PVector[] camCurveScreen = pointsToScreenCoords(camCurvePoints, this);

        popMatrix();

        strokeWeight(3);
        stroke(color(200,0,0));
        line(springAnchorFixed.x, springAnchorFixed.y, springAnchorWithRotation.x, springAnchorWithRotation.y);

        popMatrix();

        pushMatrix();
        translate(mouseX, mouseY);

        // CollisionCircle
        PVector[] circlePoints = new PVector[12];
        // CIRCLE
        PShape circle = createShape();
        circle.beginShape();
        circle.fill(0);
        circle.strokeWeight(1);
        circle.stroke(255);
        PVector point = new PVector(0,0);
        for (int i=0; i <12; i++) {
            point = new PVector(60, 0).rotate(i * PI / 6);
            //circle.vertex(point.x, point.y);
            circlePoints[i] = point;
        }

        PVector[] circlePointsScreen = pointsToScreenCoords(circlePoints, this);
        boolean hit = polyPoly(camCurveScreen, circlePointsScreen);

        if (hit) circle.fill(255,150,0);
        else circle.fill(0,150,255);

        for (int i=0; i <12; i++) {
            circle.vertex(circlePoints[i].x, circlePoints[i].y);
        }

        circle.endShape();

        shape(circle);

        popMatrix();

        // DEBUG

        fill(0, 200);
        strokeWeight(1);
        stroke(0);
        rect(10, 10, 400, 300);
        fill(255);
        textFont(font);
        text("Rotation: " + Utils.degrees(rotation) + " deg", 20, 40);

        // PLOT
        plot.defaultDraw();


    }

    private void calculateShapes(PVector joyArm, PVector joyBearing) {
        _joyArmSweep = createShape();
        _contactSweep = createShape();
        _camCurve = createShape();

        _joyArmSweep.beginShape();
        _joyArmSweep.fill(0, 0);
        _joyArmSweep.stroke(color(255, 255, 0));
        _joyArmSweep.strokeWeight(3);

        _contactSweep.beginShape();
        _contactSweep.fill(0, 0);
        _contactSweep.stroke(color(128, 90, 200));
        _contactSweep.strokeWeight(3);

        _camCurve.beginShape();
        _camCurve.fill(0, 0);
        _camCurve.stroke(color(30, 220, 20));
        _camCurve.strokeWeight(3);

        Range<Float> joyRange = new Range<>(0.0f, radians(joyLimitAngle), curveSteps);
        Range<Float> contactRange = new Range<>(0.0f, radians(contactPointLimitAngle), curveSteps);
        Range<Float> camRotationRange = new Range<>(0.0f, radians(camLimitAngle), curveSteps);

        PVector joyArmSweep = new PVector(joyArm.x, joyArm.y);
        PVector joyBearingSweep = new PVector(joyBearing.x, joyBearing.y);

        camCurvePoints = new PVector[curveSteps];

        while (joyRange.hasNext()) {

            _joyArmSweep.vertex(joyArmSweep.x, joyArmSweep.y);

            PVector contact = PVector.add(joyArmSweep, joyBearingSweep);
            _contactSweep.vertex(contact.x, contact.y);

            PVector camCurve = rotateAround(contact, camPivot, quadratic(0, radians(camLimitAngle), camRotationRange.getIterationNormalized()));
            _camCurve.vertex(camCurve.x, camCurve.y);

            camCurvePoints[joyRange.getIteration()] = camCurve;

            joyArmSweep.rotate(joyRange.getIncrement());
            joyBearingSweep.rotate(contactRange.getIncrement());

            joyRange.next();
            contactRange.next();
            camRotationRange.next();
        }
        _joyArmSweep.endShape();
        _contactSweep.endShape();
        _camCurve.endShape();
    }

    public static void main(String... args) {
        PApplet.main(App.class);
    }

    private void drawGrid(int spacing, int size, int weight, int xOffset, int yOffset) {
        stroke(0);
        strokeWeight(1);

        fill(255, 80);
        rect(-size/2 + xOffset, -size/2 + yOffset , size, size);

        for (int i = ((-(size / 2) + xOffset) / spacing); i < (((size / 2) + xOffset) / spacing)+1; i++) {
            line(spacing * i, -size / 2 + yOffset, spacing * i, size / 2 + yOffset);
        }
        for (int i = ((-(size / 2) + yOffset) / spacing); i < (((size / 2) + yOffset) / spacing)+1; i++) {
            line(-size / 2 + xOffset, spacing * i, size / 2 + xOffset, spacing * i);
        }
    }
}
