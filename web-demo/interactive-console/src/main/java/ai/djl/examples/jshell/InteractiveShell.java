package ai.djl.examples.jshell;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;

public class InteractiveShell {

    private JShell js;
    private String id;
    private long timeStamp;

    public InteractiveShell(String id) {
        js = JShell.create();
        this.id = id;
        timeStamp = System.currentTimeMillis();
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void updateTimeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    public void addDependency(Path jarPath) {
        if (!jarPath.toFile().exists()) {
            throw new IllegalArgumentException(
                    "jar does not exist in " + jarPath.toAbsolutePath().toString());
        }
        js.addToClasspath(jarPath.toAbsolutePath().toString());
    }

    public void addDependencyDir(Path dir) {
        if (!dir.toFile().isDirectory()) {
            throw new IllegalArgumentException(dir.toString() + " is not directory");
        }
        File[] files = dir.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                addDependency(file.toPath());
            }
        }
    }

    public String addCommand(String input) {
        List<SnippetEvent> events = js.eval(input);
        StringBuilder sb = new StringBuilder();
        for (SnippetEvent event : events) {
            if (event.causeSnippet() == null) {
                JShellException e = event.exception();
                if (e != null) {
                    final StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw, true);
                    e.printStackTrace(pw);
                    sb.append(sw.getBuffer().toString());
                } else {
                    if (event.status() == Snippet.Status.REJECTED) {
                        sb.append("JConsole rejected command ").append(event.toString());
                    }
                    if (event.value() != null) {
                        sb.append(event.value());
                    }
                }
            }
        }
        return sb.toString();
    }

    public void close() {
        js.close();
    }

    public String getId() {
        return id;
    }
}
