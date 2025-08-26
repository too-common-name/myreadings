package org.modular.playground.catalog.utils;

import java.util.UUID;

import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.usecases.repositories.BookRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CatalogRepositoryUtils {
    
    @Inject
    BookRepository bookRepository;

    @Transactional
    public Book saveBook(Book book) {
        return bookRepository.create(book);
    }

    @Transactional
    public void deleteBook(UUID id) {
        bookRepository.deleteById(id);
    }
}
