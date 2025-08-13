package modules.catalog.web.controllers;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.core.usecases.BookService;
import modules.catalog.infrastructure.persistence.postgres.mapper.BookMapper;
import modules.catalog.web.dto.BookRequestDTO;
import modules.catalog.web.dto.BookResponseDTO;
import modules.catalog.web.dto.BookUpdateDTO;
import modules.catalog.web.dto.PagedResponse;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class BookController {

    private static final Logger LOGGER = Logger.getLogger(BookController.class);

    @Inject
    BookService bookService;

    @Inject
    BookMapper bookMapper;

    @POST
    @RolesAllowed("admin")
    public Response createBook(@Valid BookRequestDTO createBookRequestDTO) {
        LOGGER.infof("Received request to create book with ISBN: %s", createBookRequestDTO.getIsbn());
        Book createdBook = bookService.createBook(createBookRequestDTO);
        BookResponseDTO responseDTO = bookMapper.toResponseDTO(createdBook);
        LOGGER.infof("Book created successfully with ID: %s", createdBook.getBookId());
        return Response.status(Response.Status.CREATED).entity(responseDTO).build();
    }

    @PUT
    @Path("/{bookId}")
    @RolesAllowed("admin")
    public Response updateBook(@PathParam("bookId") UUID bookId, @Valid BookUpdateDTO updateDTO) {
        LOGGER.infof("Received request to update book with ID: %s", bookId);

        Optional<Book> updatedBookOpt = bookService.updateBook(bookId, updateDTO);

        return updatedBookOpt
                .map(book -> {
                    BookResponseDTO responseDTO = bookMapper.toResponseDTO(book);
                    LOGGER.infof("Book with ID: %s updated successfully", bookId);
                    return Response.ok(responseDTO).build();
                })
                .orElseGet(() -> {
                    LOGGER.warnf("Book with ID: %s not found for update", bookId);
                    return Response.status(Response.Status.NOT_FOUND).build();
                });
    }

    @GET
    @Path("/{bookId}")
    @RolesAllowed({ "user", "admin" })
    public Response getBookById(@PathParam("bookId") UUID bookId) {
        LOGGER.infof("Received request to get book by ID: %s", bookId);
        Optional<Book> bookOptional = bookService.getBookById(bookId);
        if (bookOptional.isPresent()) {
            BookResponseDTO responseDTO = bookMapper.toResponseDTO(bookOptional.get());
            LOGGER.debugf("Book found with ID: %s", bookId);
            return Response.ok(responseDTO).build();
        } else {
            LOGGER.warnf("Book not found for ID: %s", bookId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{bookId}")
    @RolesAllowed("admin")
    public Response deleteBookById(@PathParam("bookId") UUID bookId) {
        LOGGER.infof("Received request to delete book by ID: %s", bookId);
        boolean deleted = bookService.deleteBookById(bookId);
        if (deleted) {
            LOGGER.info("Book deleted successfully.");
            return Response.noContent().build();
        } else {
            LOGGER.warnf("Attempted to delete non-existent book with ID: %s", bookId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @RolesAllowed({ "user", "admin" })
    public Response getAllBooks(
            @QueryParam("sort") String sort,
            @QueryParam("order") String order,
            @QueryParam("limit") Integer limit) {
        LOGGER.infof("Received request to get all books with parameters [sort: %s, order: %s, limit: %s]",
                sort, order, limit);
        List<Book> books = bookService.getAllBooks(sort, order, limit);
        List<BookResponseDTO> responseDTOs = bookMapper.toResponseDTOs(books);
        LOGGER.infof("Found and returning %d books.", responseDTOs.size());
        return Response.ok(responseDTOs).build();
    }

    @GET
    @Path("/search")
    @RolesAllowed({ "user", "admin" })
    public PagedResponse<BookResponseDTO> searchBooks(
            @NotNull @NotBlank @QueryParam("query") String query,
            @DefaultValue("0") @QueryParam("page") int page,
            @DefaultValue("10") @QueryParam("size") int size,
            @DefaultValue("title") @QueryParam("sort") String sortBy,
            @DefaultValue("asc") @QueryParam("order") String sortOrder) {
        LOGGER.infof("Received book search request with query: '%s', page: %d, size: %d", query, page, size);
        DomainPage<Book> searchResultPage = bookService.searchBooks(query, page, size, sortBy, sortOrder);
        List<BookResponseDTO> content = bookMapper.toResponseDTOs(searchResultPage.content());
        LOGGER.infof("Search returned %d books.", searchResultPage.totalElements());
        return new PagedResponse<>(
                content,
                searchResultPage.pageNumber(),
                searchResultPage.pageSize(),
                searchResultPage.totalElements(),
                searchResultPage.totalPages(),
                searchResultPage.isLast(),
                searchResultPage.isFirst());
    }
}