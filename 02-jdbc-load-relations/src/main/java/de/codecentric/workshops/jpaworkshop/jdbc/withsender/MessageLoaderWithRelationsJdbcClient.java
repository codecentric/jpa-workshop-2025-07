package de.codecentric.workshops.jpaworkshop.jdbc.withsender;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class MessageLoaderWithRelationsJdbcClient {
	private static final RowMapper<Message> ROW_MAPPER = (rs, rowNum) -> new Message(
		rs.getLong("message_id"),
		new User(rs.getLong("user_id"), rs.getString("username")),
		rs.getString("receiver"),
		rs.getString("content")
	);
	private final JdbcClient jdbcClient;

	public MessageLoaderWithRelationsJdbcClient(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public Message loadMessage(long id) {
		return jdbcClient.sql("SELECT * FROM messages JOIN users ON sender_id = user_id WHERE message_id = ?")
			.param(id)
			.query(ROW_MAPPER)
			.single();
	}

	public List<Message> loadAllMessages() {
		return jdbcClient.sql("SELECT * FROM messages JOIN users ON sender_id = user_id")
			.query(ROW_MAPPER)
			.list();
	}
}
