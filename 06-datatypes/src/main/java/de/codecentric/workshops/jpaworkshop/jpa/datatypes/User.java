package de.codecentric.workshops.jpaworkshop.jpa.datatypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "\"user\"")
public class User {
	@Id
	@GeneratedValue(generator = "myGenerator")
	@Column(name = "user_id")
	private Long id;
	private String name;
	@Enumerated(EnumType.STRING)
	private UserLevel level;

	@Transient
	private Address address;

	public User() {
	}

	public User(String name, UserLevel level) {
		this.name = name;
		this.level = level;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UserLevel getLevel() {
		return level;
	}

	public void setLevel(UserLevel level) {
		this.level = level;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
}
