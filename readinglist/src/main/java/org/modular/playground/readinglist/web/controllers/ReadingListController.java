package org.modular.playground.readinglist.web.controllers;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.usecases.ReadingListService;
import org.modular.playground.readinglist.infrastructure.persistence.postgres.mapper.ReadingListMapper;
import org.modular.playground.readinglist.web.dto.AddBookRequestDTO;
import org.modular.playground.readinglist.web.dto.MoveBookRequestDTO;
import org.modular.playground.readinglist.web.dto.ReadingListRequestDTO;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/readinglists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class ReadingListController {

    private static final Logger LOGGER = Logger.getLogger(ReadingListController.class);

    @Inject
    ReadingListService readingListService;

    @Inject
    JsonWebToken jwt;

    @Inject
    ReadingListMapper readingListMapper;

    @POST
    @RolesAllowed({ "user", "admin" })
    public Response createReadingList(@Valid ReadingListRequestDTO readingListRequestDTO) {
        LOGGER.infof("Received request to create reading list with name: %s", readingListRequestDTO.getName());
        ReadingList createdReadingList = readingListService.createReadingList(readingListRequestDTO, jwt);
        return Response.status(Response.Status.CREATED)
                .entity(readingListMapper.toResponseDTO(createdReadingList)).build();
    }

    @GET
    @Path("/{readingListId}")
    @RolesAllowed({ "user", "admin" })
    public Response getReadingListById(@PathParam("readingListId") UUID readingListId) {
        LOGGER.infof("Received request to get reading list by ID: %s", readingListId);
        Optional<ReadingList> readingList = readingListService.findReadingListById(readingListId, jwt);
        return readingList.map(rl -> Response.ok(readingListMapper.toResponseDTO(rl)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @RolesAllowed({ "user", "admin" })
    public Response getAllReadingListsForUser() {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        LOGGER.infof("Received request to get all reading lists for user: %s", currentUserId);
        List<ReadingList> readingLists = readingListService.getReadingListsForUser(currentUserId);
        return Response.ok(readingListMapper.toResponseDTOs(readingLists)).build();
    }

    @PUT
    @Path("/{readingListId}")
    @RolesAllowed({ "user", "admin" })
    public Response updateReadingList(@PathParam("readingListId") UUID readingListId,
            @Valid ReadingListRequestDTO readingListRequestDTO) {
        LOGGER.infof("Received request to update reading list ID: %s", readingListId);
        ReadingList updatedList = readingListService.updateReadingList(readingListId, readingListRequestDTO, jwt);
        return Response.ok(readingListMapper.toResponseDTO(updatedList)).build();
    }

    @DELETE
    @Path("/{readingListId}")
    @RolesAllowed({ "user", "admin" })
    public Response deleteReadingList(@PathParam("readingListId") UUID readingListId) {
        LOGGER.infof("Received request to delete reading list ID: %s", readingListId);
        readingListService.deleteReadingListById(readingListId, jwt);
        return Response.noContent().build();
    }

    @POST
    @Path("/{readingListId}/books")
    @RolesAllowed({ "user", "admin" })
    public Response addBookToReadingList(@PathParam("readingListId") UUID readingListId,
            @Valid AddBookRequestDTO addBookRequestDTO) {
        LOGGER.infof("Received request to add book %s to list %s", addBookRequestDTO.getBookId(), readingListId);
        readingListService.addBookToReadingList(readingListId, addBookRequestDTO.getBookId(), jwt);
        return Response.ok().entity("Book added to reading list.").build();
    }

    @DELETE
    @Path("/{readingListId}/books/{bookId}")
    @RolesAllowed({ "user", "admin" })
    public Response removeBookFromReadingList(@PathParam("readingListId") UUID readingListId,
            @PathParam("bookId") UUID bookId) {
        LOGGER.infof("Received request to remove book %s from list %s", bookId, readingListId);
        readingListService.removeBookFromReadingList(readingListId, bookId, jwt);
        return Response.noContent().build();
    }

    @GET
    @Path("/{readingListId}/books")
    @RolesAllowed({ "user", "admin" })
    public Response getBooksInReadingList(@PathParam("readingListId") UUID readingListId) {
        LOGGER.infof("Received request to get books in list %s", readingListId);
        List<Book> books = readingListService.getBooksInReadingList(readingListId, jwt);
        return Response.ok(books).build();
    }

    @GET
    @Path("/books/{bookId}/in-my-list")
    @RolesAllowed({ "user", "admin" })
    public Response getReadingListForBookAndUser(@PathParam("bookId") UUID bookId) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        LOGGER.infof("Received request to check if user %s has book %s in a list", currentUserId, bookId);
        Optional<ReadingList> readingListOptional = readingListService.findReadingListForBookAndUser(currentUserId,
                bookId);
        return readingListOptional.map(rl -> Response.ok(readingListMapper.toResponseDTO(rl)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/books/{bookId}/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "user", "admin" })
    public Response moveBookBetweenReadingLists(@PathParam("bookId") UUID bookId,
            @Valid MoveBookRequestDTO moveRequestDTO) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        LOGGER.infof("Received request to move book %s from list %s to %s", bookId, moveRequestDTO.getSourceListId(),
                moveRequestDTO.getTargetListId());
        readingListService.moveBookBetweenReadingLists(
                currentUserId, bookId, moveRequestDTO.getSourceListId(), moveRequestDTO.getTargetListId(), jwt);
        return Response.ok().entity("Book moved successfully.").build();
    }
}