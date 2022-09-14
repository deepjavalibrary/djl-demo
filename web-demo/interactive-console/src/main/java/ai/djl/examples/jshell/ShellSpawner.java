/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package ai.djl.examples.jshell;

import org.springframework.boot.system.ApplicationHome;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ShellSpawner {

    private ShellSpawner() {}

    static InteractiveShell createShell(String engine) {
        InteractiveShell shell = new InteractiveShell();
        ApplicationHome home = new ApplicationHome(BlockRunnerController.class);
        Path targetDir = home.getDir().toPath().resolve("djl");
        extractJars(targetDir);
        shell.addDependencyDir(targetDir.resolve("basic"));
        File[] engineFiles = targetDir.resolve("engines").toFile().listFiles();
        if (engineFiles == null) {
            throw new IllegalStateException("Cannot find Engine files");
        }
        for (File file : engineFiles) {
            if (file.getName().startsWith(engine)) {
                shell.addDependency(file.toPath());
            }
        }
        return shell;
    }

    private static void extractJars(Path dir) {
        List<String> deps =
                Arrays.asList(
                        "api-",
                        "gson-",
                        "commons-compress-",
                        "jna-",
                        "slf4j-api-",
                        "log4j-",
                        "log4j-to-slf4j-",
                        "protobuf-java-",
                        "javacpp-");
        List<String> engines = Arrays.asList("pytorch-", "mxnet-", "tensorflow-");
        Path basicDepsDir = dir.resolve("basic");
        extractAndCopy(basicDepsDir, deps);
        Path engineDir = dir.resolve("engines");
        extractAndCopy(engineDir, engines);
    }

    private static void extractAndCopy(Path dir, List<String> names) {
        if (!dir.toFile().exists()) {
            if (!dir.toFile().mkdirs()) {
                throw new IllegalStateException("Cannot make directories in " + dir);
            }
            URL url = ShellSpawner.class.getProtectionDomain().getCodeSource().getLocation();
            String path = url.getPath();
            if (path.endsWith("!/BOOT-INF/classes!/")) {
                path = path.substring(5, path.length() - 20);
            }
            if (!path.toLowerCase().endsWith(".jar")) {
                throw new IllegalStateException("Must run in jar file.");
            }
            try (JarFile jarFile = new JarFile(new File(path))) {
                Enumeration<JarEntry> en = jarFile.entries();
                while (en.hasMoreElements()) {
                    JarEntry entry = en.nextElement();
                    String fileName = entry.getName();
                    if (fileName.startsWith("BOOT-INF/lib/") && fileName.endsWith(".jar")) {
                        fileName = fileName.substring(13);
                        for (String n : names) {
                            if (fileName.startsWith(n)) {
                                InputStream is = jarFile.getInputStream(entry);
                                Files.copy(
                                        is,
                                        dir.resolve(fileName),
                                        StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Copy to dir failed", e);
            }
        }
    }
}
