package com.example;

import com.example.utils.Range;
import com.example.utils.Utils;
import grafica.GPlot;
import grafica.GPointsArray;
import lombok.extern.slf4j.Slf4j;
import processing.awt.PGraphicsJava2D;
import processing.core.*;

import java.awt.geom.AffineTransform;

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
            float rotation = lerp(0, 1, mouseX / Float.valueOf(width));
            //float rotation = 0;
            //float camRotation = lerp(0, radians(camLimitAngle), rotation);
            //float camRotation = quadratic(0, radians(camLimitAngle), rotation);
            float contactRotation = lerp(0, radians(contactPointLimitAngle), rotation);
            float joyRotation = lerp(0, radians(joyLimitAngle), rotation);

            // VECTORS
            fill(color(20,50,219));
            PVector joyArm = new PVector(0, joyArmLength);
            PVector joyArmWithRotation = new PVector(joyArm.x, joyArm.y).rotate(joyRotation);
            PVector joyBearing = new PVector(0, joyBearingRadius);
            PVector joyContact = PVector.add(joyArmWithRotation, new PVector(joyBearing.x, joyBearing.y).rotate(contactRotation));


            // SHAPES
            calculateShapes(joyArm, joyBearing);

            // RENDER
            ellipse(camPivot.x, camPivot.y, 20f, 20f);

            ellipse(joyArmWithRotation.x, joyArmWithRotation.y, 2 * joyBearingRadius, 2 * joyBearingRadius);
            line(0,0, joyArmWithRotation.x, joyArmWithRotation.y);
            line(joyArmWithRotation.x,joyArmWithRotation.y, joyContact.x, joyContact.y);

            // COLLISION CIRCLE
            int precision = 360;
            PVector[] circlePoints = new PVector[precision];
            PShape circle = createShape();
            circle.beginShape();
            circle.fill(0);
            circle.strokeWeight(1);
            circle.stroke(255);
            PVector point = new PVector(0,0);
            for (int i=0; i <precision; i++) {
                point = PVector.add(joyArmWithRotation, new PVector(joyBearing.x, joyBearing.y).rotate(i * PI / Float.valueOf(precision/2)));
                circlePoints[i] = point;
            }
            PVector[] circlePointsScreen = pointsToScreenCoords(circlePoints, this);
            for (int i=0; i <precision; i++) {
                circle.vertex(circlePoints[i].x, circlePoints[i].y);
            }

            circle.endShape(CLOSE);
            shape(circle);
            //shape(_joyArmSweep);
            //shape(_contactSweep);

            // SIMULATE CAM CURVE ROTATION
            PVector collision = null;

            pushMatrix();
                float camRotation = 0;
                translate(camPivot.x, camPivot.y);
                rotate(-1f);
                camRotation -=1f;
                translate(-camPivot.x, -camPivot.y);


                while (collision == null) {
                    translate(camPivot.x, camPivot.y);
                    rotate(0.001f);
                    camRotation += 0.001f;
                    translate(-camPivot.x, -camPivot.y);

                    PVector[] camCurveScreen = pointsToScreenCoords(camCurvePoints, this);
                    collision = polyPoly(camCurveScreen, circlePointsScreen);
                }
                shape(_camCurve);
            popMatrix();


            PVector springAnchorFixed = new PVector(-springPivot.x, springPivot.y);
            PVector springInitialLength = PVector.sub(springAnchorFixed, springPivot);
            PVector springAnchorWithRotation = rotateAround(springPivot, camPivot, camRotation);

            ellipse(springAnchorWithRotation.x, springAnchorWithRotation.y, 20f, 20f);
            ellipse(springAnchorFixed.x, springAnchorFixed.y, 20f, 20f);

            PVector collisionWs = applyMatrix(collision, ((PGraphicsJava2D) g).g2.getTransform());
            ellipse(collisionWs.x, collisionWs.y, 10,10);

            PVector contactForceDirection = PVector.sub(joyArmWithRotation, collisionWs);

            stroke(255);
            line(joyArmWithRotation.x, joyArmWithRotation.y, joyArmWithRotation.x+contactForceDirection.x, joyArmWithRotation.y+contactForceDirection.y);

            strokeWeight(3);
            stroke(color(200,0,0));
            line(springAnchorFixed.x, springAnchorFixed.y, springAnchorWithRotation.x, springAnchorWithRotation.y);

            // Calculate

            PVector springLength = PVector.sub(springAnchorWithRotation, springAnchorFixed);

            float springMomentum = moment(springLength.copy().setMag((springLength.mag() - springInitialLength.mag()) * 10), springAnchorWithRotation);

            PVector contactArmToCamPivot = PVector.sub(camPivot, collisionWs);
            float contactForceSize = springMomentum / (sin(PVector.angleBetween(collisionWs, contactArmToCamPivot)) * contactArmToCamPivot.mag());
            PVector contactForceVector = contactForceDirection.copy().setMag(contactForceSize);

            line(collisionWs.x, collisionWs.y, collisionWs.x +  0.1f * contactForceVector.x, collisionWs.y + 0.1f *contactForceVector.y);

            float joyArmMomentum = moment(contactForceVector, joyArmWithRotation);

        popMatrix();


        // DEBUG

        fill(0, 200);
        strokeWeight(1);
        stroke(0);
        rect(10, 10, 400, 300);
        fill(255);
        textFont(font);
        text("Rotation: " + Utils.degrees(rotation) + " deg", 20, 40);
        text("camRotation: " + Utils.degrees(camRotation) + " deg", 20, 60);
        text("Spring: " + (springLength.mag() - springInitialLength.mag()) + " units", 20, 80);
        text("Spring Momentum: " + springMomentum + " N/unit", 20, 100);
        text("Joy arm momentum: " + joyArmMomentum + " N/unit", 20, 120);



        // PLOT
        plot.defaultDraw();


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
}
