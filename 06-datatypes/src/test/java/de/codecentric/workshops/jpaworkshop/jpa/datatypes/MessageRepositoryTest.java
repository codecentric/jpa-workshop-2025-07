package de.codecentric.workshops.jpaworkshop.jpa.datatypes;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
	final User user1 = new User("user1", UserLevel.USER);
	final User user2 = new User("user2", UserLevel.MODERATOR);
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
		msg1 = new Message(user1, "to1", "content one", Instant.now());
		entityManager.persist(msg1);
		msg2 = new Message(user2, "to2", "content two", Instant.now());
		entityManager.persist(msg2);
		msg3 = new Message(user1, "to3", "content three", Instant.now());
		entityManager.persist(msg3);
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@Test
	void loadsSingleMessage() {
		final Optional<Message> loadedMessageOption = underTest.findById(msg1.getId());
		assertThat(loadedMessageOption).isPresent();
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
		assertThat(actual).hasSize(2);
		assertThat(actual.get(0).getId()).isEqualTo(msg1.getId());
		assertThat(actual.get(1).getId()).isEqualTo(msg3.getId());
	}

	@Test
	void findsMessageBySenderId() {
		final List<Message> actual = underTest.findAllBySenderId(user1.getId());
		assertThat(actual).hasSize(2);
		assertThat(actual.get(0).getId()).isEqualTo(msg1.getId());
		assertThat(actual.get(1).getId()).isEqualTo(msg3.getId());
	}

	@Test
	void findsBySenderName() {
		final List<Message> actual = underTest.findAllBySenderName("user1");
		assertThat(actual).hasSize(2);
		assertThat(actual.get(0).getId()).isEqualTo(msg1.getId());
		assertThat(actual.get(1).getId()).isEqualTo(msg3.getId());
	}

	@Test
	void findsBySenderIdAndContent() {
		var msg4 = new Message(user1, "to4", "another three", Instant.now());
		msg4 = underTest.save(msg4);
		final List<Message> actual = underTest.findAllBySenderIdAndContentContains(user1.getId(), "three");
		assertThat(actual).hasSize(2);
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
		final Message newMessage = new Message(user1, "to1", "content_new", Instant.now());
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
		assertThat(actual).hasSize(12); // 2 from above and 10 from here
		actual.stream().map(Message::getTimestamp).reduce(Instant.EPOCH, (previous, current) -> {
			assertThat(previous).isBeforeOrEqualTo(current);
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
		assertThat(actual).hasSize(12); // 2 from above and 10 from here
		actual.stream().map(Message::getTimestamp).reduce(Instant.MAX, (previous, current) -> {
			assertThat(previous).isAfterOrEqualTo(current);
			return current;
		});
	}
}