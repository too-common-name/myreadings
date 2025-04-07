package modules.catalog.infrastructure.persistence.in_memory;

import jakarta.enterprise.context.ApplicationScoped;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.core.usecases.repositories.BookRepository;

import java.util.UUID;

import io.quarkus.arc.properties.IfBuildProperty;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
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
                .build();
        books.put(bookToSave.getBookId(), bookToSave);
        return bookToSave;
    }

    @Override
    public Optional<Book> findById(UUID bookId) {
        return Optional.ofNullable(books.get(bookId));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    @Override
    public void deleteById(UUID bookId) {
        books.remove(bookId);
    }
}