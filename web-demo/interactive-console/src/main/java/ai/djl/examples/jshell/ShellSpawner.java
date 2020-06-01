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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.system.ApplicationHome;

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
                        "api-0.6.0-SNAPSHOT.jar",
                        "gson-2.8.6.jar",
                        "jna-5.3.0.jar",
                        "slf4j-api-1.7.30.jar",
                        "log4j-api-2.13.2.jar",
                        "log4j-to-slf4j-2.13.2.jar",
                        "javacpp-1.5.3.jar");
        List<String> engines =
                Arrays.asList(
                        "pytorch-engine-0.6.0-SNAPSHOT.jar",
                        "pytorch-model-zoo-0.6.0-SNAPSHOT.jar",
                        "pytorch-native-auto-1.5.0.jar",
                        "mxnet-engine-0.6.0-SNAPSHOT.jar",
                        "mxnet-model-zoo-0.6.0-SNAPSHOT.jar",
                        "mxnet-native-auto-1.7.0-a.jar",
                        "tensorflow-api-0.5.0.jar",
                        "tensorflow-engine-0.6.0-SNAPSHOT.jar",
                        "tensorflow-model-zoo-0.6.0-SNAPSHOT.jar",
                        "tensorflow-native-auto-2.1.0.jar");
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
            for (String name : names) {
                InputStream is =
                        ai.djl.examples.jshell.ShellSpawner.class.getResourceAsStream(
                                "/BOOT-INF/lib/" + name);
                try {
                    Files.copy(is, dir.resolve(name));
                } catch (IOException e) {
                    throw new RuntimeException("Copy to dir failed", e);
                }
            }
        }
    }
}
