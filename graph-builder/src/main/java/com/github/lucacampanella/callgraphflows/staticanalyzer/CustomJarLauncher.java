package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.SpoonException;
import spoon.decompiler.Decompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class CustomJarLauncher extends Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomJarLauncher.class);

    public static class Builder {
        String decompiledSrcPath = Paths.get(System.getProperty("java.io.tmpdir"), "spoon-tmp", "decompiledSrc").toString();
        DecompilerEnum decompilerEnum = DecompilerEnum.CFR;
        Decompiler decompiler = null;
        List<String> jarPaths;
        boolean decompile = true;

        public Builder(List<String> jarPaths) {
            this.jarPaths = jarPaths;
        }

        public Builder withDecompiledSourcePath(String decompiledSrcPath) {
            this.decompiledSrcPath = decompiledSrcPath;
            decompile = false;
            return this;
        }

        public Builder withDecompilerEnum(DecompilerEnum decompilerEnum) {
            this.decompilerEnum = decompilerEnum;
            decompile = true;
            return this;
        }

        public Builder withDecompiler(Decompiler decompiler) {
            this.decompiler = decompiler;
            decompile = true;
            return this;
        }

        public Builder withDecompile(Boolean decompile) {
            this.decompile = decompile;
            return this;
        }

        public CustomJarLauncher build() {

            CustomJarLauncher customJarLauncher = new CustomJarLauncher();
            LOGGER.trace("Decompiled source path = {}", decompiledSrcPath);
            LOGGER.trace("decompilerEnum = {}", decompilerEnum);
            LOGGER.trace("decompiler = {}", decompiler);

            File decompiledDirectory = new File(decompiledSrcPath);
            if (decompiledDirectory.exists() && !decompiledDirectory.canWrite()) {
                throw new SpoonException("Dir " + decompiledDirectory.getPath() + " already exists and is not deletable.");
            } else {
                if (decompiledDirectory.exists() && this.decompile) {
                    try {
                        FileUtils.deleteDirectory(decompiledDirectory);
                    } catch (IOException e) {
                        throw new SpoonException("Dir " + decompiledDirectory.getPath() + " already exists and is not deletable.");
                    }
                }

                if (!decompiledDirectory.exists()) {
                    decompiledDirectory.mkdirs();
                    this.decompile = true;
                }

                if (decompiler == null) {
                    decompiler = decompilerEnum.getDecompiler(decompiledDirectory);
                }

                jarPaths.forEach(jarPath -> {
                    File jar = new File(jarPath);
                    if (jar.exists() && jar.isFile()) {
                        if (this.decompile || (jar.lastModified() > decompiledDirectory.lastModified())) {
                            this.decompiler.decompile(jar.getAbsolutePath());
                        }
                    } else {
                        throw new SpoonException("Jar " + jar.getPath() + " not found.");
                    }
                });
                customJarLauncher.addInputResource(decompiledDirectory.getAbsolutePath());
            }

            return customJarLauncher;
        }
    }

    private CustomJarLauncher(){
        //use builder
    }
}






