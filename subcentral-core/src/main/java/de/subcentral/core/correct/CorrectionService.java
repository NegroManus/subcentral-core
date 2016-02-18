package de.subcentral.core.correct;

import java.util.List;

public interface CorrectionService
{
	public String getDomain();

	public List<Correction> correct(Object bean);
}
