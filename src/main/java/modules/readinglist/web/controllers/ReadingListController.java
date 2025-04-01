package modules.readinglist.web.controllers;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modules.catalog.domain.Book;
import modules.readinglist.domain.ReadingList;
import modules.readinglist.domain.ReadingListImpl;
import modules.readinglist.usecases.ReadingListService;
import modules.readinglist.web.dto.AddBookRequestDTO;
import modules.readinglist.web.dto.ReadingListRequestDTO;
import modules.readinglist.web.dto.ReadingListResponseDTO;
import modules.user.domain.User;
import modules.user.usecases.UserService;

import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/v1/readinglists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReadingListController {

    @Inject
    ReadingListService readingListService;

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    private UUID getCurrentUserIdFromJwt() {
        String userIdClaim = jwt.getClaim("sub");
        if (userIdClaim == null) {
            throw new NotAuthorizedException("User ID not found in JWT.");
        }
        return UUID.fromString(userIdClaim);
    }

    private ReadingListResponseDTO mapToReadingListResponseDTO(ReadingList readingList) {
        return ReadingListResponseDTO.builder()
                .readingListId(readingList.getReadingListId())
                .name(readingList.getName())
                .description(readingList.getDescription())
                .books(readingList.getBooks())
                .build();
    }

    private ReadingList findReadingListAndCheckOwnership(UUID readingListId, UUID currentUserId) {
        Optional<ReadingList> readingListOptional = readingListService.findReadingListById(readingListId);
        if (readingListOptional.isEmpty()) {
            throw new NotFoundException("Reading list not found with ID: " + readingListId);
        }
        ReadingList readingList = readingListOptional.get();
        if (!readingList.getUser().getUserId().equals(currentUserId)) {
            throw new ForbiddenException("Reading list does not belong to the current user.");
        }
        return readingList;
    }

    @POST
    @RolesAllowed({"user", "admin"})
    public Response createReadingList(@Valid ReadingListRequestDTO readingListRequestDTO) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();
            Optional<User> user = userService.findUserProfileById(currentUserId);

            if (user.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("User not found.").build();
            }

            ReadingList newReadingList = new ReadingListImpl.ReadingListBuilder()
                    .readingListId(UUID.randomUUID())
                    .user(user.get())
                    .name(readingListRequestDTO.getName())
                    .description(readingListRequestDTO.getDescription())
                    .creationDate(LocalDateTime.now())
                    .books(List.of())
                    .build();

            ReadingList createdReadingList = readingListService.createReadingList(newReadingList);
            return Response.status(Response.Status.CREATED).entity(mapToReadingListResponseDTO(createdReadingList)).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{readingListId}")
    @RolesAllowed({ "user", "admin" })
    public Response getReadingListById(@PathParam("readingListId") UUID readingListId) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();
            ReadingList readingList = findReadingListAndCheckOwnership(readingListId, currentUserId);
            return Response.ok(mapToReadingListResponseDTO(readingList)).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }
    }

    @GET
    @RolesAllowed({ "user", "admin" })
    public Response getAllReadingListsForUser() {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();
            List<ReadingList> readingLists = readingListService.getReadingListsForUser(currentUserId);
            return Response.ok(readingLists.stream()
                    .map(this::mapToReadingListResponseDTO)
                    .collect(Collectors.toList())).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{readingListId}")
    @RolesAllowed({ "user", "admin" })
    public Response updateReadingList(@PathParam("readingListId") UUID readingListId,
                                      @Valid ReadingListRequestDTO readingListRequestDTO) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();
            ReadingList existingReadingList = findReadingListAndCheckOwnership(readingListId, currentUserId);

            ReadingList updatedReadingList = new ReadingListImpl.ReadingListBuilder()
                    .readingListId(readingListId)
                    .user(existingReadingList.getUser())
                    .name(readingListRequestDTO.getName())
                    .description(readingListRequestDTO.getDescription())
                    .books(existingReadingList.getBooks())
                    .creationDate(existingReadingList.getCreationDate())
                    .build();

            ReadingList result = readingListService.updateReadingList(updatedReadingList);
            return Response.ok(mapToReadingListResponseDTO(result)).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{readingListId}")
    @RolesAllowed({ "user", "admin" })
    public Response deleteReadingList(@PathParam("readingListId") UUID readingListId) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();
            findReadingListAndCheckOwnership(readingListId, currentUserId);
            readingListService.deleteReadingListById(readingListId);
            return Response.noContent().build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{readingListId}/books")
    @RolesAllowed({ "user", "admin" })
    public Response addBookToReadingList(@PathParam("readingListId") UUID readingListId,
                                          @Valid AddBookRequestDTO addBookRequestDTO) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();
            findReadingListAndCheckOwnership(readingListId, currentUserId);
            readingListService.addBookToReadingList(readingListId, addBookRequestDTO.getBookId());
            return Response.ok().entity("Book added to reading list.").build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{readingListId}/books/{bookId}")
    @RolesAllowed({ "user", "admin" })
    public Response removeBookFromReadingList(@PathParam("readingListId") UUID readingListId,
                                             @PathParam("bookId") UUID bookId) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();
            findReadingListAndCheckOwnership(readingListId, currentUserId);
            readingListService.removeBookFromReadingList(readingListId, bookId);
            return Response.noContent().build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{readingListId}/books")
    @RolesAllowed({ "user", "admin" })
    public Response getBooksInReadingList(@PathParam("readingListId") UUID readingListId) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();
            findReadingListAndCheckOwnership(readingListId, currentUserId);
            List<Book> books = readingListService.getBooksInReadingList(readingListId);
            return Response.ok(books).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }
    }
}