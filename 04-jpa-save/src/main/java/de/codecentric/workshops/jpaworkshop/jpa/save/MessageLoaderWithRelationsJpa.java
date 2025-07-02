package de.codecentric.workshops.jpaworkshop.jpa.save;

import java.util.List;

import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		return entityManager.createQuery("SELECT m FROM Message m", Message.class).getResultList();
	}

	List<Message> findAllBySender(User sender) {
		return findAllBySenderId(sender.getId());
	}

	List<Message> findAllBySenderId(long id) {
		return entityManager.createQuery("SELECT m FROM Message m WHERE sender.id = :id", Message.class)
			.setParameter("id", id)
			.getResultList();
	}

	public List<Message> findAllBySenderIdAndContentContains(long senderId, String content) {
		return entityManager.createQuery("SELECT m FROM Message m where sender.id = :id and m.content like :content",
				Message.class
			)
			.setParameter("id", senderId)
			.setParameter("content", "%" + content + "%")
			.getResultList();
	}

	public List<Message> findAllBySenderName(String senderName) {
		return entityManager.createQuery("SELECT m FROM Message m where sender.name = :name", Message.class)
			.setParameter("name", senderName)
			.getResultList();
	}

	public long countMessagesBySenderId(long senderId) {
		return entityManager.createQuery("SELECT count(m) from Message m where sender.id = :id", long.class)
			.setParameter("id", senderId)
			.getSingleResult();
	}

	@Transactional
	public Message save(Message newMessage) {
		entityManager.persist(newMessage);
		return newMessage;
	}
}
