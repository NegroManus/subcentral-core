package de.subcentral.core.metadata;

import java.util.List;

public interface Work
{
	/**
	 * The contributions to this work. Ordered in a list, so that a specific ordering can be maintained.
	 * 
	 * @return the contributions
	 */
	public List<Contribution> getContributions();
}
