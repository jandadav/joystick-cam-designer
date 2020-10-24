package com.example;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import processing.core.PVector;

import java.util.stream.Stream;

import static com.example.utils.Utils.atan2;
import static com.example.utils.Utils.moment;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static processing.core.PConstants.*;

public class VectorOperationsTest {

    @ParameterizedTest
    @MethodSource("generateAngleVectors")
    void angleBetweenHasExpectedValue(PVector v1, PVector v2, float expectedAngle) {
        assertThat((double)atan2(v1, v2), closeTo(expectedAngle, 0.00001));
    }

    static Stream<Arguments> generateAngleVectors() {
        return Stream.of(
                Arguments.arguments(new PVector(1,0), new PVector(1,0), 0),
                Arguments.arguments(new PVector(1,0), new PVector(1,1), QUARTER_PI),
                Arguments.arguments(new PVector(1,0), new PVector(0,1), HALF_PI),
                Arguments.arguments(new PVector(1,0), new PVector(-1,0), PI),
                Arguments.arguments(new PVector(1,0), new PVector(1,-1), PI + HALF_PI + QUARTER_PI)
        );
    }

    @ParameterizedTest
    @MethodSource("generateMomentVectors")
    void momentHasExpectedValue(PVector force, PVector arm, float expectedValue) {
        assertThat((double) moment(force, arm), closeTo(expectedValue, 0.00001f));
    }

    static Stream<Arguments> generateMomentVectors() {
        return Stream.of(
                Arguments.arguments(new PVector(1,0), new PVector(1,0), 0),
                Arguments.arguments(new PVector(1,0), new PVector(1,1), -1f),
                Arguments.arguments(new PVector(1,0), new PVector(0,1), -1f),
                Arguments.arguments(new PVector(1,0), new PVector(-1,0), 0f),
                Arguments.arguments(new PVector(1,0), new PVector(0,-1), 1f)
        );
    }
}
