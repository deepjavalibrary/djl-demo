package ai.djl.examples.jshell;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShellController {

    private static ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
    private Map<String, InteractiveShell> shells;

    @RequestMapping("/")
    public String index() {
        return "Greetings from DJL Live Console!";
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/addCommand")
    Map<String, String> addCommand(@RequestBody Map<String, String> request) throws IOException {
        String clientConsoleId = request.get("console_id");
        InteractiveShell shell =
                getShells().getOrDefault(clientConsoleId, createShell(clientConsoleId));
        String command = request.get("command");
        command = command.endsWith(";") ? command : command + ";";
        String result = shell.addCommand(command);
        Map<String, String> response = new ConcurrentHashMap<>();
        response.put("result", result);
        shell.updateTimeStamp();
        shells.put(clientConsoleId, shell);
        return response;
    }

    private void houseKeeping() {
        for (String consoleId : shells.keySet()) {
            InteractiveShell shell = shells.get(consoleId);
            // over 5 mins
            if (System.currentTimeMillis() - shell.getTimeStamp() > 300000) {
                shell.close();
                shells.remove(consoleId);
            }
        }
    }

    private Map<String, InteractiveShell> getShells() {
        if (shells == null) {
            shells = new ConcurrentHashMap<>();
            // trigger every 1 min
            ses.scheduleAtFixedRate(this::houseKeeping, 1, 1, TimeUnit.MINUTES);
        }
        return shells;
    }

    private InteractiveShell createShell(String consoleId) throws IOException {
        InteractiveShell shell = new InteractiveShell(consoleId);
        ApplicationHome home = new ApplicationHome(ShellController.class);
        Path targetDir = home.getDir().toPath().resolve("djl");
        extractJars(targetDir);
        shell.addDependencyDir(targetDir);
        shell.addCommand("import ai.djl.ndarray.NDManager;");
        shell.addCommand("import ai.djl.ndarray.NDArray;");
        shell.addCommand("import ai.djl.ndarray.types.Shape;");
        shell.addCommand("NDManager manager = NDManager.newBaseManager();");
        return shell;
    }

    private void extractJars(Path dir) throws IOException {
        List<String> names =
                Arrays.asList(
                        "api-0.5.0.jar",
                        "gson-2.8.6.jar",
                        "jna-5.3.0.jar",
                        "slf4j-api-1.7.30.jar",
                        "pytorch-engine-0.5.0.jar",
                        "pytorch-native-auto-1.5.0.jar",
                        "log4j-api-2.13.2.jar",
                        "log4j-to-slf4j-2.13.2.jar");
        if (!dir.toFile().exists()) {
            if (!dir.toFile().mkdirs()) {
                throw new IOException("Cannot make directories in " + dir);
            }
            for (String name : names) {
                InputStream is = ShellController.class.getResourceAsStream("/BOOT-INF/lib/" + name);
                Files.copy(is, dir.resolve(name));
            }
        }
    }
}
