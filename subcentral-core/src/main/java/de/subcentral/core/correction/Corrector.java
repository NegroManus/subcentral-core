package de.subcentral.core.correction;

import java.util.List;

public interface Corrector<T>
{
	public void correct(T bean, List<Correction> corrections);
}
