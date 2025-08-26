package org.modular.playground.catalog.core.usecases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.DomainPage;
import org.modular.playground.catalog.web.dto.BookRequestDTO;
import org.modular.playground.catalog.web.dto.BookUpdateDTO;

public interface BookService {
    Book createBook(BookRequestDTO createBookRequestDTO);
    Optional<Book> getBookById(UUID bookId);
    List<Book> getBooksByIds(List<UUID> bookIds);
    List<Book> getAllBooks(String sort, String order, Integer limit);
    Optional<Book> updateBook(UUID bookId, BookUpdateDTO updateDTO);
    boolean deleteBookById(UUID bookId);
    DomainPage<Book> searchBooks(String query, int page, int size, String sortBy, String sortOrder);
}
