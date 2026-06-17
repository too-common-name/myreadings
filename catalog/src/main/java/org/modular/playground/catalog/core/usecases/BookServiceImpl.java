package org.modular.playground.catalog.core.usecases;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.BookImpl;
import org.modular.playground.catalog.core.domain.DomainPage;
import org.modular.playground.catalog.core.usecases.repositories.BookRepository;
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapper;
import org.modular.playground.catalog.web.dto.BookRequestDTO;
import org.modular.playground.catalog.web.dto.BookUpdateDTO;

import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class BookServiceImpl implements BookService {

    private static final Logger LOGGER = Logger.getLogger(BookServiceImpl.class);

    @Inject
    BookRepository bookRepository;

    @Inject
    BookMapper bookMapper;

    @ConfigProperty(name = "app.search.enrichment-strategy", defaultValue = "normal")
    String searchEnrichmentStrategy;

    @Override
    @Transactional
    public Book createBook(BookRequestDTO createBookRequestDTO) {
        LOGGER.info("Starting creation process for a new book.");
        Book book = bookMapper.toDomain(createBookRequestDTO);
        LOGGER.debugf("Domain object 'Book' created, ready for persistence");
        Book savedBook = bookRepository.create(book);
        LOGGER.infof("Book saved to repository with ID: %s", savedBook.getBookId());
        return savedBook;
    }

    @Override
    public Optional<Book> getBookById(UUID bookId) {
        LOGGER.debugf("Searching for book by ID: %s", bookId);
        return bookRepository.findById(bookId);
    }

    @Override
    public List<Book> getAllBooks(String sort, String order, Integer limit) {
        LOGGER.debugf("Passing 'getAllBooks' request to repository with params [sort: %s, order: %s, limit: %s]",
                sort, order, limit);
        return bookRepository.findAll(sort, order, limit);
    }

    @Override
    @Transactional
    public Optional<Book> updateBook(UUID bookId, BookUpdateDTO updateDTO) {
        LOGGER.infof("Starting update process for book with ID: %s", bookId);
        Optional<Book> existingBookOpt = bookRepository.findById(bookId);
        if (existingBookOpt.isEmpty()) {
            LOGGER.warnf("Book with ID: %s not found. Update failed.", bookId);
            return Optional.empty();
        }
        BookImpl bookToUpdate = (BookImpl) existingBookOpt.get();
        LOGGER.debugf("Book found, applying updates...");
        bookMapper.updateDomainFromDto(updateDTO, bookToUpdate);
        Book updatedBook = bookRepository.update(bookToUpdate);
        LOGGER.infof("Book with ID: %s updated successfully.", updatedBook.getBookId());
        return Optional.of(updatedBook);
    }

    @Override
    @Transactional
    public boolean deleteBookById(UUID bookId) {
        LOGGER.infof("Deleting book with ID: %s", bookId);
        return bookRepository.deleteById(bookId);
    }

    @Override
    @WithSpan("catalog.searchBooks")
    public DomainPage<Book> searchBooks(String query, int page, int size, String sortBy, String sortOrder) {
        LOGGER.debugf("Searching books with query: '%s' (strategy: %s)", query, searchEnrichmentStrategy);
        DomainPage<Book> results = bookRepository.searchBooks(query, page, size, sortBy, sortOrder);

        if ("broken".equals(searchEnrichmentStrategy) && !results.content().isEmpty()) {
            return refetchResultsIndividually(results);
        }

        return results;
    }

    @WithSpan("catalog.searchBooks.refetchBroken")
    private DomainPage<Book> refetchResultsIndividually(DomainPage<Book> results) {
        List<Book> refetched = results.content().stream()
            .map(book -> bookRepository.findById(book.getBookId()).orElse(book))
            .collect(Collectors.toList());
        return new DomainPage<>(refetched, results.totalElements(), results.totalPages(),
                results.pageNumber(), results.pageSize(), results.isLast(), results.isFirst());
    }

    @Override
    public List<Book> getBooksByIds(List<UUID> bookIds) {
        LOGGER.debugf("Searching for %d books by IDs", bookIds.size());
        return bookRepository.findByIds(bookIds);
    }
}
