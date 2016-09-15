package de.subcentral.core.metadata;

import java.io.Serializable;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.util.ObjectUtil;

public class Contribution implements Comparable<Contribution>, Serializable {
	private static final long	serialVersionUID		= 8988760112562101666L;

	public static final int		AMOUNT_NOT_MEASURABLE	= 0;

	private Contributor			contributor;
	private String				type;
	private String				description;
	private int					amount					= 1;
	private float				progress				= 1.0f;

	public Contribution() {
		// default constructor
	}

	public Contribution(Contributor contributor, String type) {
		this.contributor = contributor;
		this.type = type;
	}

	public Contribution(Contributor contributor, String type, String description) {
		this.contributor = contributor;
		this.type = type;
		this.description = description;
	}

	public Contribution(Contributor contributor, String type, String description, int amount) {
		this.contributor = contributor;
		this.type = type;
		this.description = description;
		setAmount(amount);
	}

	public Contribution(Contributor contributor, String type, String description, int amount, float progress) {
		this.contributor = contributor;
		this.type = type;
		this.description = description;
		setAmount(amount);
		setProgress(progress);
	}

	/**
	 * The person / company / etc that contributed.
	 * 
	 * @return the contributor
	 */
	public Contributor getContributor() {
		return contributor;
	}

	public void setContributor(Contributor contributor) {
		this.contributor = contributor;
	}

	/**
	 * The type of the contribution.
	 * 
	 * @return the contribution type
	 */
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Further description / specification of the contribution.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * The amount of the contribution. This is a relative value. How big that amount is, can only be determined by knowing the amount of the other contributions. The default value is 1.
	 * 
	 * @return the amount (a zero or positive int)
	 */
	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) throws IllegalArgumentException {
		if (amount < 0) {
			throw new IllegalArgumentException("amount must be zero or positive");
		}
		this.amount = amount;
	}

	/**
	 * The progress of the contribution. A percentage value between 0.0f and 1.0f inclusively. The default value is 1.0f (complete).
	 * 
	 * @return the progress (0.0f - 1.0f)
	 */
	public float getProgress() {
		return progress;
	}

	/**
	 * 
	 * @param progress
	 * @throws IllegalArgumentException
	 *             if progress is smaller than 0 or greater than 1
	 */
	public void setProgress(float progress) throws IllegalArgumentException {
		if (progress < 0.0f || progress > 1.0f) {
			throw new IllegalArgumentException("progress must be between 0.0 and 1.0 inclusively");
		}
		this.progress = progress;
	}

	// Object methods
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Contribution) {
			Contribution o = (Contribution) obj;
			return Objects.equals(contributor, o.contributor) && Objects.equals(type, o.type) && Objects.equals(description, o.description);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(contributor, type, description);
	}

	@Override
	public int compareTo(Contribution o) {
		if (this == o) {
			return 0;
		}
		// nulls first
		if (o == null) {
			return 1;
		}
		return ComparisonChain.start()
				.compare(contributor, o.contributor, ObjectUtil.getDefaultOrdering())
				.compare(type, o.type, ObjectUtil.getDefaultStringOrdering())
				.compare(description, o.description, ObjectUtil.getDefaultStringOrdering())
				.result();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Contribution.class)
				.omitNullValues()
				.add("contributor", contributor)
				.add("type", type)
				.add("description", description)
				.add("amount", amount)
				.add("progress", progress)
				.toString();
	}
}
