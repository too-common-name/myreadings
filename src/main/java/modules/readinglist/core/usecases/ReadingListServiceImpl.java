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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ReadingListServiceImpl implements ReadingListService {

    private static final Logger LOGGER = Logger.getLogger(ReadingListServiceImpl.class);

    @Inject
    ReadingListRepository readingListRepository;
    @Inject
    UserService userService;
    @Inject
    BookService bookService;

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
        if (!readingList.getUserId().equals(currentUserId) && !isAdmin) {
            throw new ForbiddenException("Reading list does not belong to the current user.");
        }
    }

    @Override
    @Transactional
    public ReadingList createReadingList(ReadingList readingList) {
        LOGGER.infof("Internally creating reading list '%s' for user %s", readingList.getName(), readingList.getUserId());
        return readingListRepository.create(readingList);
    }

    @Override
    public ReadingList createReadingList(ReadingListRequestDTO request, JsonWebToken principal) {
        UUID userId = UUID.fromString(principal.getSubject());
        LOGGER.infof("User %s creating new reading list named '%s'", userId, request.getName());
        User user = userService.findUserProfileById(userId, principal)
            .orElseThrow(() -> new NotFoundException("User not found."));

        ReadingList newReadingList = ReadingListImpl.builder()
                .readingListId(UUID.randomUUID())
                .userId(user.getKeycloakUserId())
                .name(request.getName())
                .description(request.getDescription())
                .creationDate(LocalDateTime.now())
                .build();
        
        return createReadingList(newReadingList);
    }

    @Override
    public Optional<ReadingList> findReadingListById(UUID readingListId, JsonWebToken principal) {
        LOGGER.debugf("Finding reading list %s for user %s", readingListId, principal.getSubject());
        Optional<ReadingList> readingListOpt = readingListRepository.findById(readingListId);
        readingListOpt.ifPresent(list -> checkOwnership(list, principal));
        return readingListOpt;
    }

    @Override
    public List<ReadingList> getReadingListsForUser(UUID userId) {
        LOGGER.debugf("Finding all reading lists for user ID: %s", userId);
        return readingListRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public ReadingList updateReadingList(UUID readingListId, ReadingListRequestDTO request, JsonWebToken principal) {
        LOGGER.infof("User %s updating reading list %s", principal.getSubject(), readingListId);
        ReadingList existing = findReadingListById(readingListId, principal)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
            
        ReadingList updated = ReadingListImpl.builder()
                .readingListId(existing.getReadingListId())
                .userId(existing.getUserId())
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
        ReadingList listToDelete = findReadingListById(readingListId, principal)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        readingListRepository.deleteById(listToDelete.getReadingListId());
    }

    @Override
    public void addBookToReadingList(UUID readingListId, UUID bookId, JsonWebToken principal) {
        LOGGER.infof("User %s adding book %s to list %s", principal.getSubject(), bookId, readingListId);
        ReadingList readingList = findReadingListById(readingListId, principal)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        Book book = bookService.getBookById(bookId)
            .orElseThrow(() -> new NotFoundException("Book not found with ID: " + bookId));
        addBookInTransaction(readingList.getReadingListId(), book);
    }

    @Transactional
    protected void addBookInTransaction(UUID readingListId, Book book) {
        readingListRepository.addBookToReadingList(readingListId, book);
    }

    @Override
    @Transactional
    public void removeBookFromReadingList(UUID readingListId, UUID bookId, JsonWebToken principal) {
        LOGGER.infof("User %s removing book %s from list %s", principal.getSubject(), bookId, readingListId);
        ReadingList readingList = findReadingListById(readingListId, principal)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        readingListRepository.removeBookFromReadingList(readingList.getReadingListId(), bookId);
    }

    @Override
    public List<Book> getBooksInReadingList(UUID readingListId, JsonWebToken principal) {
        LOGGER.debugf("User %s getting books from list %s", principal.getSubject(), readingListId);
        ReadingList readingList = findReadingListById(readingListId, principal)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        return readingListRepository.getBooksInReadingList(readingList.getReadingListId());
    }

    @Override
    public Optional<ReadingList> findReadingListForBookAndUser(UUID userId, UUID bookId) {
        LOGGER.debugf("Finding if user %s has book %s in any list", userId, bookId);
        return readingListRepository.findReadingListContainingBookForUser(userId, bookId);
    }

    @Override
    @Transactional
    public void moveBookBetweenReadingLists(UUID userId, UUID bookId, UUID sourceListId, UUID targetListId, JsonWebToken principal) {
        LOGGER.infof("User %s moving book %s from list %s to list %s", userId, bookId, sourceListId, targetListId);
        ReadingList sourceList = findReadingListById(sourceListId, principal)
            .orElseThrow(() -> new NotFoundException("Source list not found with ID: " + sourceListId));
        ReadingList targetList = findReadingListById(targetListId, principal)
            .orElseThrow(() -> new NotFoundException("Target list not found with ID: " + targetListId));
            
        if(!sourceList.getUserId().equals(userId) || !targetList.getUserId().equals(userId)){
            throw new ForbiddenException("Both reading lists must belong to the user.");
        }
        Book bookToMove = sourceList.getBooks().stream()
            .filter(b -> b.getBookId().equals(bookId)).findFirst()
            .orElseThrow(() -> new NotFoundException("Book not found in source list."));
            
        readingListRepository.removeBookFromReadingList(sourceListId, bookId);
        readingListRepository.addBookToReadingList(targetListId, bookToMove);
    }
}