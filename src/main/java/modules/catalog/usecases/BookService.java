package modules.catalog.usecases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import modules.catalog.domain.Book;

public interface BookService {
    Book createBook(Book book);
    Optional<Book> getBookById(UUID bookId);
    List<Book> getAllBooks();
    Book updateBook(Book book);
    void deleteBookById(UUID bookId);
}
