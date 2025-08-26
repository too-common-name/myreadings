package org.modular.playground.catalog.infrastructure.persistence.in_memory;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.BookImpl;
import org.modular.playground.catalog.core.domain.DomainPage;
import org.modular.playground.catalog.core.usecases.repositories.BookRepository;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "in-memory", enableIfMissing = true)
public class InMemoryBookRepository implements BookRepository {

    private static final Logger LOGGER = Logger.getLogger(InMemoryBookRepository.class);
    private final Map<UUID, Book> books = new HashMap<>();

    @Override
    public Book create(Book book) {
        LOGGER.debugf("In-memory: Creating book with ISBN: %s", book.getIsbn());
        UUID bookId = UUID.randomUUID();

        Book bookToSave = BookImpl.builder()
                .bookId(bookId)
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .authors(book.getAuthors())
                .publicationDate(book.getPublicationDate())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .pageCount(book.getPageCount())
                .coverImageId(book.getCoverImageId())
                .originalLanguage(book.getOriginalLanguage())
                .genre(book.getGenre())
                .build();

        books.put(bookToSave.getBookId(), bookToSave);
        return bookToSave;
    }

    @Override
    public Book update(Book book) {
        LOGGER.debugf("In-memory: Updating book with ID: %s", book.getBookId());
        if (book.getBookId() == null || !books.containsKey(book.getBookId())) {
            throw new IllegalArgumentException("Book with ID " + book.getBookId() + " not found for update.");
        }
        books.put(book.getBookId(), book);
        return book;
    }

    @Override
    public Optional<Book> findById(UUID bookId) {
        LOGGER.debugf("In-memory: Finding book by ID: %s", bookId);
        return Optional.ofNullable(books.get(bookId));
    }

    @Override
    public List<Book> findByIds(List<UUID> bookIds) {
        LOGGER.debugf("In-memory: Finding %d books by IDs", bookIds.size());
        if (bookIds == null || bookIds.isEmpty()) {
            return Collections.emptyList();
        }
        return bookIds.stream()
                .map(books::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findAll(String sort, String order, Integer limit) {
        LOGGER.debugf("In-memory: Finding all books with params [sort: %s, order: %s, limit: %d]", sort, order, limit);
        Stream<Book> bookStream = books.values().stream();

        if (sort != null && !sort.trim().isEmpty()) {
            Comparator<Book> comparator = getBookComparator(sort);
            if (comparator != null) {
                if ("desc".equalsIgnoreCase(order)) {
                    comparator = comparator.reversed();
                }
                bookStream = bookStream.sorted(comparator);
            } else {
                LOGGER.warnf("In-memory: Invalid sort field provided for findAll: %s", sort);
            }
        }

        if (limit != null && limit > 0) {
            bookStream = bookStream.limit(limit);
        }

        return bookStream.collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(UUID bookId) {
        LOGGER.debugf("In-memory: Deleting book with ID: %s", bookId);
        return books.remove(bookId) != null;
    }

    @Override
    public DomainPage<Book> searchBooks(String query, int page, int size, String sortBy, String sortOrder) {
        LOGGER.debugf("In-memory: Searching books with query: '%s', page: %d, size: %d", query, page, size);
        String lowerCaseQuery = query.toLowerCase();

        Stream<Book> filteredStream = books.values().stream()
                .filter(book -> (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerCaseQuery)) ||
                        (book.getDescription() != null
                                && book.getDescription().toLowerCase().contains(lowerCaseQuery)));

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            Comparator<Book> comparator = getBookComparator(sortBy);
            if (comparator != null) {
                if ("desc".equalsIgnoreCase(sortOrder)) {
                    comparator = comparator.reversed();
                }
                filteredStream = filteredStream.sorted(comparator);
            } else {
                LOGGER.warnf("In-memory: Invalid sort field provided for search: %s", sortBy);
            }
        }

        List<Book> allFilteredBooks = filteredStream.collect(Collectors.toList());
        
        return DomainPage.of(allFilteredBooks, page, size);
    }

    private Comparator<Book> getBookComparator(String sortField) {
        switch (sortField.toLowerCase()) {
            case "publicationdate":
                return Comparator.comparing(Book::getPublicationDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "title":
                return Comparator.comparing(Book::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            default:
                return null;
        }
    }
}