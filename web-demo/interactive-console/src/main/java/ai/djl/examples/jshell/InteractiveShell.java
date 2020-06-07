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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;

public class InteractiveShell {

    private JShell js;
    private long timeStamp;
    private SourceCodeAnalysis sca;

    public InteractiveShell() {
        js = JShell.create();
        timeStamp = System.currentTimeMillis();
        sca = js.sourceCodeAnalysis();
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
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw, true);
                    e.printStackTrace(pw);
                    sb.append(sw.getBuffer().toString());
                } else {
                    if (event.status() == Snippet.Status.REJECTED) {
                        sb.append("JConsole rejected command ").append(event.toString());
                    }
                    if (event.value() != null) {
                        sb.append(event.value());
                        sb.append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    public String compute(String input) {
        StringBuilder sb = new StringBuilder();
        SourceCodeAnalysis.CompletionInfo info;
        for (info = sca.analyzeCompletion(input);
                info.completeness().isComplete();
                info = sca.analyzeCompletion(info.remaining())) {
            sb.append(addCommand(info.source()));
        }

        if (info.completeness() != SourceCodeAnalysis.Completeness.EMPTY) {
            sb.append("Incomplete input:").append(info.remaining().trim());
        }
        return sb.toString();
    }

    public void close() {
        js.close();
    }
}
