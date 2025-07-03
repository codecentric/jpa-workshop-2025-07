package de.codecentric.workshops.jpaworkshop.jpa.lazyloading;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import de.codecentric.workshops.jpaworkshop.jpa.lazyloading.zipcode.Zipcode;
import de.codecentric.workshops.jpaworkshop.jpa.lazyloading.zipcode.ZipcodeCH;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = { "spring.jpa.hibernate.ddl-auto=create-drop" })
class UserRepositoryTest {
	@Autowired
	private UserRepository underTest;
	@Autowired
	EntityManager em;

	@Test
	void savesAndLoadsUserWithAddress() {
		final User user1 = new User("user1", UserLevel.ADMIN, LocalDate.now());
		user1.setAddress(new Address("strasse", "stadt", Zipcode.of("81671")));
		underTest.save(user1);
		em.flush();
		em.clear();
		final Optional<User> loaded = underTest.findById(user1.getId());
		Assertions.assertThat(loaded).isPresent();
		assertThat(loaded.get().getAddress())
			.isEqualTo(new Address("strasse", "stadt", Zipcode.of("81671")));
	}

	@Test
	void savesAndLoadsUserWithSwissAddress() {
		final User user1 = new User("user1", UserLevel.ADMIN, LocalDate.now());
		user1.setAddress(new Address("strasse", "stadt", Zipcode.of("8161")));
		underTest.save(user1);
		em.flush();
		em.clear();
		final Optional<User> loaded = underTest.findById(user1.getId());
		Assertions.assertThat(loaded).isPresent();
		assertThat(loaded.get().getAddress())
			.isEqualTo(new Address("strasse", "stadt", Zipcode.of("8161")));
		assertThat(loaded.get().getAddress().getZip()).isInstanceOf(ZipcodeCH.class);
	}
}