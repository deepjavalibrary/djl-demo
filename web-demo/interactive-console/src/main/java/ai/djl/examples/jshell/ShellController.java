package ai.djl.examples.jshell;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
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

    private static final ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
    private static final long TIME_OUT = Duration.of(5, MINUTES).toMillis();
    private static final Map<String, InteractiveShell> shells = new ConcurrentHashMap<>();

    static {
        ses.scheduleAtFixedRate(ShellController::houseKeeping, 1, 1, TimeUnit.MINUTES);
    }

    @RequestMapping("/")
    public String index() {
        return "Greetings from DJL Live Console!";
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/addCommand")
    Map<String, String> addCommand(@RequestBody Map<String, String> request) {
        String clientConsoleId = request.get("console_id");
        InteractiveShell shell = shells.computeIfAbsent(clientConsoleId, this::createShell);
        String command = request.get("command");
        command = command.endsWith(";") ? command : command + ";";
        String result = shell.addCommand(command);
        Map<String, String> response = new ConcurrentHashMap<>();
        response.put("result", result);
        shell.updateTimeStamp();
        return response;
    }

    private static void houseKeeping() {
        for (Map.Entry<String, InteractiveShell> entry : shells.entrySet()) {
            // over 5 mins
            InteractiveShell shell = entry.getValue();
            if (System.currentTimeMillis() - shell.getTimeStamp() > TIME_OUT) {
                shell.close();
                shells.remove(entry.getKey());
            }
        }
    }

    private InteractiveShell createShell(String consoleId) {
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

    private void extractJars(Path dir) {
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
                throw new RuntimeException("Cannot make directories in " + dir);
            }
            for (String name : names) {
                InputStream is = ShellController.class.getResourceAsStream("/BOOT-INF/lib/" + name);
                try {
                    Files.copy(is, dir.resolve(name));
                } catch (IOException e) {
                    throw new RuntimeException("Copy to dir failed", e);
                }
            }
        }
    }
}
