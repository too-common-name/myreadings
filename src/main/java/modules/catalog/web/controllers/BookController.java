package modules.catalog.web.controllers;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modules.catalog.domain.Book;
import modules.catalog.domain.BookImpl;
import modules.catalog.usecases.BookService;
import modules.catalog.web.dto.BookResponseDTO;
import modules.catalog.web.dto.BookRequestDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.quarkus.security.Authenticated;

@Path("/api/v1/books") 
@Produces(MediaType.APPLICATION_JSON) 
@Consumes(MediaType.APPLICATION_JSON) 
@Authenticated
public class BookController {

    @Inject
    BookService bookService;

    @POST
    @RolesAllowed("admin")
    public Response createBook(@Valid BookRequestDTO createBookRequestDTO) {
        Book bookToCreate = new BookImpl.BookBuilder()
                .bookId(UUID.randomUUID())
                .isbn(createBookRequestDTO.getIsbn())
                .title(createBookRequestDTO.getTitle())
                .authors(createBookRequestDTO.getAuthors())
                .publicationDate(createBookRequestDTO.getPublicationDate())
                .publisher(createBookRequestDTO.getPublisher())
                .description(createBookRequestDTO.getDescription())
                .pageCount(createBookRequestDTO.getPageCount())
                .coverImageId(createBookRequestDTO.getCoverImageId())
                .originalLanguage(createBookRequestDTO.getOriginalLanguage())
                .build();

        Book createdBook = bookService.createBook(bookToCreate);

        BookResponseDTO responseDTO = mapToBookResponseDTO(createdBook);

        return Response.status(Response.Status.CREATED)
                       .entity(responseDTO)
                       .build();
    }

    @GET
    @Path("/{bookId}")
    @RolesAllowed({"user", "admin"})
    public Response getBookById(@PathParam("bookId") UUID bookId) {
        Optional<Book> bookOptional = bookService.getBookById(bookId);
        if (bookOptional.isPresent()) {
            BookResponseDTO responseDTO = mapToBookResponseDTO(bookOptional.get());
            return Response.ok(responseDTO)
                           .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                           .build();
        }
    }

    @DELETE
    @Path("/{bookId}")
    @RolesAllowed("admin")
    public Response deleteBookById(@PathParam("bookId") UUID bookId) {
        if (bookService.getBookById(bookId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                           .build();
        }
        bookService.deleteBookById(bookId);
        return Response.noContent()
                       .build();
    }

    @GET
    @RolesAllowed({"user", "admin"})
    public Response getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        List<BookResponseDTO> responseDTOs = books.stream()
                                                    .map(this::mapToBookResponseDTO)
                                                    .collect(Collectors.toList());
        return Response.ok(responseDTOs)
                       .build();
    }

    private BookResponseDTO mapToBookResponseDTO(Book book) {
        return BookResponseDTO.builder()
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .authors(book.getAuthors())
                .publicationDate(book.getPublicationDate())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .pageCount(book.getPageCount())
                .coverImageId(book.getCoverImageId())
                .originalLanguage(book.getOriginalLanguage())
                .build();
    }
}