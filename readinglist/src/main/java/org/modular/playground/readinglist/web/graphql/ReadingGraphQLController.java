package org.modular.playground.readinglist.web.graphql;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapper;
import org.modular.playground.catalog.web.dto.BookResponseDTO;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.usecases.ReadingListService;
import org.modular.playground.readinglist.infrastructure.persistence.postgres.mapper.ReadingListMapper;
import org.modular.playground.readinglist.web.dto.ReadingListRequestDTO;
import org.modular.playground.readinglist.web.dto.ReadingListResponseDTO;

import java.util.List;
import java.util.UUID;

@GraphQLApi
@Authenticated
public class ReadingGraphQLController {

    private static final Logger LOGGER = Logger.getLogger(ReadingGraphQLController.class);

    @Inject
    ReadingListService readingListService;

    @Inject
    JsonWebToken jwt;

    @Inject
    ReadingListMapper readingListMapper;

    @Inject
    BookMapper bookMapper;

    @Mutation
    @Description("Creates a new reading list for the current user.")
    @RolesAllowed({ "user", "admin" })
    public ReadingListResponseDTO createReadingList(@Name("readingList") @Valid ReadingListRequestDTO readingListRequestDTO) {
        LOGGER.infof("GraphQL request to create reading list with name: %s", readingListRequestDTO.getName());
        ReadingList createdReadingList = readingListService.createReadingList(readingListRequestDTO, jwt);
        return readingListMapper.toResponseDTO(createdReadingList);
    }

    @Query
    @Description("Finds a reading list by its unique ID.")
    @RolesAllowed({ "user", "admin" })
    public ReadingListResponseDTO readingListById(UUID readingListId) {
        LOGGER.infof("GraphQL request to get reading list by ID: %s", readingListId);
        return readingListService.findReadingListById(readingListId, jwt)
                .map(readingListMapper::toResponseDTO)
                .orElse(null);
    }

    @Query
    @Description("Gets all reading lists for the current user.")
    @RolesAllowed({ "user", "admin" })
    public List<ReadingListResponseDTO> myReadingLists() {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        LOGGER.infof("GraphQL request to get all reading lists for user: %s", currentUserId);
        List<ReadingList> readingLists = readingListService.getReadingListsForUser(currentUserId);
        return readingListMapper.toResponseDTOs(readingLists);
    }

    @Mutation
    @Description("Updates an existing reading list.")
    @RolesAllowed({ "user", "admin" })
    public ReadingListResponseDTO updateReadingList(UUID readingListId, @Name("updates") @Valid ReadingListRequestDTO readingListRequestDTO) {
        LOGGER.infof("GraphQL request to update reading list ID: %s", readingListId);
        ReadingList updatedList = readingListService.updateReadingList(readingListId, readingListRequestDTO, jwt);
        return readingListMapper.toResponseDTO(updatedList);
    }

    @Mutation
    @Description("Deletes a reading list.")
    @RolesAllowed({ "user", "admin" })
    public boolean deleteReadingList(UUID readingListId) {
        LOGGER.infof("GraphQL request to delete reading list ID: %s", readingListId);
        readingListService.deleteReadingListById(readingListId, jwt);
        return true;
    }

    @Mutation
    @Description("Adds a book to a reading list.")
    @RolesAllowed({ "user", "admin" })
    public boolean addBookToReadingList(UUID readingListId, UUID bookId) {
        LOGGER.infof("GraphQL request to add book %s to list %s", bookId, readingListId);
        readingListService.addBookToReadingList(readingListId, bookId, jwt);
        return true;
    }

    @Mutation
    @Description("Removes a book from a reading list.")
    @RolesAllowed({ "user", "admin" })
    public boolean removeBookFromReadingList(UUID readingListId, UUID bookId) {
        LOGGER.infof("GraphQL request to remove book %s from list %s", bookId, readingListId);
        readingListService.removeBookFromReadingList(readingListId, bookId, jwt);
        return true;
    }

    @Query
    @Description("Gets all books in a specific reading list.")
    @RolesAllowed({ "user", "admin" })
    public List<BookResponseDTO> booksInReadingList(UUID readingListId) {
        LOGGER.infof("GraphQL request to get books in list %s", readingListId);
        return readingListService.getBooksInReadingList(readingListId, jwt)
                .stream()
                .map(bookMapper::toResponseDTO)
                .toList();
    }

    @Query
    @Description("Finds the reading list that contains a specific book for the current user.")
    @RolesAllowed({ "user", "admin" })
    public ReadingListResponseDTO readingListContainingBook(UUID bookId) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        LOGGER.infof("GraphQL request to check if user %s has book %s in a list", currentUserId, bookId);
        return readingListService.findReadingListForBookAndUser(currentUserId, bookId)
                .map(readingListMapper::toResponseDTO)
                .orElse(null);
    }

    @Mutation
    @Description("Moves a book from one reading list to another for the current user.")
    @RolesAllowed({ "user", "admin" })
    public boolean moveBookBetweenReadingLists(UUID bookId, UUID sourceListId, UUID targetListId) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        LOGGER.infof("GraphQL request to move book %s from list %s to %s", bookId, sourceListId, targetListId);
        readingListService.moveBookBetweenReadingLists(currentUserId, bookId, sourceListId, targetListId, jwt);
        return true;
    }
}