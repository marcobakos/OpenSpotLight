package org.openspotlight.bundle.language.java.bundle;

import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.openspotlight.bundle.language.java.parser.JavaPublicElementsTree;
import org.openspotlight.bundle.language.java.parser.executor.JavaExecutorSupport;
import org.openspotlight.bundle.language.java.parser.executor.JavaPublicElementsTreeExecutor;
import org.openspotlight.federation.context.ExecutionContext;
import org.openspotlight.federation.domain.artifact.LastProcessStatus;
import org.openspotlight.federation.domain.artifact.StringArtifact;
import org.openspotlight.federation.processing.BundleProcessorArtifactPhase;
import org.openspotlight.federation.processing.CurrentProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaParserPublicElementsPhase implements
		BundleProcessorArtifactPhase<StringArtifact> {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void beforeProcessArtifact(final StringArtifact artifact,
			final CurrentProcessorContext currentContext,
			final ExecutionContext context) {

	}

	public void didFinishToProcessArtifact(final StringArtifact artifact,
			final LastProcessStatus status,
			final CurrentProcessorContext currentContext,
			final ExecutionContext context) {

	}

	public Class<StringArtifact> getArtifactType() {
		return StringArtifact.class;
	}

	public LastProcessStatus processArtifact(final StringArtifact artifact,
			final CurrentProcessorContext currentContext,
			final ExecutionContext context) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(" starting to process artifact " + artifact);
		}
		try {

			JavaTransientDto dto = (JavaTransientDto) artifact
					.getTransientMap().get("DTO-Parser");
			final CommonTreeNodeStream treeNodes = new CommonTreeNodeStream(
					dto.tree);
			final JavaPublicElementsTree walker = new JavaPublicElementsTree(
					treeNodes);
			treeNodes.setTokenStream(dto.commonTokenStream);
			final JavaExecutorSupport support = new JavaExecutorSupport(
					currentContext.getCurrentNodeGroup(), context
							.getGraphSession(), artifact
							.getArtifactCompleteName());
			walker.setExecutor(new JavaPublicElementsTreeExecutor(support,
					artifact.getVersion()));
			walker.compilationUnit();
			dto = JavaTransientDto.fromTree(dto).withTreeNodeStream(treeNodes)
					.withWalker(walker).withExecutorSupport(support).create();
			artifact.getTransientMap().put("DTO-PublicElementsTree", dto);

			return LastProcessStatus.PROCESSED;
		} finally {
			if (logger.isDebugEnabled()) {
				logger.debug("ending to process artifact " + artifact);
			}
		}

	}
}
