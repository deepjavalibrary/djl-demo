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

import static java.time.temporal.ChronoUnit.MINUTES;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
public class ShellController {

    private static final ScheduledExecutorService SES = Executors.newScheduledThreadPool(1);
    private static final long TIME_OUT = Duration.of(5, MINUTES).toMillis();
    private static final Map<String, InteractiveShell> SHELLS = new ConcurrentHashMap<>();

    static {
        SES.scheduleAtFixedRate(ShellController::houseKeeping, 1, 1, TimeUnit.MINUTES);
    }

    @RequestMapping("/")
    public String index() {
        return "<html><head>\n"
                + "  <meta http-equiv=\"refresh\" content=\"0;"
                + " URL=https://djl.ai/website/demo.html\" />\n"
                + "</head></html>";
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/addCommand")
    Map<String, String> addCommand(@RequestBody Map<String, String> request) {
        String clientConsoleId = request.get("console_id");
        InteractiveShell shell = SHELLS.computeIfAbsent(clientConsoleId, id -> prepareShell());
        String command = request.get("command");
        command = command.endsWith(";") ? command : command + ";";
        String result = shell.addCommand(command);
        Map<String, String> response = new ConcurrentHashMap<>();
        response.put("result", result);
        shell.updateTimeStamp();
        return response;
    }

    private static void houseKeeping() {
        for (Map.Entry<String, InteractiveShell> entry : SHELLS.entrySet()) {
            // over 5 mins
            InteractiveShell shell = entry.getValue();
            if (System.currentTimeMillis() - shell.getTimeStamp() > TIME_OUT) {
                shell.close();
                SHELLS.remove(entry.getKey());
            }
        }
    }

    private InteractiveShell prepareShell() {
        InteractiveShell shell = ShellSpawner.createShell("pytorch");
        shell.addCommand("import ai.djl.ndarray.NDManager;");
        shell.addCommand("import ai.djl.ndarray.NDArray;");
        shell.addCommand("import ai.djl.ndarray.types.Shape;");
        shell.addCommand("import ai.djl.ndarray.index.NDIndex;");
        shell.addCommand("NDManager manager = NDManager.newBaseManager();");
        return shell;
    }
}
