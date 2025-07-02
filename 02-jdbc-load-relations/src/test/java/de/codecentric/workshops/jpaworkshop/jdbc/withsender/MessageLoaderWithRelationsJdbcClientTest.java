package de.codecentric.workshops.jpaworkshop.jdbc.withsender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest
class MessageLoaderWithRelationsJdbcClientTest {
	@Autowired
	JdbcClient client;
	@Autowired
	MessageLoaderWithRelationsJdbcClient underTest;

	@BeforeEach
	void clearDb() {
		client.sql("DELETE from messages;").update();
		client.sql("DELETE from users;").update();
	}

	@Test
	void loadsSingleMessage() {
		client.sql("INSERT INTO users VALUES (42, 'user1')").update();
		client.sql("INSERT INTO messages VALUES (1, 42, 'to1', 'content1')").update();
		client.sql("INSERT INTO messages VALUES (2, 42, 'to1', 'content1')").update();
		final Message loadedMessage = underTest.loadMessage(1);
		assertThat(loadedMessage).usingRecursiveComparison()
			.isEqualTo(new Message(1, new User(42, "user1"), "to1", "content1"));
	}

	@Test
	void loadsAllMessages() {
		client.sql("INSERT INTO users VALUES (42, 'user1')").update();
		client.sql("INSERT INTO users VALUES (43, 'user2')").update();
		client.sql("INSERT INTO messages VALUES (1, 42, 'to1', 'content1')").update();
		client.sql("INSERT INTO messages VALUES (2, 43, '2to', '2content')").update();
		final List<Message> messages = underTest.loadAllMessages();
		Assertions.assertThat(messages).hasSize(2);
		assertThat(messages.get(0)).usingRecursiveComparison()
			.isEqualTo(new Message(1, new User(42, "user1"), "to1", "content1"));
		assertThat(messages.get(1)).usingRecursiveComparison()
			.isEqualTo(new Message(2, new User(43, "user2"), "2to", "2content"));
	}
}