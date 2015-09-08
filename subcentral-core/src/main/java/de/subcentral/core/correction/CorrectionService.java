package de.subcentral.core.correction;

import java.util.List;

public interface CorrectionService
{
	public String getDomain();

	public List<Correction> correct(Object bean);
}
