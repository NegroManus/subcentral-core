package de.subcentral.core.correct;

import java.util.List;

import de.subcentral.core.util.Service;

public interface CorrectionService extends Service
{
	public List<Correction> correct(Object bean);
}
