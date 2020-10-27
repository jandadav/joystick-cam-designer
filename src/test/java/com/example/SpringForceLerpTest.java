package com.example;

import com.example.utils.Utils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SpringForceLerpTest {
    @Test
    void springForceCalcuatesCorrectly() {
        float f0 = 1f;
        float f8 = 2f;
        float l0 = 1f;
        float l8 = 2f;

        assertThat(Utils.springForceLerp(f0,f8,l0,l8,l0), is(f0));
        assertThat(Utils.springForceLerp(f0,f8,l0,l8,l8), is(f8));
        assertThat(Utils.springForceLerp(f0,f8,l0,l8,l0 + ((l8-l0)/2)), is(f0 + (f8-f0)/2));
    }

}
