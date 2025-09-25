package org.modular.playground.catalog.web.graphql;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.eclipse.microprofile.graphql.*;
import org.jboss.logging.Logger;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.DomainPage;
import org.modular.playground.catalog.core.usecases.BookService;
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapper;
import org.modular.playground.catalog.web.dto.BookRequestDTO;
import org.modular.playground.catalog.web.dto.BookResponseDTO;
import org.modular.playground.catalog.web.dto.BookUpdateDTO;

import io.quarkus.security.Authenticated;

import java.util.List;
import java.util.UUID;

@GraphQLApi
@Authenticated
public class BookGraphQLController {

    private static final Logger LOGGER = Logger.getLogger(BookGraphQLController.class);

    @Inject
    BookService bookService;

    @Inject
    BookMapper bookMapper;

    @Query("bookById")
    @Description("Gets a single book by its unique ID.")
    @RolesAllowed({ "user", "admin" })
    public BookResponseDTO getBookById(@Name("bookId") UUID bookId) {
        LOGGER.infof("GraphQL request for book by ID: %s", bookId);
        return bookService.getBookById(bookId)
                .map(bookMapper::toResponseDTO)
                .orElse(null);
    }

    @Query("allBooks")
    @Description("Gets a list of all books with simple sorting and limiting.")
    @RolesAllowed({ "user", "admin" })
    public List<BookResponseDTO> getAllBooks(
            @Name("sort") String sort,
            @Name("order") String order,
            @Name("limit") @DefaultValue("25") int limit) {
        LOGGER.infof("GraphQL request for all books [sort: %s, order: %s, limit: %d]", sort, order, limit);
        List<Book> books = bookService.getAllBooks(sort, order, limit);
        return bookMapper.toResponseDTOs(books);
    }

    @Query("searchBooks")
    @Description("Searches for books with pagination and sorting.")
    @RolesAllowed({ "user", "admin" })
    public BookPage searchBooks(
            @Name("query") String query,
            @Name("page") @DefaultValue("0") int page,
            @Name("size") @DefaultValue("10") int size,
            @Name("sortBy") @DefaultValue("title") String sortBy,
            @Name("sortOrder") @DefaultValue("asc") String sortOrder) {
        LOGGER.infof("GraphQL search for books [query: '%s', page: %d, size: %d]", query, page, size);
        DomainPage<Book> searchResultPage = bookService.searchBooks(query, page, size, sortBy, sortOrder);
        return BookPage.from(searchResultPage, bookMapper);
    }

    @Mutation("createBook")
    @Description("Creates a new book in the catalog.")
    @RolesAllowed("admin")
    public BookResponseDTO createBook(@Name("book") @Valid BookRequestDTO bookInput) {
        LOGGER.infof("GraphQL request to create book with ISBN: %s", bookInput.getIsbn());
        Book createdBook = bookService.createBook(bookInput);
        return bookMapper.toResponseDTO(createdBook);
    }

    @Mutation("updateBook")
    @Description("Updates an existing book.")
    @RolesAllowed("admin")
    public BookResponseDTO updateBook(@Name("bookId") UUID bookId, @Name("updates") @Valid BookUpdateDTO bookUpdateInput) {
        LOGGER.infof("GraphQL request to update book with ID: %s", bookId);
        return bookService.updateBook(bookId, bookUpdateInput)
                .map(bookMapper::toResponseDTO)
                .orElse(null);
    }

    @Mutation("deleteBook")
    @Description("Deletes a book from the catalog.")
    @RolesAllowed("admin")
    public boolean deleteBook(@Name("bookId") UUID bookId) {
        LOGGER.infof("GraphQL request to delete book by ID: %s", bookId);
        return bookService.deleteBookById(bookId);
    }
}