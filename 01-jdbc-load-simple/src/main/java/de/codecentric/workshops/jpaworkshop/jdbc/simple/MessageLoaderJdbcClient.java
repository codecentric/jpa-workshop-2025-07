package de.codecentric.workshops.jpaworkshop.jdbc.simple;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class MessageLoaderJdbcClient {
	private final JdbcClient jdbcClient;

	public MessageLoaderJdbcClient(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public Message loadMessage(long id) {
		return jdbcClient.sql("SELECT * FROM messages WHERE message_id = ?").param(id).query(Message.class).single();
	}

	public List<Message> loadAllMessages() {
		return jdbcClient.sql("SELECT * FROM messages").query(Message.class).list();
	}
}
