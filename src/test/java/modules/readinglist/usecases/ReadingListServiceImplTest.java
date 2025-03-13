package modules.readinglist.usecases;

import modules.catalog.domain.Book;
import modules.catalog.usecases.BookService;
import modules.readinglist.domain.ReadingList;
import modules.readinglist.infrastructure.ReadingListRepository;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReadingListServiceImplTest {

    @Mock
    private ReadingListRepository readingListRepository;

    @Mock
    private BookService bookService;

    private ReadingListServiceImpl readingListService;

    @BeforeEach
    void setUp() {
        readingListService = new ReadingListServiceImpl(readingListRepository, bookService);
    }

    @Test
    void createReadingListSuccessful() {
        ReadingList readingListToCreate = ReadingListTestUtils.createValidReadingList();
        ReadingList createdReadingList = ReadingListTestUtils.createValidReadingListWithId(readingListToCreate.getReadingListId());
        when(readingListRepository.save(readingListToCreate)).thenReturn(createdReadingList);

        ReadingList result = readingListService.createReadingList(readingListToCreate);

        assertEquals(createdReadingList, result);
        verify(readingListRepository, times(1)).save(readingListToCreate);
    }

    @Test
    void findReadingListByIdSuccessful() {
        UUID readingListId = UUID.randomUUID();
        ReadingList readingList = ReadingListTestUtils.createValidReadingListWithId(readingListId);
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.of(readingList));

        Optional<ReadingList> result = readingListService.findReadingListById(readingListId);

        assertTrue(result.isPresent());
        assertEquals(readingList, result.get());
        verify(readingListRepository, times(1)).findById(readingListId);
    }

    @Test
    void findReadingListByIdFails() {
        UUID readingListId = UUID.randomUUID();
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.empty());

        Optional<ReadingList> result = readingListService.findReadingListById(readingListId);

        assertTrue(result.isEmpty());
        verify(readingListRepository, times(1)).findById(readingListId);
    }

    @Test
    void getReadingListsForUserSuccessful() {
        UUID userId = UUID.randomUUID();
        User user = UserTestUtils.createValidUserWithId(userId);
        List<ReadingList> readingLists = List.of(
                ReadingListTestUtils.createValidReadingListForUser(user, "List 1"),
                ReadingListTestUtils.createValidReadingListForUser(user, "List 2")
        );
        when(readingListRepository.findByUserId(userId)).thenReturn(readingLists);

        List<ReadingList> result = readingListService.getReadingListsForUser(userId);

        assertEquals(readingLists, result);
        verify(readingListRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getReadingListsForUserFails() {
        UUID userId = UUID.randomUUID();
        when(readingListRepository.findByUserId(userId)).thenReturn(List.of());

        List<ReadingList> result = readingListService.getReadingListsForUser(userId);

        assertTrue(result.isEmpty());
        verify(readingListRepository, times(1)).findByUserId(userId);
    }


    @Test
    void updateReadingListSuccessful() {
        UUID readingListId = UUID.randomUUID();
        ReadingList existingReadingList = ReadingListTestUtils.createValidReadingListWithId(readingListId);
        ReadingList updatedReadingList = ReadingListTestUtils.createValidReadingListWithId(readingListId);
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.of(existingReadingList));
        when(readingListRepository.save(updatedReadingList)).thenReturn(updatedReadingList);

        ReadingList result = readingListService.updateReadingList(updatedReadingList);

        assertEquals(updatedReadingList, result);
        verify(readingListRepository, times(1)).findById(readingListId);
        verify(readingListRepository, times(1)).save(updatedReadingList);
    }

    @Test
    void updateReadingListFails() {
        UUID readingListId = UUID.randomUUID();
        ReadingList updatedReadingList = ReadingListTestUtils.createValidReadingListWithId(readingListId);
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> readingListService.updateReadingList(updatedReadingList));

        verify(readingListRepository, times(1)).findById(readingListId);
        verify(readingListRepository, never()).save(any());
    }

    @Test
    void deleteReadingListByIdSuccessful() {
        UUID readingListId = UUID.randomUUID();
        ReadingList existingReadingList = ReadingListTestUtils.createValidReadingListWithId(readingListId);
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.of(existingReadingList));

        readingListService.deleteReadingListById(readingListId);

        verify(readingListRepository, times(1)).findById(readingListId);
        verify(readingListRepository, times(1)).deleteById(readingListId);
    }

    @Test
    void deleteReadingListByIdFails() {
        UUID readingListId = UUID.randomUUID();
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> readingListService.deleteReadingListById(readingListId));

        verify(readingListRepository, times(1)).findById(readingListId);
        verify(readingListRepository, never()).deleteById(any());
    }

    @Test
    void addBookToReadingListSuccessful() {
        UUID readingListId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        ReadingList readingList = ReadingListTestUtils.createValidReadingListWithId(readingListId);
        Book book = CatalogTestUtils.createValidBookWithId(bookId);
        Optional<ReadingList> readingListOptional = Optional.of(readingList);
        Optional<Book> bookOptional = Optional.of(book);

        when(readingListRepository.findById(readingListId)).thenReturn(readingListOptional);
        when(bookService.getBookById(bookId)).thenReturn(bookOptional);

        readingListService.addBookToReadingList(readingListId, bookId);

        verify(readingListRepository, times(1)).findById(readingListId);
        verify(bookService, times(1)).getBookById(bookId);
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(readingListRepository, times(1)).addBookToReadingList(eq(readingListId), bookCaptor.capture());
        assertEquals(book, bookCaptor.getValue());
    }

    @Test
    void addBookToReadingListFailsReadingListNotFound() {
        UUID readingListId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> readingListService.addBookToReadingList(readingListId, bookId));

        verify(readingListRepository, times(1)).findById(readingListId);
        verify(bookService, never()).getBookById(any());
        verify(readingListRepository, never()).addBookToReadingList(any(), any());
    }

    @Test
    void addBookToReadingListFailsBookNotFoundInCatalog() {
        UUID readingListId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        ReadingList readingList = ReadingListTestUtils.createValidReadingListWithId(readingListId);
        Optional<ReadingList> readingListOptional = Optional.of(readingList);
        when(readingListRepository.findById(readingListId)).thenReturn(readingListOptional);
        when(bookService.getBookById(bookId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> readingListService.addBookToReadingList(readingListId, bookId));

        verify(readingListRepository, times(1)).findById(readingListId);
        verify(bookService, times(1)).getBookById(bookId);
        verify(readingListRepository, never()).addBookToReadingList(any(), any());
    }

    @Test
    void removeBookFromReadingListSuccessful() {
        UUID readingListId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        ReadingList readingList = ReadingListTestUtils.createValidReadingListWithId(readingListId);
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.of(readingList));

        readingListService.removeBookFromReadingList(readingListId, bookId);

        verify(readingListRepository, times(1)).findById(readingListId);
        verify(readingListRepository, times(1)).removeBookFromReadingList(readingListId, bookId);
    }

    @Test
    void removeBookFromReadingListFails() {
        UUID readingListId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> readingListService.removeBookFromReadingList(readingListId, bookId));

        verify(readingListRepository, times(1)).findById(readingListId);
        verify(readingListRepository, never()).removeBookFromReadingList(any(), any());
    }

    @Test
    void getBooksInReadingListSuccessful() {
        UUID readingListId = UUID.randomUUID();
        List<Book> expectedBooks = List.of(
                CatalogTestUtils.createValidBookWithId(UUID.randomUUID()),
                CatalogTestUtils.createValidBookWithId(UUID.randomUUID())
        );
        when(readingListRepository.getBooksInReadingList(readingListId)).thenReturn(expectedBooks);

        List<Book> result = readingListService.getBooksInReadingList(readingListId);

        assertEquals(expectedBooks, result);
        verify(readingListRepository, times(1)).getBooksInReadingList(readingListId);
    }

    @Test
    void getBooksInReadingList() {
        UUID readingListId = UUID.randomUUID();
        when(readingListRepository.getBooksInReadingList(readingListId)).thenReturn(List.of());

        List<Book> result = readingListService.getBooksInReadingList(readingListId);

        assertTrue(result.isEmpty());
        verify(readingListRepository, times(1)).getBooksInReadingList(readingListId);
    }
}