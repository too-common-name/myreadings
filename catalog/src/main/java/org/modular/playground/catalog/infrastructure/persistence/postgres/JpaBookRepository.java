package org.modular.playground.catalog.infrastructure.persistence.postgres;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.NotFoundException;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.DomainPage;
import org.modular.playground.catalog.core.usecases.repositories.BookRepository;
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapper;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "jpa", enableIfMissing = false)
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
    BookMapper bookMapper;

    @Override
    public Book create(Book book) {
        LOGGER.debugf("JPA: Saving or updating book entity with ID: %s", book.getBookId());
        BookEntity entity = bookMapper.toEntity(book);
        entityManager.persist(entity);
        return bookMapper.toDomain(entity);
    }

    @Override
    public Book update(Book book) {
        LOGGER.debugf("JPA: Updating book entity with ID: %s", book.getBookId());
        if (book.getBookId() == null) {
            throw new IllegalArgumentException("Book ID cannot be null for update");
        }
        BookEntity entity = entityManager.find(BookEntity.class, book.getBookId());
        if (entity == null) {
            throw new NotFoundException("Book with ID " + book.getBookId() + " not found.");
        }
        bookMapper.updateEntityFromDomain(book, entity);
        return bookMapper.toDomain(entity);
    }

    @Override
    public Optional<Book> findById(UUID bookId) {
        LOGGER.debugf("JPA: Finding book entity by ID: %s", bookId);
        return Optional.ofNullable(entityManager.find(BookEntity.class, bookId))
                .map(bookMapper::toDomain);
    }

    @Override
    public List<Book> findByIds(List<UUID> bookIds) {
        LOGGER.debugf("JPA: Finding %d books by IDs", bookIds.size());
        if (bookIds == null || bookIds.isEmpty()) {
            return Collections.emptyList();
        }
        TypedQuery<BookEntity> query = entityManager.createQuery("SELECT b FROM BookEntity b WHERE b.bookId IN :ids",
                BookEntity.class);
        query.setParameter("ids", bookIds);
        return bookMapper.toDomainList(query.getResultList());
    }

    @Override
    public List<Book> findAll(String sort, String order, Integer limit) {
        LOGGER.debugf("JPA: Finding all book entities with params [sort: %s, order: %s, limit: %d]", sort, order,
                limit);
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
                .map(bookMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(UUID bookId) {
        LOGGER.debugf("JPA: Deleting book entity with ID: %s", bookId);
        return BookEntity.deleteById(bookId);
    }

    @Override
    public DomainPage<Book> searchBooks(String query, int page, int size, String sortBy, String sortOrder) {
        LOGGER.debugf("JPA: Searching book entities with query: '%s', page: %d, size: %d", query, page, size);
        String lowerCaseQuery = "%" + query.toLowerCase() + "%";
        String countJpqlString = "SELECT COUNT(b) FROM BookEntity b WHERE LOWER(b.title) LIKE :query OR LOWER(b.description) LIKE :query";
        StringBuilder contentJpql = new StringBuilder(
                "SELECT b FROM BookEntity b WHERE LOWER(b.title) LIKE :query OR LOWER(b.description) LIKE :query");

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
                .map(bookMapper::toDomain)
                .collect(Collectors.toList());

        LOGGER.debugf("JPA: Executing search count query: %s", countJpqlString);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpqlString, Long.class);
        countQuery.setParameter("query", lowerCaseQuery);
        long totalElements = countQuery.getSingleResult();

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new DomainPage<>(
                content,
                totalElements,
                totalPages,
                page,
                size,
                (page + 1) >= totalPages,
                page == 0);
    }
}