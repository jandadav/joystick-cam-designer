package com.example.utils;

import lombok.Getter;

public class Range<T extends Number>  {
    private T start;
    private T stop;
    @Getter
    private int iteration;
    private int totalSteps;
    @Getter
    private float value;

    public Range(T start, T stop, int steps) {
        this.start = start;
        this.stop = stop;
        this.totalSteps = steps-1;
        this.iteration = 0;
        this.value = start.floatValue();
    }

    public boolean hasNext() {
        return iteration <= totalSteps;
    }

    public void next(){
        if (iteration++ > totalSteps) {
            throw new IndexOutOfBoundsException();
        }

        value = value + (stop.floatValue() - start.floatValue()) / totalSteps;
    }


    public float getIncrement() {
        return (stop.floatValue() - start.floatValue()) / totalSteps;
    }

}
