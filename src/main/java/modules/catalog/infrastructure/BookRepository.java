package modules.catalog.infrastructure;

import modules.catalog.domain.Book;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface BookRepository {

    Book save(Book book);
    Optional<Book> findById(UUID bookId);
    List<Book> findAll();
    void deleteById(UUID bookId);
    
}