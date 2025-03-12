package modules.catalog.infrastructure;

import modules.catalog.domain.Book;
import jakarta.enterprise.context.ApplicationScoped; 
import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class InMemoryBookRepository implements BookRepository {

    private final Map<UUID, Book> books = new HashMap<>();

    @Override
    public Book save(Book book) {
        books.put(book.getBookId(), book);
        return book;
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