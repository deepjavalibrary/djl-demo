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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
}
