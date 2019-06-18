package com.github.lucacampanella.callgraphflows.graphics.svg.utils;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GUtilsTest {

    @Test
    void fromColorToHex() {
        assertThat(GUtils.fromColorToHex(Color.WHITE)).isEqualTo("#FFFFFF");
    }
}