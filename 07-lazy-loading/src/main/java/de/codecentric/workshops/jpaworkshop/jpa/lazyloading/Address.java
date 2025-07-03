package de.codecentric.workshops.jpaworkshop.jpa.lazyloading;

import java.util.Objects;

import de.codecentric.workshops.jpaworkshop.jpa.lazyloading.zipcode.Zipcode;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String street;
	private String city;
	private Zipcode zip;

	public Address(String street, String city, Zipcode zip) {
		this.street = street;
		this.city = city;
		this.zip = zip;
	}

	public Address() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Zipcode getZip() {
		return zip;
	}

	public void setZip(Zipcode zip) {
		this.zip = zip;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (Address) obj;
		return Objects.equals(this.street, that.street) && Objects.equals(this.city, that.city) && Objects.equals(this.zip,
			that.zip);
	}

	@Override
	public int hashCode() {
		return Objects.hash(street, city, zip);
	}

	@Override
	public String toString() {
		return "Address[" + "street=" + street + ", " + "city=" + city + ", " + "zip=" + zip + ']';
	}

	// could be either javabean or record, both works as embeddable
}
