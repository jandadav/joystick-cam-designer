package com.example.utils;

import com.example.App;
import processing.core.PApplet;
import processing.core.PVector;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.TWO_PI;

public class Utils {

    public static PVector rotateAround(PVector position, PVector pivot, float angle) {
        PVector arm = PVector.sub(position, pivot);
        arm.rotate(angle);
        return PVector.add(pivot, arm);
    }

    public static float radians(float degrees) {
        return degrees * PI / 180;
    }
    public static float degrees(float radians) {
        return radians / PI * 180;
    }

    public static float quadratic(float min, float max, float i, float power) {
        return (float) (min + pow(i * sqrt(max-min), power));
    }

    public static PVector[] pointsToScreenCoords(PVector[] points, PApplet applet) {
        PVector[] screenPoints = new PVector[points.length];   // create output array
        for (int i=0; i<points.length; i++) {                  // go through all the points
            float x = applet.screenX(points[i].x, points[i].y);         // get the screen x coordinate
            float y = applet.screenY(points[i].x, points[i].y);
            screenPoints[i] = new PVector(x, y);
        }
        return screenPoints;
    }


    // POLYGON/POLYGON
    public static PVector polyPoly(PVector[] p1, PVector[] p2) {

        // go through each of the vertices, plus the next vertex in the list
        int next = 0;
        for (int current=0; current<p1.length; current++) {

            // get next vertex in list
            // if we’ve hit the end, wrap around to 0
            next = current+1;
            if (next == p1.length) next = 0;

            // get the PVectors at our current position
            // this makes our if statement a little cleaner
            PVector vc = p1[current];    // c for “current”
            PVector vn = p1[next];       // n for “next”

            // now we can use these two points (a line) to compare to the
            // other polygon’s vertices using polyLine()
            PVector collision = polyLine(p2, vc.x,vc.y,vn.x,vn.y);
            if (collision!= null) return collision;

            // optional: check if the 2nd polygon is INSIDE the first
//            collision = polyPoint(p1, p2[0].x, p2[0].y);
//            if (collision) return true;
        }

        return null;
    }


    // POLYGON/LINE
    public static PVector polyLine(PVector[] vertices, float x1, float y1, float x2, float y2) {

        // go through each of the vertices, plus the next vertex in the list
        int next = 0;
        for (int current=0; current<vertices.length; current++) {

            // get next vertex in list
            // if we’ve hit the end, wrap around to 0
            next = current+1;
            if (next == vertices.length) next = 0;

            // get the PVectors at our current position
            // extract X/Y coordinates from each
            float x3 = vertices[current].x;
            float y3 = vertices[current].y;
            float x4 = vertices[next].x;
            float y4 = vertices[next].y;

            // do a Line/Line comparison
            // if true, return ‘true’ immediately and stop testing (faster)
            PVector collision = lineLine(x1, y1, x2, y2, x3, y3, x4, y4);
            if (collision!= null) {
                return collision;
            }
        }

        // never got a hit
        return null;
    }


    // LINE/LINE
    public static PVector lineLine(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {

        // calculate the direction of the lines
        float uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
        float uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));

        // if uA and uB are between 0-1, lines are colliding
        if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
            float intersectionX = x1 + (uA * (x2-x1));
            float intersectionY = y1 + (uA * (y2-y1));
            return new PVector(intersectionX, intersectionY);
        }
        return null;
    }


    // POLYGON/POINT
    // used only to check if the second polygon is INSIDE the first
    public static boolean polyPoint(PVector[] vertices, float px, float py) {
        boolean collision = false;

        // go through each of the vertices, plus the next vertex in the list
        int next = 0;
        for (int current=0; current<vertices.length; current++) {

            // get next vertex in list
            // if we’ve hit the end, wrap around to 0
            next = current+1;
            if (next == vertices.length) next = 0;

            // get the PVectors at our current position
            // this makes our if statement a little cleaner
            PVector vc = vertices[current];    // c for “current”
            PVector vn = vertices[next];       // n for “next”

            // compare position, flip ‘collision’ variable back and forth
            if ( ((vc.y > py && vn.y < py) || (vc.y < py && vn.y > py)) &&
                    (px < (vn.x-vc.x) * (py-vc.y) / (vn.y-vc.y) + vc.x) ) {
                collision = !collision;
            }
        }
        return collision;
    }

    public static float moment(PVector force, PVector arm) {
        return arm.mag() * force.mag() * PApplet.sin(atan2(arm, force));
    }


    /**
     * Calculates angle between vec1 and vec2, in anti-clockwise direction
     * The range is 0-2 PI
     * The order matters
     *
     * @param v1
     * @param v2
     * @return
     */
    public static float atan2(PVector v1, PVector v2) {
        float a = PApplet.atan2(v2.y, v2.x) - PApplet.atan2(v1.y, v1.x);
        if (a < 0) a += TWO_PI;
        return a;
    }

    public static float pixels(float metricValue) {
        return metricValue * App.dpi;
    }

    public static float springForceLerp(float f0, float f8, float l0, float l8, float springLength) {
        return f0 + (((f8-f0) / (l8-l0)) * (springLength - l0));
    }
}
