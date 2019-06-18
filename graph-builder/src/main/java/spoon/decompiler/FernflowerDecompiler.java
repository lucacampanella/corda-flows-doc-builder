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

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FernflowerDecompiler implements Decompiler {

	File outputDir;

	public FernflowerDecompiler(File outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public void decompile(String jarPath) {
		ConsoleDecompiler.main(new String[]{jarPath, outputDir.getPath()});

		String jarName = jarPath.substring(jarPath.lastIndexOf(System.getProperty("file.separator"))+1);
		try {
			unzipJar(Paths.get(outputDir.getPath(), jarName).toString(), outputDir.getPath());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not unzip decompiled jar file");
		}
	}

	private void unzipJar(String toBeExtractedJar, String destDir) throws IOException {
		java.util.jar.JarFile jar = new java.util.jar.JarFile(toBeExtractedJar);
		java.util.Enumeration enumEntries = jar.entries();
		while (enumEntries.hasMoreElements()) {
			java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
			java.io.File f = new java.io.File(destDir + java.io.File.separator + file.getName());
			f.getParentFile().mkdirs();
			if (file.isDirectory()) { // if its a directory, create it
				f.mkdir();
				continue;
			}
			java.io.InputStream is = jar.getInputStream(file); // get the input stream
			java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
			while (is.available() > 0) {  // write contents of 'is' to 'fos'
				fos.write(is.read());
			}
			fos.close();
			is.close();
		}
		jar.close();
	}
}
