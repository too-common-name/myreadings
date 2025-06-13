package modules.readinglist.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.persistence.TypedQuery;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
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
            throw new IllegalArgumentException("ReadingList with ID " + list.getReadingListId() + " not found for update.");
        }
    }

    @Override
    public Optional<ReadingList> findById(UUID readingListId) {
        return Optional.ofNullable(entityManager.find(ReadingListEntity.class, readingListId))
                .map(mapper::toDomain);
    }

    @Override
    public List<ReadingList> findByUserId(UUID userId) {
        String jpql = "SELECT r FROM ReadingListEntity r WHERE r.userId = :userId";
        TypedQuery<ReadingListEntity> query = entityManager.createQuery(jpql, ReadingListEntity.class);
        query.setParameter("userId", userId);
        return query.getResultList().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID readingListId) {
        Optional.ofNullable(entityManager.find(ReadingListEntity.class, readingListId))
                .ifPresent(entityManager::remove);
    }

    @Override
    public void addBookToReadingList(UUID readingListId, Book book) {
        ReadingListEntity listEntity = entityManager.find(ReadingListEntity.class, readingListId);
        if (listEntity == null) {
            throw new IllegalArgumentException("ReadingList with ID " + readingListId + " not found.");
        }

        ReadingListItemEntity newItem = new ReadingListItemEntity();
        newItem.setId(new ReadingListItemId(readingListId, book.getBookId()));
        newItem.setReadingList(listEntity);

        listEntity.getItems().add(newItem);
    }

    @Override
    public void removeBookFromReadingList(UUID readingListId, UUID bookId) {
        ReadingListEntity listEntity = entityManager.find(ReadingListEntity.class, readingListId);
        if (listEntity != null) {
            listEntity.getItems().removeIf(item -> item.getId().getBookId().equals(bookId));
        }
    }

    @Override
    public List<Book> getBooksInReadingList(UUID readingListId) {
        String jpql = "SELECT i.id.bookId FROM ReadingListItemEntity i WHERE i.readingList.id = :readingListId";
        TypedQuery<UUID> query = entityManager.createQuery(jpql, UUID.class);
        query.setParameter("readingListId", readingListId);

        return query.getResultList().stream()
                .map(bookId -> BookImpl.builder().bookId(bookId).build())
                .collect(Collectors.toList());
    }
}