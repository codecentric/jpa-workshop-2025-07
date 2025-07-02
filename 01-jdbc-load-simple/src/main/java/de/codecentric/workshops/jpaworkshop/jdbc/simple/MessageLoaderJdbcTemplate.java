package de.codecentric.workshops.jpaworkshop.jdbc.simple;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageLoaderJdbcTemplate {
	private final JdbcTemplate jdbcTemplate;

	public MessageLoaderJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Message loadMessage(long id) {
		var resultList = this.jdbcTemplate.query(
			"SELECT * FROM messages WHERE message_id = ?",
			(rs, rowNum) -> new Message(
				rs.getLong("message_id"),
				rs.getString("sender"),
				rs.getString("receiver"),
				rs.getString("content")
			),
			id
		);

		if (resultList.size() != 1) {
			throw new RuntimeException("Expected 1 row but got " + resultList.size());
		}

		return resultList.get(0);
	}

	public List<Message> loadAllMessages() {
		return this.jdbcTemplate.query(
			"SELECT * FROM messages",
			(rs, rowNum) -> new Message(
				rs.getLong("message_id"),
				rs.getString("sender"),
				rs.getString("receiver"),
				rs.getString("content")
			)
		);
	}
}
