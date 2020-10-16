package com.example;

public class Range<T extends Number>  {
    private T start;
    private T stop;
    private int steps;
    private float value;


    public int getSteps() {
        return steps;
    }

    public Range(T start, T stop, int steps) {
        this.start = start;
        this.stop = stop;
        this.steps = steps+1;
        this.value = start.floatValue();
    }

    public void increment(){
        if (steps-- < 0) {
            throw new IndexOutOfBoundsException();
        }

        value = value + (stop.floatValue() - start.floatValue()) / steps;
    }

    public float getValue() {
        return value;
    }

    public float getStep() {
        float v = (stop.floatValue() - start.floatValue()) / steps;
        return v;
    }


}
