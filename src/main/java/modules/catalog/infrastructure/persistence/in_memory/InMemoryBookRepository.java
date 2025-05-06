package modules.catalog.infrastructure.persistence.in_memory;

import jakarta.enterprise.context.ApplicationScoped;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.core.usecases.repositories.BookRepository;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.arc.properties.IfBuildProperty;

import java.util.Optional;
import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "in-memory", enableIfMissing = true)
public class InMemoryBookRepository implements BookRepository {

    private final Map<UUID, Book> books = new HashMap<>();

    @Override
    public Book save(Book book) {
        Book bookToSave = BookImpl.builder()
                .bookId(UUID.randomUUID())
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
    public Optional<Book> findById(UUID bookId) {
        return Optional.ofNullable(books.get(bookId));
    }

    @Override
    public List<Book> findAll(String sort, String order, Integer limit) {

        Stream<Book> bookStream = books.values().stream();

        if (sort != null && !sort.trim().isEmpty()) {
            Comparator<Book> comparator = null;
            switch (sort.toLowerCase()) {
                case "publicationdate":
                    comparator = Comparator.comparing(Book::getPublicationDate,
                            Comparator.nullsLast(Comparator.naturalOrder()));
                    break;

                default:
                    System.err.println("Warning: Invalid sort field provided for in-memory repository: " + sort
                            + ". Only 'publicationDate' is supported.");
                    break;
            }

            if (comparator != null) {
                if (order != null && order.equalsIgnoreCase("desc")) {
                    comparator = comparator.reversed();
                }
                bookStream = bookStream.sorted(comparator);
            }
        }

        if (limit != null && limit > 0) {
            bookStream = bookStream.limit(limit);
        }

        return bookStream.collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID bookId) {
        books.remove(bookId);
    }
}