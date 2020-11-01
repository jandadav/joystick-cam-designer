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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.utils.Utils.*;

@Slf4j
public class App extends PApplet {


    public static float dpi = 10000;
    float joyArmLength = 0.04f;
    float joyBearingRadius = 0.005f;
    float contactPointLimitAngle = 160f;
    float camLimitAngle = 20f;
    float joyLimitAngle = 20f;
    float joyLimitAngleExtension = 3f;

    private final PVector joyArm = new PVector(0, joyArmLength);
    private final PVector joyBearing = new PVector(0, joyBearingRadius);
    private final PVector camCurveApex = PVector.add(joyArm, joyBearing);

    private final PVector camPivot = new PVector(0.03f,0.04f);
    private final PVector springMovingEnd = new PVector(-0.05f, 0.05f);
    private final PVector springFixedEnd = new PVector(-0.05f, -0.01f);
    private PVector springL0;

    private PFont font;
    private final int curveSteps = 80;
    private PShape _joyArmSweep;
    private PShape _contactSweep;
    private PShape _camCurve;
    private GPlot plot1;
    private GPlot plot2;
    private PVector[] camCurvePoints;

    private List<SimStep> simData = new ArrayList<>();
    private int actualSimStep = 0;
    private int simSteps = 61;
    private boolean isSimulateOn = true;


    @Override
    public void settings() {
        super.settings();
        size(1200,900);
        smooth();
    }


    @Override
    public void setup() {

        // GENERAL SETUP
        ellipseMode(CENTER);
        font = createFont("Consolas", 14, true);

        // CREATE SHAPES

        //calculateShapes(joyArm, joyBearing);
        calculateShapes2();

        // SIM

        Range<Float> simRange = new Range<Float>(0f, 1f, simSteps);

        while(simRange.hasNext()) {

            // DATA
            SimStep s = new SimStep();
            simData.add(s);

            // INPUT
            s.joyRotation = lerp(-radians(joyLimitAngle), radians(joyLimitAngle), simRange.getValue());


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

            if (isSimulateOn) {
                pushMatrix();
                s.camRotation = 0;
                translate(camPivot.x, camPivot.y);
                rotate(-1f);
                s.camRotation -=1f;
                translate(-camPivot.x, -camPivot.y);

                // TODO 0.00001f increment gives great results but veeeeery slow.
                //float[] steps = {0.001f,0.0001f,0.00001f};
                float[] steps = {0.001f, 0.0001f};
                int iteration = 0;
                while (s.collision == null) {
                    translate(camPivot.x, camPivot.y);
                    rotate(steps[iteration]);
                    s.camRotation += steps[iteration];
                    translate(-camPivot.x, -camPivot.y);

                    PVector[] camCurveScreen = pointsToScreenCoords(camCurvePoints, this);
                    PVector collision = polyPoly(camCurveScreen, circlePointsScreen);
                    if (collision != null) {
                        if(iteration == steps.length - 1) {
                            s.collision = collision;
                        } else {
                            rotate(-2 * steps[iteration]);
                            s.camRotation -= 2 * steps[iteration];
                            iteration++;
                        }
                    }
                }
                popMatrix();
            } else {
                s.camRotation = 0;
                s.collision = camCurveApex;
            }
            // CALCULATE

            s.springAnchorFixed = new PVector(springFixedEnd.x, springFixedEnd.y);
            springL0 = PVector.sub(s.springAnchorFixed, springMovingEnd);
            s.springInitialLength = PVector.sub(s.springAnchorFixed, springMovingEnd);
            s.springAnchorWithRotation = rotateAround(springMovingEnd, camPivot, s.camRotation);
            s.collisionWs = applyMatrix(s.collision, ((PGraphicsJava2D) g).g2.getTransform());
            s.contactForceDirection = PVector.sub(s.joyArmWithRotation, s.collisionWs);
            s.springLength = PVector.sub(s.springAnchorFixed, s.springAnchorWithRotation);

            // pull spring
            // TZ 1000x0110x0390 Spring
            /*float f0 = 2.82f;
            float f8 = 29.5f;
            float l0 = 0.039f;
            float l8 = 0.092f;
            s.springForce = s.springLength.copy().setMag(springForceLerp(f0, f8, l0, l8, s.springLength.mag()));*/

            // push spring
            // TZ 1250x093x0480
            float C = 3.096f;
            float precompression = 0f;
            float preload  = precompression * C;
            float maxDelta = 0.048f - precompression - 0.0268f;

            float springDelta = s.springLength.mag() - s.springInitialLength.mag();
            if (springDelta>=maxDelta) {
                s.messages.add("ERROR: Spring compression exceeded");
                log.error("Spring compression exceeded");
            }
            s.springForce = s.springLength.copy().setMag(preload + springDelta * C * 1000);

            s.springMomentum = moment(s.springForce, s.springAnchorWithRotation);
            s.contactArmToCamPivot = PVector.sub(camPivot, s.collisionWs);
            s.contactForceSize = s.springMomentum / (sin(PVector.angleBetween(s.collisionWs, s.contactArmToCamPivot)) * s.contactArmToCamPivot.mag());
            s.contactForceVector = s.contactForceDirection.copy().setMag(s.contactForceSize);
            s.joyArmMomentum = moment(s.contactForceVector, s.joyArmWithRotation);

            simRange.next();
        }

        // PLOTS
        plot1 = new GPlot(this);
        plot2 = new GPlot(this);
        int nPoints = simSteps;
        GPointsArray points = new GPointsArray(nPoints);
        for (int i = 0; i < nPoints; i++) {
            points.add(i, simData.get(i).joyArmMomentum );
            //points.add(i, degrees(simData.get(nPoints-1-i).camRotation) ); // For checking symmetry of cam rotation, reverse cam rotation points
        }

        GPointsArray points2 = new GPointsArray(nPoints);
        for (int i = 0; i < nPoints; i++) {
            points2.add(i, degrees(simData.get(i).camRotation) );
        }

        plot1.setPos(420, 10);
        plot1.setAllFontProperties("Consolas", 0, 12);
        plot1.setTitleText("Contact point rotation curve");
        plot1.getXAxis().setAxisLabelText("Joy arm angle");
        plot1.getYAxis().setAxisLabelText("Joy arm moment");
        plot1.getMainLayer().setLineColor(color(0,128,0));
        plot1.getMainLayer().setPointColor(color(0,128,0));
        plot1.setPoints(points);

        plot2.setPos(420, 10);
        plot2.setAllFontProperties("Consolas", 0, 12);
        plot2.setTitleText("Contact point rotation curve");
        plot2.getXAxis().setAxisLabelText("Joy arm angle");
        plot2.getRightAxis().setAxisLabelText("Cam rotation");
        plot2.getRightAxis().setDrawTickLabels(true);
        plot2.setPoints(points2);

    }

