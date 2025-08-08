package modules.catalog.core.usecases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.web.dto.BookRequestDTO;

public interface BookService {
    Book createBook(BookRequestDTO createBookRequestDTO);
    Optional<Book> getBookById(UUID bookId);
    List<Book> getBooksByIds(List<UUID> bookIds);
    List<Book> getAllBooks(String sort, String order, Integer limit);
    Book updateBook(Book book);
    void deleteBookById(UUID bookId);
    DomainPage<Book> searchBooks(String query, int page, int size, String sortBy, String sortOrder);
}
