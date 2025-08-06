package modules.catalog.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.core.usecases.repositories.BookRepository;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.PersistenceUnit;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@IfBuildProperty(name = "app.book.repository.type", stringValue = "jpa", enableIfMissing = true)
public class JpaBookRepository implements BookRepository {

    private static final Logger LOGGER = Logger.getLogger(JpaBookRepository.class);
    private static final Map<String, String> SORTABLE_FIELDS = new HashMap<>();
    static {
        SORTABLE_FIELDS.put("publicationdate", "b.publicationDate");
        SORTABLE_FIELDS.put("title", "b.title");
    }

    @Inject
    @PersistenceUnit("books-db")
    EntityManager entityManager;

    @Inject
    BookPersistenceMapper mapper;

    @Override
    public Book save(Book book) {
        LOGGER.debugf("JPA: Persisting entity for book ID: %s", book.getBookId());
        BookEntity entity = mapper.toEntity(book);
        if (entity.getBookId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
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
            String validatedSortField = SORTABLE_FIELDS.get(sort.toLowerCase());
            if (validatedSortField != null) {
                jpql.append(" ORDER BY ").append(validatedSortField);
                jpql.append("desc".equalsIgnoreCase(order) ? " DESC" : " ASC");
            } else {
                LOGGER.warnf("JPA: Invalid sort field provided for findAll: %s", sort);
            }
        }
        
        LOGGER.debugf("JPA: Executing findAll query: %s", jpql.toString());
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
        LOGGER.debugf("JPA: Deleting entity for book ID: %s", bookId);
        Optional.ofNullable(entityManager.find(BookEntity.class, bookId))
                .ifPresent(entityManager::remove);
    }

    @Override
    public DomainPage<Book> searchBooks(String query, int page, int size, String sortBy, String sortOrder) {
        String lowerCaseQuery = "%" + query.toLowerCase() + "%";
        String countJpqlString = "SELECT COUNT(b) FROM BookEntity b WHERE LOWER(b.title) LIKE :query OR LOWER(b.description) LIKE :query";
        StringBuilder contentJpql = new StringBuilder("SELECT b FROM BookEntity b WHERE LOWER(b.title) LIKE :query OR LOWER(b.description) LIKE :query");

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            String validatedSortField = SORTABLE_FIELDS.get(sortBy.toLowerCase());
            if (validatedSortField != null) {
                contentJpql.append(" ORDER BY ").append(validatedSortField);
                contentJpql.append("desc".equalsIgnoreCase(sortOrder) ? " DESC" : " ASC");
            } else {
                LOGGER.warnf("JPA: Invalid sort field provided for search: %s", sortBy);
            }
        }

        LOGGER.debugf("JPA: Executing search content query: %s", contentJpql.toString());
        TypedQuery<BookEntity> contentQuery = entityManager.createQuery(contentJpql.toString(), BookEntity.class);
        contentQuery.setParameter("query", lowerCaseQuery);
        contentQuery.setFirstResult(page * size);
        contentQuery.setMaxResults(size);

        List<Book> content = contentQuery.getResultList().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        LOGGER.debugf("JPA: Executing search count query: %s", countJpqlString);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpqlString, Long.class);
        countQuery.setParameter("query", lowerCaseQuery);
        long totalElements = countQuery.getSingleResult();

        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isLast = (page + 1) * size >= totalElements;
        boolean isFirst = page == 0;

        return new DomainPage<>(
                content,
                totalElements,
                totalPages,
                page,
                size,
                isLast,
                isFirst);
    }
}