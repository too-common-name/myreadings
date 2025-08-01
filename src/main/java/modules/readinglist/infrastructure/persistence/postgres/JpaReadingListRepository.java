package modules.readinglist.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.persistence.TypedQuery;
import modules.catalog.core.domain.Book;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@IfBuildProperty(name = "app.book.repository.type", stringValue = "jpa", enableIfMissing = true)
public class JpaReadingListRepository implements ReadingListRepository {

    @Inject
    @PersistenceUnit("readinglist-db")
    EntityManager entityManager;

    @Inject
    ReadingListPersistenceMapper mapper;

    @Override
    public ReadingList create(ReadingList list) {
        ReadingListEntity newEntity = mapper.toEntity(list);
        entityManager.persist(newEntity);
        return mapper.toDomain(newEntity);
    }

    @Override
    public ReadingList update(ReadingList list) {
        ReadingListEntity managedEntity = entityManager.find(ReadingListEntity.class, list.getReadingListId());

        if (managedEntity != null) {
            mapper.updateEntityFromDomain(managedEntity, list);
            return mapper.toDomain(managedEntity);
        } else {
            throw new IllegalArgumentException(
                    "ReadingList with ID " + list.getReadingListId() + " not found for update.");
        }
    }

    @Override
    public Optional<ReadingList> findById(UUID readingListId) {
        String jpql = "SELECT DISTINCT rl FROM ReadingListEntity rl LEFT JOIN FETCH rl.items i WHERE rl.id = :readingListId";
        TypedQuery<ReadingListEntity> query = entityManager.createQuery(jpql, ReadingListEntity.class);
        query.setParameter("readingListId", readingListId);
        return query.getResultList().stream().findFirst().map(mapper::toDomain);
    }

    @Override
    public List<ReadingList> findByUserId(UUID userId) {
        String jpql = "SELECT DISTINCT rl FROM ReadingListEntity rl LEFT JOIN FETCH rl.items i WHERE rl.userId = :userId";
        TypedQuery<ReadingListEntity> query = entityManager.createQuery(jpql, ReadingListEntity.class);
        query.setParameter("userId", userId);
        return query.getResultList().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID readingListId) {
        entityManager.createQuery("DELETE FROM ReadingListItemEntity i WHERE i.id.readingListId = :listId")
                .setParameter("listId", readingListId)
                .executeUpdate();

        Optional.ofNullable(entityManager.find(ReadingListEntity.class, readingListId))
                .ifPresent(entityManager::remove);
    }

    @Override
    public void addBookToReadingList(UUID readingListId, Book book) {
        ReadingListEntity listEntity = entityManager.find(ReadingListEntity.class, readingListId);
        if (listEntity == null) {
            throw new IllegalArgumentException("ReadingList with ID " + readingListId + " not found.");
        }

        String jpqlCheck = "SELECT COUNT(i) FROM ReadingListItemEntity i WHERE i.id.readingListId = :listId AND i.id.bookId = :bookId";
        Long count = entityManager.createQuery(jpqlCheck, Long.class)
                .setParameter("listId", readingListId)
                .setParameter("bookId", book.getBookId())
                .getSingleResult();

        if (count > 0) {
            return;
        }

        ReadingListItemEntity newItem = new ReadingListItemEntity();
        newItem.setId(new ReadingListItemId(readingListId, book.getBookId()));
        newItem.setReadingList(listEntity);

        listEntity.getItems().add(newItem);
        entityManager.merge(listEntity);
    }

    @Override
    public void removeBookFromReadingList(UUID readingListId, UUID bookId) {
        int deletedCount = entityManager.createQuery(
                "DELETE FROM ReadingListItemEntity i WHERE i.id.readingListId = :listId AND i.id.bookId = :bookId")
                .setParameter("listId", readingListId)
                .setParameter("bookId", bookId)
                .executeUpdate();

        if (deletedCount == 0) {
            throw new IllegalArgumentException(
                    "Book with ID " + bookId + " not found in reading list " + readingListId + ".");
        }
    }

    @Override
    public List<Book> getBooksInReadingList(UUID readingListId) {
        String jpql = "SELECT i.id.bookId FROM ReadingListItemEntity i WHERE i.readingList.id = :readingListId";
        TypedQuery<UUID> query = entityManager.createQuery(jpql, UUID.class);
        query.setParameter("readingListId", readingListId);
        List<Book> all = query.getResultList().stream()
                .map(bookId -> modules.catalog.core.domain.BookImpl.builder().bookId(bookId).build())
                .collect(Collectors.toList());
        return all;
    }

    @Override
    public Optional<ReadingList> findReadingListContainingBookForUser(UUID userId, UUID bookId) {
        String jpql = "SELECT DISTINCT rl FROM ReadingListEntity rl " +
                "JOIN FETCH rl.items i " +
                "WHERE rl.userId = :userId AND i.id.bookId = :bookId";
        TypedQuery<ReadingListEntity> query = entityManager.createQuery(jpql, ReadingListEntity.class);
        query.setParameter("userId", userId);
        query.setParameter("bookId", bookId);

        return query.getResultList().stream().findFirst().map(mapper::toDomain);
    }

    public List<ReadingList> findAll() {
        // Query era già giusta
        String jpql = "SELECT DISTINCT rl FROM ReadingListEntity rl LEFT JOIN FETCH rl.items i";
        TypedQuery<ReadingListEntity> query = entityManager.createQuery(jpql, ReadingListEntity.class);
        return query.getResultList().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}