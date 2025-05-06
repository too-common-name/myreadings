package modules.catalog.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.repositories.BookRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.PersistenceUnit;

@ApplicationScoped
@Transactional
@IfBuildProperty(name = "app.book.repository.type", stringValue = "jpa", enableIfMissing = true)
public class JpaBookRepository implements BookRepository {

    @Inject
    @PersistenceUnit("books-db")
    EntityManager entityManager;

    @Inject
    BookPersistenceMapper mapper;

    @Override
    public Book save(Book book) {
        BookEntity entity = mapper.toEntity(book);
        entityManager.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Book> findById(UUID bookId) {
        return Optional.ofNullable(entityManager.find(BookEntity.class, bookId))
                .map(mapper::toDomain);
    }

    @Override
    public List<Book> findAll(String sort, String order, Integer limit) {
        StringBuilder jpql = new StringBuilder("SELECT b FROM BookEntity b");

        if (sort != null && !sort.trim().isEmpty()) {
            String validatedSortField = null;
            switch (sort.toLowerCase()) {
                case "publicationdate":
                    validatedSortField = "b.publicationDate";
                    break;
                default:
                    System.err.println("Warning: Invalid sort field provided for JPA repository: " + sort + ". Only 'publicationDate' is supported.");
                    break;
            }

            if (validatedSortField != null) {
                jpql.append(" ORDER BY ").append(validatedSortField);
                if (order != null && order.equalsIgnoreCase("desc")) {
                    jpql.append(" DESC"); 
                } else {
                    jpql.append(" ASC");
                }
            }
        }

        TypedQuery<BookEntity> query = entityManager.createQuery(jpql.toString(), BookEntity.class);

        if (limit != null && limit > 0) {
            query.setMaxResults(limit); 
        }

        return query.getResultList().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID bookId) {
        Optional.ofNullable(entityManager.find(BookEntity.class, bookId))
                .ifPresent(entityManager::remove);
    }
}