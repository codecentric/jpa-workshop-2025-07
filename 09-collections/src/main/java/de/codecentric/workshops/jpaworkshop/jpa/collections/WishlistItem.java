package de.codecentric.workshops.jpaworkshop.jpa.collections;

import java.net.URI;

import jakarta.persistence.Embeddable;

@Embeddable
public record WishlistItem(String description, URI orderLink) {
}
