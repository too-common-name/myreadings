package modules.catalog.core.usecases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import modules.catalog.core.domain.Book;

public interface BookService {
    Book createBook(Book book);
    Optional<Book> getBookById(UUID bookId);
    List<Book> getAllBooks(String sort, String order, Integer limit);
    Book updateBook(Book book);
    void deleteBookById(UUID bookId);
}
