package com.example;

import com.example.components.CoordsPlot;
import com.example.components.Polygon;
import processing.core.PApplet;

public class App extends PApplet {

    Polygon coords;


    @Override
    public void settings() {
        super.settings();
        size(900,900, P2D);
        smooth();
        //noLoop();

    }

    @Override
    public void setup() {
        coords = new CoordsPlot(this);
        coords.position.x = width / 2;
        coords.position.y = height / 2;
    }

    @Override
    public void draw() {
        background(128);

        coords.display();
    }

    public static void main(String... args) {
        PApplet.main(App.class);
    }
}
