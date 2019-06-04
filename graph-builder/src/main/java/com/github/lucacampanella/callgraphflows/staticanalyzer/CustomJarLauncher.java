package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.apache.commons.io.FileUtils;
import spoon.Launcher;
import spoon.SpoonException;
import spoon.decompiler.CFRDecompiler;
import spoon.decompiler.Decompiler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class CustomJarLauncher extends Launcher {
    File decompiledRoot;
    File decompiledSrc;
    Decompiler decompiler;
    boolean decompile;

    public CustomJarLauncher(String jarPath) {
        this(Arrays.asList(jarPath), (String)null, null);
    }

    public CustomJarLauncher(String jarPath, String decompiledSrcPath) {
        this(Arrays.asList(jarPath), decompiledSrcPath, null);
    }

    public CustomJarLauncher(List<String> jarPaths) {
        this(jarPaths, (String)null, null);
    }

    public CustomJarLauncher(List<String> jarPaths, String decompiledSrcPath) {
        this(jarPaths, decompiledSrcPath, null);
    }

    public CustomJarLauncher(List<String> jarPaths, String decompiledSrcPath, Decompiler decompiler) {
        this.decompile = false;
        this.decompiler = decompiler;
        if (decompiledSrcPath == null) {
            decompiledSrcPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "spoon-tmp";
            this.decompile = true;
        }

        this.decompiledRoot = new File(decompiledSrcPath);
        if (this.decompiledRoot.exists() && !this.decompiledRoot.canWrite()) {
            throw new SpoonException("Dir " + this.decompiledRoot.getPath() + " already exists and is not deletable.");
        } else {
            if (this.decompiledRoot.exists() && this.decompile) {
                try {
                    FileUtils.deleteDirectory(decompiledRoot);
                } catch (IOException e) {
                    throw new SpoonException("Dir " + decompiledRoot.getPath() + " already exists and is not deletable.");
                }
            }

            if (!this.decompiledRoot.exists()) {
                this.decompiledRoot.mkdirs();
                this.decompile = true;
            }

            this.decompiledSrc = new File(this.decompiledRoot, "src/main/java");
            if (!this.decompiledSrc.exists()) {
                this.decompiledSrc.mkdirs();
                this.decompile = true;
            }

            if (decompiler == null) {
                this.decompiler = this.getDefaultDecompiler();
            }

            jarPaths.forEach(jarPath -> {
                File jar = new File(jarPath);
                if (jar.exists() && jar.isFile()) {
                    if (this.decompile || (jar.lastModified() > this.decompiledSrc.lastModified())) {
                        this.decompiler.decompile(jar.getAbsolutePath());
                    }
                } else {
                    throw new SpoonException("Jar " + jar.getPath() + " not found.");
                }
            });
            this.addInputResource(this.decompiledSrc.getAbsolutePath());
        }
    }


    protected Decompiler getDefaultDecompiler() {
        return new CFRDecompiler(this.decompiledSrc);
    }
}






