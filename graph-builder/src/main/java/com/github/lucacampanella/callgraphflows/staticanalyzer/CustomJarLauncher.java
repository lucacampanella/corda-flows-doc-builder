package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.SpoonException;
import spoon.decompiler.CFRDecompiler;
import spoon.decompiler.Decompiler;
import spoon.decompiler.FernflowerDecompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
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

//    public CustomJarLauncher(List<String> jarPaths) {
//        this(jarPaths, (String)null, null);
//    }
//
//    public CustomJarLauncher(List<String> jarPaths, String decompiledSrcPath, Decompiler decompiler) {
//        this.decompile = false;
//        this.decompiler = decompiler;
//        if (decompiledSrcPath == null) {
//            decompiledSrcPath = defaultDecompiledSourcePath;
//            this.decompile = true;
//        }
//
//        this.decompiledRoot = new File(decompiledSrcPath);
//        if (this.decompiledRoot.exists() && !this.decompiledRoot.canWrite()) {
//            throw new SpoonException("Dir " + this.decompiledRoot.getPath() + " already exists and is not deletable.");
//        } else {
//            if (this.decompiledRoot.exists() && this.decompile) {
//                try {
//                    FileUtils.deleteDirectory(decompiledRoot);
//                } catch (IOException e) {
//                    throw new SpoonException("Dir " + decompiledRoot.getPath() + " already exists and is not deletable.");
//                }
//            }
//
//            if (!this.decompiledRoot.exists()) {
//                this.decompiledRoot.mkdirs();
//                this.decompile = true;
//            }
//
//            this.decompiledSrc = new File(this.decompiledRoot, "src/main/java");
//            if (!this.decompiledSrc.exists()) {
//                this.decompiledSrc.mkdirs();
//                this.decompile = true;
//            }
//
//            if (decompiler == null) {
//                this.decompiler = this.getDefaultDecompiler();
//            }
//
//            jarPaths.forEach(jarPath -> {
//                File jar = new File(jarPath);
//                if (jar.exists() && jar.isFile()) {
//                    if (this.decompile || (jar.lastModified() > this.decompiledSrc.lastModified())) {
//                        this.decompiler.decompile(jar.getAbsolutePath());
//                    }
//                } else {
//                    throw new SpoonException("Jar " + jar.getPath() + " not found.");
//                }
//            });
//            this.addInputResource(this.decompiledSrc.getAbsolutePath());
//        }
//    }



}






