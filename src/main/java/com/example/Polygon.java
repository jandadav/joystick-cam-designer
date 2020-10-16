package com.example;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

public abstract class Polygon extends PApplet{

    protected static PApplet it;

    PShape pivot;
    public PVector position = new PVector(0,0);

    public Polygon(PApplet it) {
        this.it = it;
        pivot = it.createShape(ELLIPSE, 0, 0, 20, 20);
        pivot.setFill(color(190, 0, 0, 255));

    }

    public void display() {
        it.translate(position.x, position.y);
        it.rotate(0);
        it.scale(0.8f, -0.8f);
        it.shape(pivot);
    };

}
