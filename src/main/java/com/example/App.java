package com.example;

import processing.core.PApplet;
import processing.core.PShape;

public class App extends PApplet {

    Polygon polygon;

    @Override
    public void settings() {
        super.settings();
        size(900,900, P2D);
        smooth();

    }

    @Override
    public void setup() {
        polygon = new Polygon(this);
        polygon.position.x = width / 2;
        polygon.position.y = height / 2;

    }

    @Override
    public void draw() {
        background(128);

        polygon.display();
    }

    public static void main(String... args) {
        PApplet.main(App.class);
    }
}
