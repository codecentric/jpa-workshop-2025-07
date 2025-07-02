package de.codecentric.workshops.jpaworkshop.jpa.datatypes;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
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
			"""
					SELECT m FROM Message m
					WHERE sender.id = :id
					order by m.timestamp asc
				""", Message.class
		).setParameter("id", senderId).getResultList();
	}

	public List<Message> findAllBySenderIdJPQL(Long senderId, Sort sort) {
		final var queryBuilder = new StringBuilder("SELECT m FROM Message m WHERE sender.id = :id ");

		var needComma = false;
		if (sort.isSorted()) {
			queryBuilder.append(" ORDER BY ");

			for (Sort.Order order : sort) {
				if (needComma) {
					queryBuilder.append(" , ");
				}
				queryBuilder.append(order.getProperty()).append(" ").append(order.getDirection().name());
				needComma = true;
			}
		}

		return entityManager.createQuery(queryBuilder.toString()).setParameter("id", senderId).getResultList();
	}

	public List<Message> findAllBySenderIdCriteria(Long senderId, Sort sort) {
		final var criteriaBuilder = entityManager.getCriteriaBuilder();
		final var criteriaQuery = criteriaBuilder.createQuery(Message.class);

		final var m = criteriaQuery.from(Message.class);
		final var sender = m.join("sender", JoinType.INNER);

		criteriaQuery.select(m).where(criteriaBuilder.equal(sender.get("id"), senderId));

		if (sort.isSorted()) {
			final var orders = new ArrayList<Order>();
			for (Sort.Order order : sort) {
				orders.add(order.isAscending()
						   ? criteriaBuilder.asc(m.get(order.getProperty()))
						   : criteriaBuilder.desc(m.get(order.getProperty())));
			}
			criteriaQuery.orderBy(orders);
		}

		return entityManager.createQuery(criteriaQuery).getResultList();
	}
}
