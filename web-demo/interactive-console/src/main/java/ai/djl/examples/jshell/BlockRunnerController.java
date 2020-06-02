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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlockRunnerController {

    @CrossOrigin(origins = "*")
    @PostMapping("/compute")
    Map<String, String> addCommand(@RequestBody Map<String, String> request) {
        InteractiveShell shell = ShellSpawner.createShell(request.get("engine"));
        String commands = request.get("commands");
        String result = shell.compute(commands);
        Map<String, String> response = new ConcurrentHashMap<>();
        response.put("result", result);
        shell.close();
        return response;
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/createzip", produces = "application/zip")
    public void zipFiles(@RequestBody Map<String, String> request, HttpServletResponse response)
            throws IOException, URISyntaxException {
        // setting headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename=\"starter.zip\"");
        response.addHeader("Content-Type", "application/zip");
        prepareFiles(request.get("engine"), request.get("commands"), response);
    }

    private void prepareFiles(String engine, String commands, HttpServletResponse response)
            throws IOException, URISyntaxException {
        ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
        List<String> names =
                Arrays.asList(
                        "gradlew",
                        "gradlew.bat",
                        "settings.gradle",
                        "gradle/wrapper/gradle-wrapper.properties",
                        "gradle/wrapper/GradleWrapperDownloader.java");
        for (String name : names) {
            InputStream is = getClass().getClassLoader().getResourceAsStream("/starter/" + name);
            addZipEntry(is, name, zos);
        }
        InputStream is =
                getClass().getClassLoader().getResourceAsStream("/starter/" + engine + ".gradle");
        addZipEntry(is, "build.gradle", zos);
        String javaFileContents = naiveCommandSplitter(commands);
        is = new ByteArrayInputStream(javaFileContents.getBytes(UTF_8));
        addZipEntry(is, "src/main/java/ai/djl/examples/Example.java", zos);
        zos.close();
    }

    private void addZipEntry(InputStream is, String targetLocation, ZipOutputStream zos)
            throws IOException {
        ZipEntry entry = new ZipEntry("djl-starter/" + targetLocation);
        zos.putNextEntry(entry);
        is.transferTo(zos);
        zos.closeEntry();
    }

    private String naiveCommandSplitter(String commands) {
        String[] splitCommands = commands.split("\n");
        StringBuilder sb = new StringBuilder();
        List<String> imports = new ArrayList<>();
        List<String> scopeCommands = new ArrayList<>();
        for (String command : splitCommands) {
            if (command.startsWith("import ")) {
                imports.add(command);
            } else {
                scopeCommands.add(command);
            }
        }
        sb.append("package ai.djl.examples;\n");
        sb.append(String.join("\n", imports));
        sb.append("\n\npublic class Example {\n  public static void main(String[] args) {\n    ");
        sb.append(String.join("\n    ", scopeCommands));
        sb.append("\n  }\n}\n");
        return sb.toString();
    }
}
