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


    public static float dpi = 10000;
    float joyArmLength = 0.03f;
    float joyBearingRadius = 0.005f;
    float contactPointLimitAngle = 80f;
    float camLimitAngle = 30f;
    float joyLimitAngle = 20f;

    private PFont font;
    private final PVector camPivot = new PVector(0.02f,0.02f);
    private final PVector springPivot = new PVector(0.025f, 0.04f);
    private PVector springL0;
    private final int curveSteps = 80;
    private PShape _joyArmSweep;
    private PShape _contactSweep;
    private PShape _camCurve;
    private GPlot plot1;
    private GPlot plot2;
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
        font = createFont("Consolas", 14, true);

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
            springL0 = PVector.sub(s.springAnchorFixed, springPivot);
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

        // PLOTS
        plot1 = new GPlot(this);
        plot2 = new GPlot(this);
        int nPoints = 20;
        GPointsArray points = new GPointsArray(nPoints);
        for (int i = 0; i < nPoints; i++) {
            points.add(i, simData.get(i).joyArmMomentum );
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

        SimStep s = simData.get(actualSimStep);

        background(100);
        pushMatrix();
            translate(width/2, height-100);
            scale(1f, -1f);

            // GRID
            drawGrid(100, 800, 1, 0,300);
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



            ellipse(pixels(s.springAnchorWithRotation.x), pixels(s.springAnchorWithRotation.y), 20f, 20f);
            ellipse(pixels(s.springAnchorFixed.x), pixels(s.springAnchorFixed.y), 20f, 20f);

            ellipse(pixels(s.collisionWs.x), pixels(s.collisionWs.y), 10,10);

            PVector contactForceDirection = PVector.sub(s.joyArmWithRotation, s.collisionWs);

            stroke(255);
            line(pixels(s.joyArmWithRotation.x), pixels(s.joyArmWithRotation.y), pixels(s.joyArmWithRotation.x+contactForceDirection.x), pixels(s.joyArmWithRotation.y+contactForceDirection.y));

            strokeWeight(3);
            stroke(color(200,0,0));
            line(pixels(s.springAnchorFixed.x), pixels(s.springAnchorFixed.y), pixels( s.springAnchorWithRotation.x), pixels(s.springAnchorWithRotation.y));

            line(pixels(s.collisionWs.x), pixels(s.collisionWs.y), pixels(s.collisionWs.x +  0.1f * s.contactForceVector.x), pixels(s.collisionWs.y + 0.1f * s.contactForceVector.y));


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
        text("Spring deltaL: " + String.format("%f", (s.springLength.mag() - s.springInitialLength.mag())) + " units", 20, offset+=spacing);



        // PLOT
        plot1.beginDraw();
        plot1.drawBackground();
        plot1.drawBox();
        plot1.drawXAxis();
        plot1.drawYAxis();
        plot1.drawTitle();
        plot1.drawPoints();
        plot1.drawLines();
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

            _joyArmSweep.vertex(pixels(joyArmSweep.x), pixels(joyArmSweep.y));

            PVector contact = PVector.add(joyArmSweep, joyBearingSweep);
            _contactSweep.vertex(pixels(contact.x), pixels(contact.y));

            PVector camCurve = rotateAround(contact, camPivot, quadratic(0, radians(camLimitAngle), camRotationRange.getIterationNormalized()));
            _camCurve.vertex(pixels(camCurve.x), pixels(camCurve.y));

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

        camCurvePoints[curveSteps] = new PVector(-200/dpi, 100/dpi);
        camCurvePoints[curveSteps+1] = new PVector(-200/dpi, 375/dpi);
        camCurvePoints[curveSteps+2] = new PVector(0, 375/dpi);

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
