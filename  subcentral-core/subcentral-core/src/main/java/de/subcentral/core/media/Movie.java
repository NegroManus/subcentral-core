package de.subcentral.core.media;

import java.util.HashSet;
import java.util.Set;

public class Movie extends AbstractAvMedia {
	private String originalLanguage;
	private Set<String> countriesOfOrigin = new HashSet<>(1);

	@Override
	public String getOriginalLanguage() {
		return originalLanguage;
	}

	public void setOriginalLanguage(String originalLanguage) {
		this.originalLanguage = originalLanguage;
	}

	@Override
	public Set<String> getCountriesOfOrigin() {
		return countriesOfOrigin;
	}

	public void setCountriesOfOrigin(Set<String> countriesOfOrigin) {
		this.countriesOfOrigin = countriesOfOrigin;
	}
}
