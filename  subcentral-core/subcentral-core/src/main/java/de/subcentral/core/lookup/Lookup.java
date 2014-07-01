package de.subcentral.core.lookup;

public interface Lookup<R, P>
{
	public String getDomain();

	public boolean isLookupAvailable();

	public Class<R> getResultClass();

	public LookupQuery<R> createQuery(String query);

	public LookupQuery<R> createQueryFromParameters(P parameterBean);

	public Class<P> getParameterBeanClass();

	public LookupQuery<R> createQueryFromEntity(Object queryEntity);

	public boolean isQueryEntitySupported(Object queryEntity);

}
