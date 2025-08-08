package modules.readinglist.core.usecases;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.BookService;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.readinglist.web.dto.ReadingListRequestDTO;
import modules.user.core.domain.User;
import modules.user.core.usecases.UserService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReadingListServiceImpl implements ReadingListService {

    private static final Logger LOGGER = Logger.getLogger(ReadingListServiceImpl.class);

    @Inject
    ReadingListRepository readingListRepository;
    @Inject
    UserService userService;
    @Inject
    BookService bookService;

    private ReadingList enrichReadingList(ReadingList list, JsonWebToken principal) {
        if (list == null) return null;
        User fullUser = (principal != null) ? userService.findUserProfileById(list.getUser().getKeycloakUserId(), principal).orElse(list.getUser()) : list.getUser();
        List<UUID> bookIds = readingListRepository.getBookIdsInReadingList(list.getReadingListId());
        List<Book> fullBooks = bookIds.isEmpty() ? Collections.emptyList() : bookService.getBooksByIds(bookIds);
        return ReadingListImpl.builder()
            .readingListId(list.getReadingListId())
            .user(fullUser)
            .name(list.getName())
            .description(list.getDescription())
            .creationDate(list.getCreationDate())
            .books(fullBooks)
            .build();
    }

    private void checkOwnership(ReadingList readingList, JsonWebToken principal) {
        UUID currentUserId = UUID.fromString(principal.getSubject());
        boolean isAdmin = false;
        if (principal.getClaim("realm_access") instanceof jakarta.json.JsonObject) {
            jakarta.json.JsonObject realmAccess = principal.getClaim("realm_access");
            jakarta.json.JsonArray roles = realmAccess.getJsonArray("roles");
            if (roles != null) {
                isAdmin = roles.stream().anyMatch(role -> "admin".equals(((jakarta.json.JsonString) role).getString()));
            }
        }
        if (!readingList.getUser().getKeycloakUserId().equals(currentUserId) && !isAdmin) {
            throw new ForbiddenException("Reading list does not belong to the current user.");
        }
    }
    
    @Override
    public ReadingList createReadingList(ReadingListRequestDTO request, JsonWebToken principal) {
        UUID userId = UUID.fromString(principal.getSubject());
        LOGGER.infof("User %s creating new reading list named '%s'", userId, request.getName());
        User user = userService.findUserProfileById(userId, principal)
            .orElseThrow(() -> new NotFoundException("User not found."));
        ReadingList newReadingList = ReadingListImpl.builder()
                .readingListId(UUID.randomUUID())
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .creationDate(LocalDateTime.now())
                .build();
        return createReadingList(newReadingList);
    }
    
    @Override
    @Transactional
    public ReadingList createReadingList(ReadingList readingList) {
        LOGGER.infof("Internally creating reading list '%s' for user %s", readingList.getName(), readingList.getUser().getKeycloakUserId());
        return readingListRepository.create(readingList);
    }

    @Override
    public Optional<ReadingList> findReadingListById(UUID readingListId, JsonWebToken principal) {
        LOGGER.debugf("Finding reading list %s for user %s", readingListId, principal.getSubject());
        Optional<ReadingList> readingListOpt = readingListRepository.findById(readingListId);
        readingListOpt.ifPresent(list -> checkOwnership(list, principal));
        return readingListOpt.map(list -> enrichReadingList(list, principal));
    }

    @Override
    public List<ReadingList> getReadingListsForUser(UUID userId) {
        LOGGER.debugf("Finding all reading lists for user ID: %s", userId);
        List<ReadingList> lists = readingListRepository.findByUserId(userId);
        return lists.stream()
            .map(list -> enrichReadingList(list, null))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReadingList updateReadingList(UUID readingListId, ReadingListRequestDTO request, JsonWebToken principal) {
        LOGGER.infof("User %s updating reading list %s", principal.getSubject(), readingListId);
        ReadingList existing = readingListRepository.findById(readingListId)
             .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        checkOwnership(existing, principal);
        ReadingList updated = ReadingListImpl.builder()
                .readingListId(existing.getReadingListId())
                .user(existing.getUser())
                .name(request.getName())
                .description(request.getDescription())
                .creationDate(existing.getCreationDate())
                .books(existing.getBooks())
                .build();
        return readingListRepository.update(updated);
    }
    
    @Override
    @Transactional
    public void deleteReadingListById(UUID readingListId, JsonWebToken principal) {
        LOGGER.infof("User %s deleting reading list %s", principal.getSubject(), readingListId);
        ReadingList listToDelete = readingListRepository.findById(readingListId)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        checkOwnership(listToDelete, principal);
        readingListRepository.deleteById(readingListId);
    }

    @Override
    public void addBookToReadingList(UUID readingListId, UUID bookId, JsonWebToken principal) {
        LOGGER.infof("User %s adding book %s to list %s", principal.getSubject(), bookId, readingListId);
        ReadingList readingList = readingListRepository.findById(readingListId)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        checkOwnership(readingList, principal);
        if (bookService.getBookById(bookId).isEmpty()) {
            throw new NotFoundException("Book not found with ID: " + bookId);
        }
        addBookInTransaction(readingListId, bookId);
    }

    @Transactional
    protected void addBookInTransaction(UUID readingListId, UUID bookId) {
        readingListRepository.addBookToReadingList(readingListId, bookId);
    }

    @Override
    @Transactional
    public void removeBookFromReadingList(UUID readingListId, UUID bookId, JsonWebToken principal) {
        LOGGER.infof("User %s removing book %s from list %s", principal.getSubject(), bookId, readingListId);
        ReadingList readingList = readingListRepository.findById(readingListId)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        checkOwnership(readingList, principal);
        readingListRepository.removeBookFromReadingList(readingListId, bookId);
    }

    @Override
    public List<Book> getBooksInReadingList(UUID readingListId, JsonWebToken principal) {
        LOGGER.debugf("User %s getting books from list %s", principal.getSubject(), readingListId);
        ReadingList readingList = readingListRepository.findById(readingListId)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        checkOwnership(readingList, principal);
        List<UUID> bookIds = readingListRepository.getBookIdsInReadingList(readingListId);
        if (bookIds.isEmpty()) {
            return List.of();
        }
        return bookService.getBooksByIds(bookIds);
    }
    
    @Override
    public Optional<ReadingList> findReadingListForBookAndUser(UUID userId, UUID bookId) {
        LOGGER.debugf("Finding if user %s has book %s in any list", userId, bookId);
        return readingListRepository.findReadingListContainingBookForUser(userId, bookId)
            .map(list -> enrichReadingList(list, null));
    }

    @Override
    public void moveBookBetweenReadingLists(UUID userId, UUID bookId, UUID sourceListId, UUID targetListId, JsonWebToken principal) {
        LOGGER.infof("User %s moving book %s from list %s to list %s", userId, bookId, sourceListId, targetListId);
        ReadingList sourceList = readingListRepository.findById(sourceListId)
            .orElseThrow(() -> new NotFoundException("Source list not found with ID: " + sourceListId));
        checkOwnership(sourceList, principal);
        ReadingList targetList = readingListRepository.findById(targetListId)
            .orElseThrow(() -> new NotFoundException("Target list not found with ID: " + targetListId));
        checkOwnership(targetList, principal);
        
        List<UUID> bookIdsInSource = readingListRepository.getBookIdsInReadingList(sourceListId);
        if (!bookIdsInSource.contains(bookId)) {
             throw new NotFoundException("Book not found in source list.");
        }
        moveBookInTransaction(sourceListId, targetListId, bookId);
    }

    @Transactional
    protected void moveBookInTransaction(UUID sourceListId, UUID targetListId, UUID bookId) {
        readingListRepository.removeBookFromReadingList(sourceListId, bookId);
        readingListRepository.addBookToReadingList(targetListId, bookId);
    }
}