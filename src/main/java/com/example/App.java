package com.example;

import com.example.components.SimStep;
import com.example.utils.Range;
import com.example.utils.Utils;
import grafica.GPlot;
import grafica.GPointsArray;
import lombok.extern.slf4j.Slf4j;
import processing.awt.PGraphicsJava2D;
import processing.core.*;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

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
    private final int curveSteps = 80;
    private PShape _joyArmSweep;
    private PShape _contactSweep;
    private PShape _camCurve;
    private GPlot plot;
    private PVector[] camCurvePoints;

    private List<SimStep> simData = new ArrayList<>();
    private int actualSimStep = 0;


    @Override
    public void settings() {
        super.settings();
        size(900,900);
        smooth();
    }


    /**
     * This works in setup, the matrix stack populates
     *
     * PVector force = new PVector(1,0);
     *         pushMatrix();
     *             translate(1f, 1f);
     *             log.info("[{} : {}] ", screenX(force.x, force.y), screenY(force.x, force.y));
     *         popMatrix();
     */

    @Override
    public void setup() {

        // GENERAL SETUP
        ellipseMode(CENTER);
        font = createFont("Consolas", 20, true);

        // STATIC SETUP

        PVector joyArm = new PVector(0, joyArmLength);
        PVector joyBearing = new PVector(0, joyBearingRadius);

        calculateShapes(joyArm, joyBearing);

        // SIM

        int simSteps = 20;
        Range<Float> simRange = new Range<Float>(0f, 1f, simSteps);

        while(simRange.hasNext()) {

            // DATA
            SimStep s = new SimStep();
            simData.add(s);

            // INPUT
            s.joyRotation = lerp(0, radians(joyLimitAngle), simRange.getValue());


            // VECTORS
            s.joyArmWithRotation = new PVector(joyArm.x, joyArm.y).rotate(s.joyRotation);

            // SHAPES
            // COLLISION CIRCLE
            int precision = 360;
            PVector[] circlePoints = new PVector[precision];
            PVector point = new PVector(0,0);
            for (int i=0; i <precision; i++) {
                point = PVector.add(s.joyArmWithRotation, new PVector(joyBearing.x, joyBearing.y).rotate(i * PI / Float.valueOf(precision/2)));
                circlePoints[i] = point;
            }
            PVector[] circlePointsScreen = pointsToScreenCoords(circlePoints, this);

            // COLLISION SIMULATION
            s.collision = null;

            pushMatrix();
                s.camRotation = 0;
                translate(camPivot.x, camPivot.y);
                rotate(-1f);
                s.camRotation -=1f;
                translate(-camPivot.x, -camPivot.y);


                while (s.collision == null) {
                    translate(camPivot.x, camPivot.y);
                    rotate(0.001f);
                    s.camRotation += 0.001f;
                    translate(-camPivot.x, -camPivot.y);

                    PVector[] camCurveScreen = pointsToScreenCoords(camCurvePoints, this);
                    s.collision = polyPoly(camCurveScreen, circlePointsScreen);
                }
            popMatrix();

            // CALCULATE

            s.springAnchorFixed = new PVector(-springPivot.x, springPivot.y);
            s.springInitialLength = PVector.sub(s.springAnchorFixed, springPivot);
            s.springAnchorWithRotation = rotateAround(springPivot, camPivot, s.camRotation);
            s.collisionWs = applyMatrix(s.collision, ((PGraphicsJava2D) g).g2.getTransform());
            s.contactForceDirection = PVector.sub(s.joyArmWithRotation, s.collisionWs);
            s.springLength = PVector.sub(s.springAnchorWithRotation, s.springAnchorFixed);
            // TODO replace with spring toughness scalar
            s.springMomentum = moment(s.springLength.copy().setMag((s.springLength.mag() - s.springInitialLength.mag()) * 10), s.springAnchorWithRotation);
            s.contactArmToCamPivot = PVector.sub(camPivot, s.collisionWs);
            s.contactForceSize = s.springMomentum / (sin(PVector.angleBetween(s.collisionWs, s.contactArmToCamPivot)) * s.contactArmToCamPivot.mag());
            s.contactForceVector = s.contactForceDirection.copy().setMag(s.contactForceSize);
            s.joyArmMomentum = moment(s.contactForceVector, s.joyArmWithRotation);

            simRange.next();
        }

        plot = new GPlot(this);
        int nPoints = 20;
        GPointsArray points = new GPointsArray(nPoints);
        for (int i = 0; i < nPoints; i++) {
            points.add(i, simData.get(i).joyArmMomentum /1000000);
        }

        GPointsArray points2 = new GPointsArray(nPoints);
        for (int i = 0; i < nPoints; i++) {
            points.add(i, simData.get(i).camRotation );
        }

        plot.setPos(430, 10);
        plot.setAllFontProperties("Consolas", 0, 12);
        plot.setTitleText("Contact point rotation curve");
        plot.getXAxis().setAxisLabelText("Joy arm angle");
        plot.getYAxis().setAxisLabelText("Joy arm moment");
        plot.setPoints(points);
        plot.addLayer("camRotation", points2);

    }

    @Override
    public void draw() {

        SimStep s = simData.get(actualSimStep);

        background(100);
        pushMatrix();
            translate(width/2, height-100);
            scale(1, -1);

            // GRID
            drawGrid(100, 800, 1, 0,300);
            fill(color(255,0,0));
            ellipse(0, 0, 20, 20);


            // VECTORS
            fill(color(20,50,219));

            // SHAPES

            // RENDER
            ellipse(camPivot.x, camPivot.y, 20f, 20f);

            ellipse(s.joyArmWithRotation.x, s.joyArmWithRotation.y, 2 * joyBearingRadius, 2 * joyBearingRadius);
            line(0,0, s.joyArmWithRotation.x, s.joyArmWithRotation.y);
            //line(s.joyArmWithRotation.x,s.joyArmWithRotation.y, s.joyContact.x, s.joyContact.y);


            pushMatrix();
                translate(camPivot.x, camPivot.y);
                rotate(s.camRotation);
                translate(-camPivot.x, -camPivot.y);
                shape(_camCurve);
            popMatrix();



            ellipse(s.springAnchorWithRotation.x, s.springAnchorWithRotation.y, 20f, 20f);
            ellipse(s.springAnchorFixed.x, s.springAnchorFixed.y, 20f, 20f);

            ellipse(s.collisionWs.x, s.collisionWs.y, 10,10);

            PVector contactForceDirection = PVector.sub(s.joyArmWithRotation, s.collisionWs);

            stroke(255);
            line(s.joyArmWithRotation.x, s.joyArmWithRotation.y, s.joyArmWithRotation.x+contactForceDirection.x, s.joyArmWithRotation.y+contactForceDirection.y);

            strokeWeight(3);
            stroke(color(200,0,0));
            line(s.springAnchorFixed.x, s.springAnchorFixed.y, s.springAnchorWithRotation.x, s.springAnchorWithRotation.y);

            line(s.collisionWs.x, s.collisionWs.y, s.collisionWs.x +  0.1f * s.contactForceVector.x, s.collisionWs.y + 0.1f * s.contactForceVector.y);


        popMatrix();


        // INFO

        fill(0, 200);
        strokeWeight(1);
        stroke(0);
        rect(10, 10, 400, 300);
        fill(255);
        textFont(font);
        text("joyRotation: " + Utils.degrees(s.joyRotation) + " deg", 20, 40);
        text("camRotation: " + Utils.degrees(s.camRotation) + " deg", 20, 60);
        text("Spring: " + (s.springLength.mag() - s.springInitialLength.mag()) + " units", 20, 80);
        text("Spring Momentum: " + s.springMomentum + " N/unit", 20, 100);
        text("Joy arm momentum: " + s.joyArmMomentum + " N/unit", 20, 120);
        text("SimStep: " + actualSimStep, 20, 140);



        // PLOT
        plot.defaultDraw();


    }

    @Override
    public void keyPressed() {
        if (key == CODED) {
            if (keyCode == LEFT) {
                if(actualSimStep>0) {
                    actualSimStep--;
                }
            } else if (keyCode == RIGHT) {
                if(actualSimStep<19){
                    actualSimStep++;
                }
            }
        }
    }

    private PVector applyMatrix(PVector vector, AffineTransform matrix) {
        double flatMatrix[]  = new double[6];
        matrix.getMatrix(flatMatrix);

        float colX = (float)(flatMatrix[0]*vector.x + flatMatrix[2]*vector.y - flatMatrix[4]);
        float colY = (float)(flatMatrix[1]*vector.x + flatMatrix[3]*vector.y + flatMatrix[5]);

        return new PVector(colX, colY);
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

        camCurvePoints = new PVector[curveSteps+3];

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
        _camCurve.vertex(-200, 100);
        _camCurve.vertex(-200, 375);
        _camCurve.vertex(0, 375);

        camCurvePoints[curveSteps] = new PVector(-200,100);
        camCurvePoints[curveSteps+1] = new PVector(-200,375);
        camCurvePoints[curveSteps+2] = new PVector(0,375);

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

    private void preparePlot() {
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
}
