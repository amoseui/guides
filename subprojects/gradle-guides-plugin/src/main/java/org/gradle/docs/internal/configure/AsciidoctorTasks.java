package org.gradle.docs.internal.configure;

import groovy.lang.Closure;
import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.CopySpec;
import org.gradle.api.logging.StandardOutputListener;
import org.gradle.docs.guides.internal.GuideContentBinary;
import org.gradle.docs.internal.RenderableContentBinary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AsciidoctorTasks {
    private static final Object IGNORED_CLOSURE_OWNER = new Object();

    public static void failTaskOnRenderingErrors(AsciidoctorTask task) {
        List<String> capturedOutput = new ArrayList<>();
        StandardOutputListener listener = it -> capturedOutput.add(it.toString());

        task.getLogging().addStandardErrorListener(listener);
        task.getLogging().addStandardOutputListener(listener);

        task.doLast(new Action<Task>() {
            @Override
            public void execute(Task t) {
                task.getLogging().removeStandardOutputListener(listener);
                task.getLogging().removeStandardErrorListener(listener);
                String output = capturedOutput.stream().collect(Collectors.joining());
                if (output.indexOf("include file not found:") > 0) {
                    throw new RuntimeException("Include file(s) not found.");
                } else if (output.indexOf("no callout found for") > 0) {
                    throw new RuntimeException("Missing callout.");
                }
            }
        });
    }

    public static void configureResources(AsciidoctorTask task, Map<String, Object> attributes, Collection<? extends RenderableContentBinary> binaries) {
        attributes.put("imagesdir", "images");
        task.getInputs().files(binaries.stream().map(RenderableContentBinary::getResourceFiles).collect(Collectors.toList())).withPropertyName("resourceFiles").optional(true);
        task.resources(new Closure(IGNORED_CLOSURE_OWNER) {
            public Object doCall(Object ignore) {
                binaries.stream().map(RenderableContentBinary::getResourceSpec).forEach(spec -> ((CopySpec)this.getDelegate()).with(spec.get()));
                return null;
            }
        });
    }
}