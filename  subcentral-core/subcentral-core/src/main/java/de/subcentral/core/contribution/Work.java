package de.subcentral.core.contribution;

import java.util.List;

public interface Work
{
	/**
	 * 
	 * @return The contributions to this work. Ordered in a list, so that a specific ordering can be maintained.
	 */
	public List<Contribution> getContributions();
}
