package org.modular.playground.readinglist.core.usecases;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.usecases.BookService;
import org.modular.playground.common.security.SecurityUtils;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.domain.ReadingListImpl;
import org.modular.playground.readinglist.core.usecases.repositories.ReadingListRepository;
import org.modular.playground.readinglist.infrastructure.persistence.postgres.mapper.ReadingListMapper;
import org.modular.playground.readinglist.web.dto.ReadingListRequestDTO;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.UserService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.function.Function;
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
    @Inject
    ReadingListMapper readingListMapper;

    @Override
    public ReadingList createReadingList(ReadingListRequestDTO request, JsonWebToken principal) {
        UUID userId = UUID.fromString(principal.getSubject());
        LOGGER.infof("User %s creating new reading list named '%s'", userId, request.getName());
        User user = userService.findUserProfileById(userId, principal)
            .orElseThrow(() -> new NotFoundException("User not found."));
        
        ReadingList newReadingList = readingListMapper.toDomain(request, user);
        return createInTransaction(newReadingList);
    }

    @Override
    @Transactional
    public ReadingList createReadingListInternal(ReadingList readingList) {
        LOGGER.infof("Internally creating reading list '%s' for user %s", readingList.getName(), readingList.getUser().getKeycloakUserId());
        return readingListRepository.create(readingList);
    }

    @Override
    public Optional<ReadingList> findReadingListById(UUID readingListId, JsonWebToken principal) {
        LOGGER.debugf("Finding reading list %s for user %s", readingListId, principal.getSubject());
        
        Optional<ReadingList> readingListOpt = findByIdInTransaction(readingListId);
        
        if (readingListOpt.isEmpty()) {
            return Optional.empty();
        }
        
        ReadingList list = readingListOpt.get();
        checkOwnership(list, principal);

        return Optional.of(enrichListWithBooks(list));
    }

    @Override
    public List<ReadingList> getReadingListsForUser(UUID userId) {
        LOGGER.debugf("Finding all reading lists for user ID: %s", userId);
        List<ReadingList> lists = findByUserIdInTransaction(userId);
        return enrichListsWithBooks(lists);
    }

    @Override
    public ReadingList updateReadingList(UUID readingListId, ReadingListRequestDTO request, JsonWebToken principal) {
        LOGGER.infof("User %s updating reading list %s", principal.getSubject(), readingListId);
        ReadingListImpl existing = (ReadingListImpl) findByIdInTransaction(readingListId)
                 .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        checkOwnership(existing, principal);
        readingListMapper.updateFromDto(request, existing);
        return updateInTransaction(existing);
    }
    
    @Override
    public void deleteReadingListById(UUID readingListId, JsonWebToken principal) {
        LOGGER.infof("User %s deleting reading list %s", principal.getSubject(), readingListId);
        ReadingList listToDelete = findByIdInTransaction(readingListId)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        checkOwnership(listToDelete, principal);
        deleteByIdInTransaction(readingListId);
    }

    @Override
    public void addBookToReadingList(UUID readingListId, UUID bookId, JsonWebToken principal) {
        LOGGER.infof("User %s adding book %s to list %s", principal.getSubject(), bookId, readingListId);
        ReadingList readingList = findByIdInTransaction(readingListId)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        checkOwnership(readingList, principal);
        if (bookService.getBookById(bookId).isEmpty()) {
            throw new NotFoundException("Book not found with ID: " + bookId);
        }
        addBookInTransaction(readingListId, bookId);
    }

    @Override
    public void removeBookFromReadingList(UUID readingListId, UUID bookId, JsonWebToken principal) {
        LOGGER.infof("User %s removing book %s from list %s", principal.getSubject(), bookId, readingListId);
        ReadingList readingList = findByIdInTransaction(readingListId)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        checkOwnership(readingList, principal);
        removeBookInTransaction(readingListId, bookId);
    }

    @Override
    public List<Book> getBooksInReadingList(UUID readingListId, JsonWebToken principal) {
        LOGGER.debugf("User %s getting books from list %s", principal.getSubject(), readingListId);
        ReadingList readingList = findByIdInTransaction(readingListId)
            .orElseThrow(() -> new NotFoundException("Reading list not found with ID: " + readingListId));
        checkOwnership(readingList, principal);

        List<UUID> bookIds = getBookIdsInTransaction(readingListId);
        if (bookIds.isEmpty()) {
            return Collections.emptyList();
        }
        return bookService.getBooksByIds(bookIds);
    }
    
    @Override
    public Optional<ReadingList> findReadingListForBookAndUser(UUID userId, UUID bookId) {
        LOGGER.debugf("Finding if user %s has book %s in any list", userId, bookId);
        Optional<ReadingList> listOpt = findListContainingBookForUserInTransaction(userId, bookId);
        return listOpt.map(this::enrichListWithBooks);
    }

    @Override
    public void moveBookBetweenReadingLists(UUID userId, UUID bookId, UUID sourceListId, UUID targetListId, JsonWebToken principal) {
        LOGGER.infof("User %s moving book %s from list %s to list %s", userId, bookId, sourceListId, targetListId);
        ReadingList sourceList = findByIdInTransaction(sourceListId)
            .orElseThrow(() -> new NotFoundException("Source list not found with ID: " + sourceListId));
        checkOwnership(sourceList, principal);
        ReadingList targetList = findByIdInTransaction(targetListId)
            .orElseThrow(() -> new NotFoundException("Target list not found with ID: " + targetListId));
        checkOwnership(targetList, principal);
        
        moveBookInTransaction(sourceListId, targetListId, bookId);
    }
    
    private void checkOwnership(ReadingList readingList, JsonWebToken principal) {
        UUID currentUserId = UUID.fromString(principal.getSubject());
        boolean isAdmin = SecurityUtils.isAdmin(principal);
        if (!readingList.getUser().getKeycloakUserId().equals(currentUserId) && !isAdmin) {
            throw new ForbiddenException("Reading list does not belong to the current user.");
        }
    }

    private ReadingList enrichListWithBooks(ReadingList list) {
        List<UUID> bookIds = list.getBooks().stream().map(Book::getBookId).collect(Collectors.toList());
        if (bookIds.isEmpty()) {
            return list;
        }
        List<Book> fullBooks = bookService.getBooksByIds(bookIds);
        ((ReadingListImpl) list).setBooks(fullBooks);
        return list;
    }

    private List<ReadingList> enrichListsWithBooks(List<ReadingList> lists) {
        if (lists.isEmpty()) return Collections.emptyList();
        
        List<UUID> allBookIds = lists.stream()
            .flatMap(list -> list.getBooks().stream().map(Book::getBookId))
            .distinct().collect(Collectors.toList());
        
        if (allBookIds.isEmpty()) return lists;

        Map<UUID, Book> booksMap = bookService.getBooksByIds(allBookIds).stream()
            .collect(Collectors.toMap(Book::getBookId, Function.identity()));
            
        lists.forEach(list -> {
            List<Book> fullBooks = list.getBooks().stream()
                .map(bookStub -> booksMap.get(bookStub.getBookId()))
                .filter(Objects::nonNull).collect(Collectors.toList());
            ((ReadingListImpl) list).setBooks(fullBooks);
        });
        
        return lists;
    }

    @Transactional
    public ReadingList createInTransaction(ReadingList readingList) {
        return readingListRepository.create(readingList);
    }

    @Transactional
    protected Optional<ReadingList> findByIdInTransaction(UUID readingListId) {
        return readingListRepository.findById(readingListId);
    }

    @Transactional
    protected List<ReadingList> findByUserIdInTransaction(UUID userId) {
        return readingListRepository.findByUserId(userId);
    }

    @Transactional
    protected ReadingList updateInTransaction(ReadingList readingList) {
        return readingListRepository.update(readingList);
    }
    
    @Transactional
    protected void deleteByIdInTransaction(UUID readingListId) {
        readingListRepository.deleteById(readingListId);
    }

    @Transactional
    protected void addBookInTransaction(UUID readingListId, UUID bookId) {
        readingListRepository.addBookToReadingList(readingListId, bookId);
    }

    @Transactional
    protected void removeBookInTransaction(UUID readingListId, UUID bookId) {
        readingListRepository.removeBookFromReadingList(readingListId, bookId);
    }
    
    @Transactional
    protected List<UUID> getBookIdsInTransaction(UUID readingListId) {
        return readingListRepository.getBookIdsInReadingList(readingListId);
    }
    
    @Transactional
    protected Optional<ReadingList> findListContainingBookForUserInTransaction(UUID userId, UUID bookId) {
        return readingListRepository.findReadingListContainingBookForUser(userId, bookId);
    }

    @Transactional
    protected void moveBookInTransaction(UUID sourceListId, UUID targetListId, UUID bookId) {
        readingListRepository.removeBookFromReadingList(sourceListId, bookId);
        readingListRepository.addBookToReadingList(targetListId, bookId);
    }
}