    @Override
    public void draw() {

        if (!isSimulateOn) {
            calculateShapes2();
        }

        SimStep s = simData.get(actualSimStep);

        background(100);
        pushMatrix();
            translate(width/2, height-100);
            scale(1f, -1f);

            // GRID
            drawGrid(100, 1200, 1, 0,300);
            fill(color(255,0,0));
            ellipse(0, 0, 20, 20);


            // VECTORS
            fill(color(20,50,219));

            // SHAPES

            // RENDER
            ellipse(pixels(camPivot.x), pixels(camPivot.y), 20f, 20f);

            ellipse(pixels(s.joyArmWithRotation.x), pixels(s.joyArmWithRotation.y), pixels(2 * joyBearingRadius), pixels(2 * joyBearingRadius));
            line(0,0, pixels(s.joyArmWithRotation.x), pixels(s.joyArmWithRotation.y));
            //line(s.joyArmWithRotation.x,s.joyArmWithRotation.y, s.joyContact.x, s.joyContact.y);


            pushMatrix();
                translate(pixels(camPivot.x), pixels(camPivot.y));
                rotate(s.camRotation);
                translate(pixels(-camPivot.x), pixels(-camPivot.y));
                shape(_camCurve);
            popMatrix();

            /*shape(_joyArmSweep);
            shape(_contactSweep);*/

            ellipse(pixels(s.springAnchorWithRotation.x), pixels(s.springAnchorWithRotation.y), 20f, 20f);
            ellipse(pixels(s.springAnchorFixed.x), pixels(s.springAnchorFixed.y), 20f, 20f);

            ellipse(pixels(s.collisionWs.x), pixels(s.collisionWs.y), 10,10);

            PVector contactForceDirection = PVector.sub(s.joyArmWithRotation, s.collisionWs);

            stroke(255);
            line(pixels(s.joyArmWithRotation.x), pixels(s.joyArmWithRotation.y), pixels(s.joyArmWithRotation.x+contactForceDirection.x), pixels(s.joyArmWithRotation.y+contactForceDirection.y));

            strokeWeight(3);
            stroke(color(200,0,0));
            line(pixels(s.springAnchorFixed.x), pixels(s.springAnchorFixed.y), pixels( s.springAnchorWithRotation.x), pixels(s.springAnchorWithRotation.y));

            // FORCES
            strokeWeight(6);
            stroke(color(255, 111, 0));
            float forceDrawScale = 0.001f;
            line(pixels(s.springAnchorWithRotation.x), pixels(s.springAnchorWithRotation.y), pixels(s.springAnchorWithRotation.x +  forceDrawScale * s.springForce.x), pixels(s.springAnchorWithRotation.y + forceDrawScale * s.springForce.y));
            line(pixels(s.collisionWs.x), pixels(s.collisionWs.y), pixels(s.collisionWs.x +  forceDrawScale * s.contactForceVector.x), pixels(s.collisionWs.y + forceDrawScale * s.contactForceVector.y));


        popMatrix();


        // INFO

        fill(0, 200);
        strokeWeight(1);
        stroke(0);
        rect(10, 10, 400, 300);
        fill(255);
        textFont(font);

        int offset = 25;
        int spacing = 15;
        text("joyRotation: " + String.format("%f",Utils.degrees(s.joyRotation)) + " deg", 20, offset);
        text("camRotation: " + String.format("%f", Utils.degrees(s.camRotation)) + " deg", 20, offset+=spacing);
        text("Spring Momentum: " + String.format("%f",s.springMomentum) + " N/m", 20, offset+=spacing);
        text("Joy arm momentum: " + String.format("%f", s.joyArmMomentum) + " N/m", 20, offset+=spacing);
        text("SimStep: " + actualSimStep, 20, offset+=spacing);
        text("Spring L0: " + String.format("%f",springL0.mag()), 20, offset+=spacing);
        text("Spring Lmax: " + String.format("%f", simData.get(simData.size()-1).springLength.mag()) , 20, offset+=spacing);
        text("Spring deltaL max: " + String.format("%f", (simData.get(simData.size()-1).springLength.mag() - springL0.mag())), 20, offset+=spacing);
        text("Spring deltaL: " + String.format("%f", (s.springLength.mag() - s.springInitialLength.mag())) + " m", 15, offset+=spacing);
        text("Spring force: " + String.format("%f", s.springForce.mag()) + " N", 20, offset+=spacing);

        offset+=spacing;
        if(!s.messages.isEmpty()) {
            fill(color(230,0,0));
            text(s.messages.stream().collect(Collectors.joining("; ")), 20, offset+=spacing);
        }





        // PLOT
        plot1.beginDraw();
        plot1.drawBackground();
        plot1.drawBox();
        plot1.drawXAxis();
        plot1.drawYAxis();
        plot1.drawTitle();
        plot1.drawPoints();
        plot1.drawLines();
        plot1.drawHorizontalLine(0f);
        plot1.endDraw();

        plot2.beginDraw();
        plot2.drawRightAxis();
        plot2.drawPoints();
        plot2.drawLines();
        plot2.drawVerticalLine(actualSimStep);
        plot2.endDraw();

    }

