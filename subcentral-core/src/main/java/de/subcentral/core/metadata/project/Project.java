package de.subcentral.core.metadata.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.subcentral.core.metadata.Contributor;
import de.subcentral.core.metadata.media.Media;

public class Project {
	private Media				object;
	private String				state;
	private String				vacancies;
	private List<Membership>	members	= new ArrayList<>();

	public Media getObject() {
		return object;
	}

	public void setObject(Media object) {
		this.object = object;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVacancies() {
		return vacancies;
	}

	public void setVacancies(String vacancies) {
		this.vacancies = vacancies;
	}

	public List<Membership> getMembers() {
		return members;
	}

	public void setMembers(List<Membership> members) {
		this.members.clear();
		this.members.addAll(members);
	}

	public class Membership {
		private final Contributor	member;
		private Set<String>			roles	= new HashSet<>();

		public Membership(Contributor member) {
			this.member = member;
		}

		public Contributor getMember() {
			return member;
		}

		public Set<String> getRoles() {
			return roles;
		}

		public void setRoles(Set<String> roles) {
			this.roles.clear();
			this.roles.addAll(roles);
		}

		public Project getProject() {
			return Project.this;
		}
	}
}
