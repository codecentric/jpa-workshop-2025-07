package de.codecentric.workshops.jpaworkshop.jpa.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest
class MessageRepositoryTest {
	@Autowired
	JdbcClient jdbcClient;
	@Autowired
	EntityManagerFactory emf;
	@Autowired
	MessageRepository underTest;
	final User user1 = new User("user1", UserLevel.USER, LocalDate.now());
	final User user2 = new User("user2", UserLevel.MODERATOR, LocalDate.now());
	private Message msg1;
	private Message msg2;
	private Message msg3;
	private Instant now = Instant.now();

	@BeforeEach
	void resetDb() {
		final EntityManager entityManager = emf.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE from Message").executeUpdate();
		entityManager.createQuery("DELETE from User").executeUpdate();
		entityManager.persist(user1);
		entityManager.persist(user2);
		msg1 = new Message(user1, "to1", "content one", now);
		entityManager.persist(msg1);
		msg2 = new Message(user2, "to2", "content two", now);
		entityManager.persist(msg2);
		msg3 = new Message(user1, "to3", "content three", now);
		entityManager.persist(msg3);
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@Test
	void loadsSingleMessage() {
		final Optional<Message> loadedMessageOption = underTest.findById(msg1.getId());
		Assertions.assertThat(loadedMessageOption).isPresent();
		final Message loadedMessage = loadedMessageOption.get();
		assertThat(loadedMessage.getId()).isEqualTo(msg1.getId());
		assertThat(loadedMessage.getContent()).isEqualTo("content one");
	}

	@Test
	void loadsAllMessages() {
		final List<Message> messages = underTest.findAll();
		Assertions.assertThat(messages).hasSize(3);
		assertThat(messages.get(0).getId()).isEqualTo(msg1.getId());
		assertThat(messages.get(1).getId()).isEqualTo(msg2.getId());
		assertThat(messages.get(2).getId()).isEqualTo(msg3.getId());
	}

	@Test
	void findsMessageBySender() {
		final List<Message> actual = underTest.findAllBySender(user1);
		Assertions.assertThat(actual).hasSize(2);
		assertThat(actual.get(0).getId()).isEqualTo(msg1.getId());
		assertThat(actual.get(1).getId()).isEqualTo(msg3.getId());
	}

	@Test
	void findsMessageBySenderId() {
		final List<Message> actual = underTest.findAllBySenderId(user1.getId());
		Assertions.assertThat(actual).hasSize(2);
		assertThat(actual.get(0).getId()).isEqualTo(msg1.getId());
		assertThat(actual.get(1).getId()).isEqualTo(msg3.getId());
	}

	@Test
	void findsBySenderName() {
		final List<Message> actual = underTest.findAllBySenderName("user1");
		Assertions.assertThat(actual).hasSize(2);
		assertThat(actual.get(0).getId()).isEqualTo(msg1.getId());
		assertThat(actual.get(1).getId()).isEqualTo(msg3.getId());
	}

	@Test
	void findsBySenderIdAndContent() {
		var msg4 = new Message(user1, "to4", "another three", now);
		msg4 = underTest.save(msg4);
		final List<Message> actual = underTest.findAllBySenderIdAndContentContains(user1.getId(), "three");
		Assertions.assertThat(actual).hasSize(2);
		assertThat(actual.get(0).getId()).isEqualTo(msg3.getId());
		assertThat(actual.get(1).getId()).isEqualTo(msg4.getId());
	}

	@Test
	void findsCountBySenderId() {
		final int actual = underTest.countMessagesBySenderId(user1.getId());
		assertThat(actual).isEqualTo(2);
	}

	@Test
	void savesMessage() {
		final Message newMessage = new Message(user1, "to1", "content_new", now);
		final Message savedMessage = underTest.save(newMessage);
		assertThat(savedMessage).usingRecursiveComparison().isEqualTo(newMessage);
		assertThat(jdbcClient.sql("SELECT count(*) from messages;").query(int.class).single()).isEqualTo(4);
		assertThat(jdbcClient.sql("SELECT count(*) from messages where ID=?;")
			.param(1, savedMessage.getId())
			.query(int.class)
			.single()).isEqualTo(1);
		jdbcClient.sql("SELECT * from messages where ID=?;").param(1, 0).query(rs -> {
			assertThat(rs.getString("content")).isEqualTo("content_new");
		});

	}

	@Test
	void ordersByTimestamp() {
		new Random().longs(10, 0, 100_000_000)
			.forEach(timestamp -> underTest.save(new Message(user1,
				"to",
				"content",
				Instant.ofEpochSecond(timestamp)
			)));
		final List<Message> actual = underTest.findAllBySenderIdOrderByTimestamp(user1.getId());
		Assertions.assertThat(actual).hasSize(12); // 2 from above and 10 from here
		actual.stream().map(Message::getTimestamp).reduce(Instant.EPOCH, (previous, current) -> {
			Assertions.assertThat(previous).isBeforeOrEqualTo(current);
			return current;
		});
	}

	@Test
	void ordersByCustomSort() {
		new Random().longs(10, 0, 100_000_000)
			.forEach(timestamp -> underTest.save(new Message(user1,
				"to",
				"content",
				Instant.ofEpochSecond(timestamp)
			)));
		final List<Message> actual = underTest.findAllBySenderId(user1.getId(), Sort.by("timestamp").reverse());
		Assertions.assertThat(actual).hasSize(12); // 2 from above and 10 from here
		actual.stream().map(Message::getTimestamp).reduce(Instant.MAX, (previous, current) -> {
			Assertions.assertThat(previous).isAfterOrEqualTo(current);
			return current;
		});
	}

	@Test
	void lazyLoadingAfterDetaching() {
		final Optional<Message> loaded = underTest.findById(msg1.getId());
		Assertions.assertThat(loaded).isPresent();
		final Message loadedMessage = loaded.get();
		final User sender = loadedMessage.getSender();
		assertThatThrownBy(sender::getName).isInstanceOf(LazyInitializationException.class);
	}

	@Test
	@Transactional
	void lazyLoadingWhileInSession() {
		final Optional<Message> loaded = underTest.findById(msg1.getId());
		Assertions.assertThat(loaded).isPresent();
		final Message loadedMessage = loaded.get();
		final User sender = loadedMessage.getSender();
		assertThat(sender.getName()).isNotEmpty();
	}

	@Test
	void detachedObjectsDoNotGetSaved() {
		final Message message = underTest.findByIdWithTransaction(msg1.getId());
		message.setContent("new content");
		final Message message2 = underTest.findByIdWithTransaction(msg1.getId());
		assertThat(message2.getContent()).isEqualTo("content one");
	}

	@Test
	@Transactional
	void attachedObjectsAreCached() {
		final Message message = underTest.findByIdWithTransaction(msg1.getId());
		message.setContent("new content");
		final Message message2 = underTest.findByIdWithTransaction(msg1.getId());
		assertThat(message2.getContent()).isEqualTo("new content");
		assertThat(message == message2).isTrue();
	}

	@Test
	void detachedObjectsCanBeAttached() {
		final Message detachedMessage = underTest.findByIdWithTransaction(msg1.getId());
		detachedMessage.setContent("new content");
		final var attachedMessage = underTest.save(detachedMessage);
		final Message loadedLater = underTest.findByIdWithTransaction(msg1.getId());
		assertThat(loadedLater.getContent()).isEqualTo("new content");
	}
}