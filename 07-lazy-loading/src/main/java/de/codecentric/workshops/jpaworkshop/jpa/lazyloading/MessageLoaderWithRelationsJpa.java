package de.codecentric.workshops.jpaworkshop.jpa.lazyloading;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.domain.Sort;
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
		return entityManager.createQuery("SELECT m FROM Message m", Message.class).getResultList();
	}

	List<Message> findAllBySender(User sender) {
		return findAllBySenderIdJPQL(sender.getId());
	}

	List<Message> findAllBySenderIdJPQL(long id) {
		return entityManager.createQuery("SELECT m FROM Message m WHERE sender.id = :id", Message.class)
			.setParameter("id", id)
			.getResultList();
	}

	public List<Message> findAllBySenderIdAndContentContains(long senderId, String content) {
		return entityManager.createQuery(
				"SELECT m FROM Message m where sender.id = :id and m.content like :content",
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

	public List<Message> findAllBySenderIdOrderByTimestamp(Long senderId) {
		return entityManager.createQuery(
				"SELECT m FROM Message m WHERE sender.id = :id ORDER BY m.timestamp",
				Message.class
			)
			.setParameter("id", senderId)
			.getResultList();
	}

	public List<Message> findAllBySenderIdJPQL(Long senderId, Sort sort) {
		final StringBuilder queryString = new StringBuilder("SELECT m FROM Message m WHERE sender.id = :id");
		if (sort.isSorted()) {
			queryString.append(" ORDER BY ");
			queryString.append(sort.stream()
				// property is potentially unsafe -> check!
				.map(order -> "%s %s".formatted(order.getProperty(), order.getDirection()))
				.collect(Collectors.joining(",")));
		}
		return entityManager.createQuery(queryString.toString(), Message.class)
			.setParameter("id", senderId)
			.getResultList();
	}

	public List<Message> findAllBySenderIdCriteria(Long senderId, Sort sort) {
		final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Message> cq = cb.createQuery(Message.class);
		Root<Message> fromMessage = cq.from(Message.class);
		final Join<Message, User> sender = fromMessage.join("sender", JoinType.LEFT);
		cq.select(fromMessage).where(cb.equal(sender.get("id"), senderId));
		if (sort.isSorted()) {
			cq.orderBy(sort.stream().map(order -> {
				if (order.isAscending()) {
					return cb.asc(fromMessage.get(order.getProperty()));
				} else {
					return cb.desc(fromMessage.get(order.getProperty()));
				}
			}).toList());
		}
		return entityManager.createQuery(cq).getResultList();
	}

	@Transactional(TxType.REQUIRES_NEW)
	public Message findWithFetch(final Long id) {
		return entityManager.createQuery(
			"""
					select m
					from Message m
						join fetch m.sender
						join fetch m.sender.address
					where m.id = :id
				""", Message.class
		).setParameter("id", id).getSingleResult();
	}
}
