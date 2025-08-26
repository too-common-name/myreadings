package org.modular.playground.readinglist.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import io.quarkus.arc.lookup.LookupUnlessProperty;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.persistence.TypedQuery;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.usecases.repositories.ReadingListRepository;
import org.modular.playground.readinglist.infrastructure.persistence.postgres.mapper.ReadingListMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "jpa", enableIfMissing = false)
public class JpaReadingListRepository implements ReadingListRepository {

    private static final Logger LOGGER = Logger.getLogger(JpaReadingListRepository.class);

    @Inject
    @PersistenceUnit("readinglist-db")
    EntityManager entityManager;

    @Inject
    ReadingListMapper mapper;

    @Override
    public ReadingList create(ReadingList list) {
        LOGGER.debugf("JPA: Creating reading list entity with ID: %s", list.getReadingListId());
        ReadingListEntity newEntity = mapper.toEntity(list);
        entityManager.persist(newEntity);
        return mapper.toDomain(newEntity);
    }

    @Override
    public ReadingList update(ReadingList list) {
        LOGGER.debugf("JPA: Merging reading list entity with ID: %s", list.getReadingListId());
        ReadingListEntity managedEntity = entityManager.find(ReadingListEntity.class, list.getReadingListId());

        if (managedEntity == null) {
            throw new IllegalArgumentException(
                    "ReadingList with ID " + list.getReadingListId() + " not found for update.");
        }
        mapper.updateEntityFromDomain(list, managedEntity);
        return mapper.toDomain(managedEntity);
    }

    @Override
    public Optional<ReadingList> findById(UUID readingListId) {
        LOGGER.debugf("JPA: Finding reading list entity by ID: %s", readingListId);
        return Optional.ofNullable(entityManager.find(ReadingListEntity.class, readingListId)).map(mapper::toDomain);
    }

    @Override
    public List<ReadingList> findByUserId(UUID userId) {
        LOGGER.debugf("JPA: Finding reading list entities for user ID: %s", userId);
        TypedQuery<ReadingListEntity> query = entityManager
                .createQuery("SELECT rl FROM ReadingListEntity rl WHERE rl.userId = :userId", ReadingListEntity.class);
        query.setParameter("userId", userId);
        return query.getResultList().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID readingListId) {
        LOGGER.debugf("JPA: Deleting reading list entity with ID: %s", readingListId);
        ReadingListEntity entity = entityManager.find(ReadingListEntity.class, readingListId);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public void addBookToReadingList(UUID readingListId, UUID bookId) {
        LOGGER.debugf("JPA: Adding book %s to list %s", bookId, readingListId);
        ReadingListEntity listEntity = entityManager.find(ReadingListEntity.class, readingListId);
        if (listEntity == null) {
            throw new IllegalArgumentException("ReadingList with ID " + readingListId + " not found.");
        }

        String jpqlCheck = "SELECT COUNT(i) FROM ReadingListItemEntity i WHERE i.id.readingListId = :listId AND i.id.bookId = :bookId";
        Long count = entityManager.createQuery(jpqlCheck, Long.class)
                .setParameter("listId", readingListId)
                .setParameter("bookId", bookId)
                .getSingleResult();

        if (count > 0) {
            return;
        }

        ReadingListItemEntity newItem = new ReadingListItemEntity();
        newItem.setId(new ReadingListItemId(readingListId, bookId));
        newItem.setReadingList(listEntity);

        listEntity.getItems().add(newItem);
        entityManager.merge(listEntity);
    }

    @Override
    public void removeBookFromReadingList(UUID readingListId, UUID bookId) {
        LOGGER.debugf("JPA: Removing book %s from list %s", bookId, readingListId);
        ReadingListItemId id = new ReadingListItemId(readingListId, bookId);
        ReadingListItemEntity item = entityManager.find(ReadingListItemEntity.class, id);
        if (item != null) {
            entityManager.remove(item);
        }
    }

    @Override
    public List<UUID> getBookIdsInReadingList(UUID readingListId) {
        LOGGER.debugf("JPA: Getting book IDs for list %s", readingListId);
        TypedQuery<UUID> query = entityManager.createQuery(
                "SELECT i.id.bookId FROM ReadingListItemEntity i WHERE i.id.readingListId = :listId", UUID.class);
        query.setParameter("listId", readingListId);
        return query.getResultList();
    }

    @Override
    public Optional<ReadingList> findReadingListContainingBookForUser(UUID userId, UUID bookId) {
        LOGGER.debugf("JPA: Finding if user %s has book %s in a list", userId, bookId);
        TypedQuery<ReadingListEntity> query = entityManager.createQuery(
                "SELECT rl FROM ReadingListEntity rl JOIN rl.items i WHERE rl.userId = :userId AND i.id.bookId = :bookId",
                ReadingListEntity.class);
        query.setParameter("userId", userId);
        query.setParameter("bookId", bookId);
        return query.getResultStream().findFirst().map(mapper::toDomain);
    }
}