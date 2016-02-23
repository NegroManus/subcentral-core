package de.subcentral.watcher.controller.process;

import java.util.Objects;

import de.subcentral.core.metadata.release.Release;

public class OutputNode implements ProcessNode
{
	private final InputNode	inputNode;
	private final Release	release;

	protected OutputNode(InputNode inputNode, Release release)
	{
		this.inputNode = Objects.requireNonNull(inputNode, "inputNode");
		this.release = Objects.requireNonNull(release, "release");
	}
}
