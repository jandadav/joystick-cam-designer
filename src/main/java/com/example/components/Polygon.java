package com.example.components;

import lombok.Getter;
import lombok.Setter;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

public abstract class Polygon extends PApplet{

    protected static PApplet it;
    @Setter
    protected float scale = 1;

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
        it.scale(scale, -scale);
    };

    public float rescale(float value) {
        return value / scale;
    }
}
