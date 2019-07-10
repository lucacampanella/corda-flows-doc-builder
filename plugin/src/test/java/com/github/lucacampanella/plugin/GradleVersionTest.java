package com.github.lucacampanella.plugin;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GradleVersionTest {

    @Test
    void testMethods() {
        GradleVersion a = new GradleVersion("1.1");
        GradleVersion b = new GradleVersion("1.1.1");
        assertThat(a.compareTo(b)).isEqualTo(-1); // return -1 (a<b)
        assertThat(a.equals(b)).isEqualTo(false);    // return false

        a = new GradleVersion("2.0");
        b = new GradleVersion("1.9.9");
        assertThat(a.compareTo(b)).isEqualTo(1); // return 1 (a>b)
                assertThat(a.equals(b)).isEqualTo(false);    // return false

        a = new GradleVersion("1.0");
        b = new GradleVersion("1");
        assertThat(a.compareTo(b)).isEqualTo(0); // return 0 (a=b)
        assertThat(a.equals(b)).isEqualTo(true);   // return true

        a = new GradleVersion("1");
        b = null;
        assertThat(a.compareTo(b)).isEqualTo(1); // return 1 (a>b)
        assertThat(a.equals(b)).isEqualTo(false);   // return false

   }
}