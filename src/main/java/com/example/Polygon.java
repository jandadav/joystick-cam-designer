package com.example;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

public class Polygon extends PApplet{

    private  static PApplet it;

    PShape shape;
    PShape pivot;
    public PVector position = new PVector(0,0);

    public Polygon(PApplet it) {
        this.it = it;
        pivot = it.createShape(ELLIPSE, 0, 0, 20, 20);
        pivot.setFill(color(190, 0, 0, 255));

       shape = it.createShape(RECT,0, 0, 100, 50);
    }

    public void display() {
        it.translate(position.x, position.y);
        it.rotate(PI/4);
        it.shape(pivot);
        it.shape(shape);
    };

}
