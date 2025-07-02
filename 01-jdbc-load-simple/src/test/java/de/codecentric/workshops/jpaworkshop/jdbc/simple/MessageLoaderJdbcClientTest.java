package de.codecentric.workshops.jpaworkshop.jdbc.simple;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class MessageLoaderJdbcClientTest {
	@Autowired
	JdbcTemplate template;
	@Autowired
	MessageLoaderJdbcClient loader;

	@BeforeEach
	void clearDb() {
		template.update("DELETE from messages;");
	}

	@Test
	void loadsSingleMessage() {
		template.update("INSERT INTO messages VALUES (1, 'from1', 'to1', 'content1')");
		final Message loadedMessage = loader.loadMessage(1);
		assertThat(loadedMessage).usingRecursiveComparison().isEqualTo(new Message(1, "from1", "to1", "content1"));
	}

	@Test
	void loadsAllMessages() {
		template.update("INSERT INTO messages VALUES (1, 'from1', 'to1', 'content1')");
		template.update("INSERT INTO messages VALUES (2, '2from', '2to', '2content')");
		final List<Message> messages = loader.loadAllMessages();
		Assertions.assertThat(messages).hasSize(2);
		assertThat(messages.get(0)).usingRecursiveComparison().isEqualTo(new Message(1, "from1", "to1", "content1"));
		assertThat(messages.get(1)).usingRecursiveComparison().isEqualTo(new Message(2, "2from", "2to", "2content"));
	}
}