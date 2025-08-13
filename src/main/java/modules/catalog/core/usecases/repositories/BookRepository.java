package modules.catalog.core.usecases.repositories;

import java.util.UUID;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;

import java.util.Optional;
import java.util.List;

public interface BookRepository {

    Book create(Book book);
    Book update(Book book);
    Optional<Book> findById(UUID bookId);
    List<Book> findByIds(List<UUID> bookIds);
    List<Book> findAll(String sort, String order, Integer limit);
    boolean deleteById(UUID bookId);
    DomainPage<Book> searchBooks(String query, int page, int size, String sortBy, String sortOrder);
}