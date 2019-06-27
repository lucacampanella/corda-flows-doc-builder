package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.decompiler.CFRDecompiler;
import spoon.decompiler.Decompiler;
import spoon.decompiler.FernflowerDecompiler;

import java.io.File;

public enum DecompilerEnum {
    CFR, FERNFLOWER;

    private static final Logger LOGGER = LoggerFactory.getLogger(DecompilerEnum.class);

    public Decompiler getDecompiler(File decompiledSrc) {
        if(this == FERNFLOWER) {
            return new FernflowerDecompiler(decompiledSrc);
        }
        return new CFRDecompiler(decompiledSrc);
    }

    public static DecompilerEnum getDefault() {
        return CFR;
    }

    public static DecompilerEnum fromStringOrDefault(String value) {

        DecompilerEnum result;
        try {
            result = DecompilerEnum.valueOf(value.toUpperCase());
        } catch (Exception e) {
            result = getDefault();
            LOGGER.error("Could not find decompiler {}, defaulting on {}", value, result);
        }
        return result;
    }
}
