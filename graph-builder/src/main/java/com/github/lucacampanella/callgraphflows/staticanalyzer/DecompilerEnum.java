package com.github.lucacampanella.callgraphflows.staticanalyzer;

import spoon.decompiler.CFRDecompiler;
import spoon.decompiler.Decompiler;
import spoon.decompiler.FernflowerDecompiler;

import java.io.File;

public enum DecompilerEnum {
    CFR, FERNFLOWER;

    public Decompiler getDecompiler(File decompiledSrc) {
        if(this == FERNFLOWER) {
            return new FernflowerDecompiler(decompiledSrc);
        }
        return new CFRDecompiler(decompiledSrc);
    }
}
