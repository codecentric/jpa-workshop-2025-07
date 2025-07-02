package de.codecentric.workshops.jpaworkshop.jpa.loadwithrelations;

import java.util.List;

import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

@Service
public class MessageLoaderWithRelationsJpa {
	private final EntityManager entityManager;

	public MessageLoaderWithRelationsJpa(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Message loadMessage(long id) {
		return entityManager.find(Message.class, id);
	}

	public List<Message> loadAllMessages() {
		return entityManager.createQuery("select m from Message m", Message.class).getResultList();
	}

	List<Message> findAllBySender(User sender) {
		return findAllBySenderId(sender.getId());
	}

	List<Message> findAllBySenderId(long id) {
		return entityManager.createQuery("select m from Message m where m.sender.id = :id", Message.class)
			.setParameter("id", id)
			.getResultList();
	}

	public List<Message> findAllBySenderIdAndContentContains(long senderId, String content) {
		return entityManager.createQuery("""
				select m from Message m
				where m.sender.id = :senderId
				and m.content like :content
			""", Message.class)
			.setParameter("senderId", senderId)
			.setParameter("content", "%" + content + "%")
			.getResultList();
	}

	public List<Message> findAllBySenderName(String senderName) {
		return entityManager.createQuery("select m from Message m where m.sender.name = :senderName", Message.class)
			.setParameter("senderName", senderName)
			.getResultList();
	}

	public long countMessagesBySenderId(long senderId) {
		return entityManager
			.createQuery("select count(m) from Message m where m.sender.id = :id", Long.class)
			.setParameter("id", senderId)
			.getSingleResult();
	}

	public long countMessagesBySenderIdSql(long senderId) {
		return (long) entityManager
			.createNativeQuery("SELECT count(*) FROM messages WHERE sender_id = ?", Long.class)
			.setParameter(1, senderId)
			.getSingleResult();
	}
}
