package com.example;

import processing.core.PApplet;
import processing.core.PShape;

public class App extends PApplet {

    private PShape star;
    private PShape group;

    @Override
    public void settings() {
        super.settings();
        size(900,900, P2D);
        smooth();
    }

    @Override
    public void setup() {


        group = createShape(GROUP);

        star = createShape();
        star.beginShape();
        // You can set fill and stroke
        star.fill(102);
        star.stroke(255);
        star.strokeWeight(2);
        // Here, we are hardcoding a series of vertices
        star.vertex(0, -50);
        star.vertex(14, -20);
        star.vertex(47, -15);
        star.vertex(23, 7);
        star.vertex(29, 40);
        star.vertex(0, 25);
        star.vertex(-29, 40);
        star.vertex(-23, 7);
        star.vertex(-47, -15);
        star.vertex(-14, -20);
        star.endShape(CLOSE);

        group.addChild(star);
        group.addChild(createShape(ELLIPSE,0, 50, 30, 30));

        PShape pivot = createShape(ELLIPSE, 0, 0, 10, 10);
        pivot.setFill(color(255,0,0));

        group.addChild(pivot);

    }

    @Override
    public void draw() {
        background(128);

        pushMatrix();
        translate(width/4,height/4);
        rotate(mouseX * 0.01f);
        //translate(width/4,height/4);

        shape(group);
        for(PShape child: group.getChildren()) {
            shape(child);
        }
        popMatrix();
        rect(0,0,100,100);
        //group.rotate(0.01f);
        group.translate(0.8f,0.8f);
        
        shape(group);
    }



    /*   @Override
        public void draw() {
            background(255);
            int limit = 500;
            int spacing = 30;


            // Prepare the points for the plot
            int nPoints = 100;
            GPointsArray points = new GPointsArray(nPoints);

            for (int i = 0; i < nPoints; i++) {
                points.add(i, 10*noise(0.1f*i));
            }

            // Create a new plot and set its position on the screen
            GPlot plot = new GPlot(this);
            plot.setPos(25, 25);
            plot.setAllFontProperties("Verdana", 0, 12);
            // or all in one go
            // GPlot plot = new GPlot(this, 25, 25);

            // Set the plot title and the axis labels
            plot.setTitleText("A very simple example");
            plot.getXAxis().setAxisLabelText("x axis");
            plot.getYAxis().setAxisLabelText("y axis");

            // Add the points
            plot.setPoints(points);

            // Draw it!
            plot.defaultDraw();

            tint(255, 127);
            for (int i = 10; i < limit; i+=spacing) {
                for (int j = 10; j < limit; j+=spacing) {
                    shape(i, j);
                }
            }
        }

        public void shape(int x, int y) {
            int size = 5;
            stroke(20,20,20,125);
            noFill();
            strokeWeight(3.0f);
            strokeJoin(ROUND);
            beginShape();
            vertex(x, y);
            vertex(x + size, y + size);
            vertex(x, y + 2*size);
            endShape();
        }
    */
    public static void main(String... args) {
        PApplet.main(App.class);
    }
}
