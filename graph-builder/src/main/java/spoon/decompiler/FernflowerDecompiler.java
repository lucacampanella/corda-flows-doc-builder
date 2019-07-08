/**
 * Copyright (C) 2006-2018 INRIA and contributors
 * Spoon - http://spoon.gforge.inria.fr/
 *
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package spoon.decompiler;

import com.github.lucacampanella.callgraphflows.staticanalyzer.BuildingModelException;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class FernflowerDecompiler implements Decompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FernflowerDecompiler.class);

	File outputDir;

	public FernflowerDecompiler(File outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public void decompile(String jarPath) {

		String logLevelOption = "-log="; //log (INFO): a logging level, possible values are TRACE, INFO, WARN, ERROR
		if(LOGGER.isTraceEnabled()) {
			logLevelOption += "TRACE";
		}
		else if(LOGGER.isInfoEnabled()) {
			logLevelOption += "INFO";
		}
		else if(LOGGER.isWarnEnabled()) {
			logLevelOption += "WARN";
		}
		else{ //if is ERROR lv
			logLevelOption += "ERROR";
		}
		ConsoleDecompiler.main(new String[]{logLevelOption, jarPath, outputDir.getPath()});
		String jarName = jarPath.substring(jarPath.lastIndexOf(System.getProperty("file.separator"))+1);
		try {
			unzipJar(Paths.get(outputDir.getPath(), jarName).toString(), outputDir.getPath());
		} catch (IOException e) {
			LOGGER.error("Could not unzip decompiled jar file", e);
			throw new BuildingModelException("Could not unzip decompiled jar file");
		}
	}

	private void unzipJar(String toBeExtractedJar, String destDir) throws IOException {
		try(java.util.jar.JarFile jar = new java.util.jar.JarFile(toBeExtractedJar)) {
			java.util.Enumeration enumEntries = jar.entries();
			while (enumEntries.hasMoreElements()) {
				java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
				java.io.File f = new java.io.File(destDir + java.io.File.separator + file.getName());
				f.getParentFile().mkdirs();
				if (file.isDirectory()) { // if its a directory, create it
					f.mkdir();
					continue;
				}
				try(java.io.InputStream is = jar.getInputStream(file)) { // get the input stream
					try (java.io.FileOutputStream fos = new java.io.FileOutputStream(f)) {
						while (is.available() > 0) {  // write contents of 'is' to 'fos'
							fos.write(is.read());
						}
					}
				}
			}
		}
	}
}
