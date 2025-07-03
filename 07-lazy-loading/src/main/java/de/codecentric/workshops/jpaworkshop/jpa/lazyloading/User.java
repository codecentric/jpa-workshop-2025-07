package de.codecentric.workshops.jpaworkshop.jpa.lazyloading;

import java.time.LocalDate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
	private LocalDate dateOfBirth;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	private Address address;

	public User() {
	}

	public User(String name, UserLevel level, LocalDate dateOfBirth) {
		this.name = name;
		this.level = level;
		this.dateOfBirth = dateOfBirth;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
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

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
}
