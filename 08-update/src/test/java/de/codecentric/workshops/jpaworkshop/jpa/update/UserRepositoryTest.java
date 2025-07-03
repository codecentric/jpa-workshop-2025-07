package de.codecentric.workshops.jpaworkshop.jpa.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Optional;

import de.codecentric.workshops.jpaworkshop.jpa.update.zipcode.Zipcode;
import de.codecentric.workshops.jpaworkshop.jpa.update.zipcode.ZipcodeCH;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
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
	@Autowired
	EntityManagerFactory emf;

	@Test
	void savesAndLoadsUserWithAddress() {
		final User user1 = new User("user1", UserLevel.ADMIN, LocalDate.now());
		user1.setAddress(new Address("strasse", "stadt", Zipcode.of("81671")));
		underTest.save(user1);
		em.flush();
		em.clear();
		final Optional<User> loaded = underTest.findById(user1.getId());
		Assertions.assertThat(loaded).isPresent();
		assertThat(loaded.get().getAddress()).isEqualTo(new Address("strasse", "stadt", Zipcode.of("81671")));
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
		assertThat(loaded.get().getAddress()).isEqualTo(new Address("strasse", "stadt", Zipcode.of("8161")));
		assertThat(loaded.get().getAddress().zip()).isInstanceOf(ZipcodeCH.class);
	}

	@Test
	void pessimisticLocking() {
		final var em1 = emf.createEntityManager();
		em1.getTransaction().begin();
		final User user1 = new User("user1", UserLevel.ADMIN, LocalDate.now());
		em1.persist(user1);
		em1.getTransaction().commit();

		em1.getTransaction().begin();
		em1.merge(user1);
		em1.lock(user1, LockModeType.PESSIMISTIC_WRITE);

		final EntityManager em2 = emf.createEntityManager();
		em2.getTransaction().begin();
		final var userInEm2 = em2.find(User.class, user1.getId());
		userInEm2.setName("new name 2");
		em2.getTransaction().commit();
	}

	@Test
	void optimisticLocking() {
		final EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		final User user1 = new User("user1", UserLevel.ADMIN, LocalDate.now());
		System.out.println("user1 initial version: " + user1.getVersion());
		em.persist(user1);
		em.getTransaction().commit();
		System.out.println("user1 version after persisting: " + user1.getVersion());

		em.clear();

		em.getTransaction().begin();
		final User loaded_1 = em.find(User.class, user1.getId());
		System.out.println("loaded_1 version: " + loaded_1.getVersion());
		em.detach(loaded_1);
		loaded_1.setName("new name 1");

		final User loaded_2 = em.find(User.class, user1.getId());
		System.out.println("loaded_2 version: " + loaded_2.getVersion());
		loaded_2.setName("new name 2");
		em.getTransaction().commit();
		System.out.println("loaded_2 version after update: " + loaded_2.getVersion());

		em.clear();
		em.getTransaction().begin();

		final User loaded_final = em.find(User.class, user1.getId());
		System.out.println("loaded_final version: " + loaded_final.getVersion());
		assertThat(loaded_final.getName()).isEqualTo("new name 2");
		em.getTransaction().commit();
		em.clear();

		em.getTransaction().begin();
		System.out.println("loaded_1 version before attempted update: " + loaded_1.getVersion());
		assertThatThrownBy(() -> em.merge(loaded_1)).isInstanceOf(OptimisticLockException.class);
	}
}