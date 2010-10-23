package org.openspotlight.bundle.test;

import org.openspotlight.bundle.annotation.Dependency;
import org.openspotlight.bundle.annotation.StrongDependsOn;
import org.openspotlight.bundle.context.ExecutionContext;
import org.openspotlight.bundle.context.ExecutionContextProvider;
import org.openspotlight.bundle.task.ArtifactTask;
import org.openspotlight.federation.domain.artifact.Artifact;

/**
 * Created by IntelliJ IDEA.
 * User: feu
 * Date: Oct 4, 2010
 * Time: 3:44:10 PM
 * To change this template use File | Settings | File Templates.
 */
@StrongDependsOn(@Dependency(ExampleProcessingTask.class))
public class ExampleArtifactWithAnotherProcessingTask extends ArtifactTask {
    protected ExampleArtifactWithAnotherProcessingTask(ExecutionContextProvider provider, Artifact artifact) {
        super(provider, artifact);
    }

    @Override
    protected void execute(ExecutionContext context, Artifact artifact) throws Exception {
        ExampleExecutionHistory.add(this.getClass(), artifact, null);
    }
}