    @Override
    public void keyPressed() {
        if (key == CODED) {
            if (keyCode == RIGHT) {
                if(actualSimStep>0) {
                    actualSimStep--;
                }
            } else if (keyCode == LEFT) {
                if(actualSimStep<simSteps-1){
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
        _joyArmSweep.stroke(color(255, 255, 0));
        _joyArmSweep.strokeWeight(3);
        _joyArmSweep.fill(0, 1);

        _contactSweep.beginShape();
        _contactSweep.stroke(color(128, 90, 200));
        _contactSweep.strokeWeight(3);
        _contactSweep.fill(0, 1);

        _camCurve.beginShape();
        _camCurve.stroke(color(30, 220, 20));
        _camCurve.strokeWeight(3);
        _camCurve.fill(0, 128);

        Range<Float> joyRange = new Range<>(-radians(joyLimitAngle + joyLimitAngleExtension) , radians(joyLimitAngle + joyLimitAngleExtension), curveSteps);
        Range<Float> contactRange = new Range<>(0.0f, 2 * radians(contactPointLimitAngle), curveSteps);
        Range<Float> camRotationRange = new Range<>(radians(camLimitAngle), radians(camLimitAngle), curveSteps);

        PVector joyArmSweep = new PVector(joyArm.x, joyArm.y).rotate(-radians(joyLimitAngle + joyLimitAngleExtension));
        PVector joyBearingSweep = new PVector(joyBearing.x, joyBearing.y).rotate(-radians(contactPointLimitAngle));

        camCurvePoints = new PVector[curveSteps+3];

        while (joyRange.hasNext()) {

            _joyArmSweep.vertex(pixels(joyArmSweep.x), pixels(joyArmSweep.y));

            PVector contact = PVector.add(joyArmSweep, joyBearingSweep);
            _contactSweep.vertex(pixels(contact.x), pixels(contact.y));

            //PVector camCurve = rotateAround(contact, camCurveApex, quadratic(0, radians(camLimitAngle), camRotationRange.getIterationNormalized(), 1.7f));
            PVector camCurve = rotateAround(contact, camCurveApex, 0);
            _camCurve.vertex(pixels(camCurve.x), pixels(camCurve.y));

            camCurvePoints[joyRange.getIteration()] = camCurve;

            joyArmSweep.rotate(joyRange.getIncrement());
            joyBearingSweep.rotate(contactRange.getIncrement());

            joyRange.next();
            contactRange.next();
            camRotationRange.next();
        }
        float offset = 0.05f;
        _camCurve.vertex(pixels(camCurvePoints[curveSteps-1].x-offset), pixels(camCurvePoints[curveSteps-1].y) );
        _camCurve.vertex(pixels(camCurvePoints[curveSteps-1].x-offset), pixels(camCurvePoints[0].y+offset));
        _camCurve.vertex(pixels(camCurvePoints[0].x), pixels(camCurvePoints[0].y+offset));

        camCurvePoints[curveSteps] = new PVector(camCurvePoints[curveSteps-1].x-offset, camCurvePoints[curveSteps-1].y);
        camCurvePoints[curveSteps+1] = new PVector(camCurvePoints[curveSteps-1].x-offset, camCurvePoints[0].y+offset);
        camCurvePoints[curveSteps+2] = new PVector(camCurvePoints[0].x, camCurvePoints[0].y+offset);

        _joyArmSweep.endShape();
        _contactSweep.endShape();
        _camCurve.endShape();
    }

    private void calculateShapes2() {

        int steps = 32;

        // walk left
        float incrementSize = 0.00074f;
        List<PVector> camPointsLeft = new ArrayList<>();
        Range<Float> generationRange = new Range<>(0f , 1f, steps);
        PVector lastGeneratedPoint = camCurveApex.copy();
        PVector lastIncrement = new PVector(-incrementSize,0f);

        while (generationRange.hasNext()) {

            // TODO rotating after first step creates a little flat space in the center.
            //  If both pieces of curve do not rotate the same way, it creates noticeable asymmetry
            if(generationRange.getIterationNormalized()<0.1f) {
                lastIncrement.rotate(.09f);
            } else if (generationRange.getIterationNormalized()<0.3f) {
                lastIncrement.rotate(.050f);
            } else {
                lastIncrement.rotate(.028f);
            }

            if(generationRange.getIterationNormalized()>0.9f) {
                lastIncrement.setMag(lastIncrement.mag()+0.0005f);
            }

            PVector newPosition = PVector.add(lastGeneratedPoint, lastIncrement);
            camPointsLeft.add(newPosition);
            lastGeneratedPoint = newPosition;


            generationRange.next();
        }

        steps = 30;
        // walk right
        incrementSize = 0.00074f;
        List<PVector> camPointsRight = new ArrayList<>();
        generationRange = new Range<>(0f , 1f, steps);
        lastGeneratedPoint = camCurveApex.copy();
        lastIncrement = new PVector(incrementSize,0f);
        camPointsRight.add(camCurveApex);

        while (generationRange.hasNext()) {

            PVector newPosition = PVector.add(lastGeneratedPoint, lastIncrement);
            camPointsRight.add(newPosition);
            lastGeneratedPoint = newPosition;

            if(generationRange.getIterationNormalized()<0.15f) {
                lastIncrement.rotate(-.07f);
            } else if (generationRange.getIterationNormalized()<0.4f) {
                lastIncrement.rotate(-.005f);
            } else {
                lastIncrement.rotate(.0020f);
            }

            generationRange.next();
        }

        float offset = 0.05f;
        List<PVector> camPointsClosing = new ArrayList<>();
        camPointsClosing.add(new PVector(camPointsLeft.get(camPointsLeft.size()-1).x, camPointsLeft.get(camPointsLeft.size()-1).y + offset));
        camPointsClosing.add(new PVector(camPointsRight.get(camPointsRight.size()-1).x, camPointsRight.get(camPointsRight.size()-1).y + offset));

        _camCurve = createShape();
        _camCurve.beginShape();
        _camCurve.stroke(color(30, 220, 20));
        _camCurve.strokeWeight(3);
        _camCurve.fill(0, 128);

        Collections.reverse(camPointsRight);

        List<PVector> curvePoints = new ArrayList<>();
        curvePoints.addAll(camPointsRight);
        curvePoints.addAll(camPointsLeft);
        curvePoints.addAll(camPointsClosing);

        curvePoints.forEach(v -> _camCurve.vertex(pixels(v.x), pixels(v.y)));

        _camCurve.endShape();

        camCurvePoints = curvePoints.toArray(new PVector[0]);

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
