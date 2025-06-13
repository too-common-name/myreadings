package modules.readinglist.usecases;

import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.BookService;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.usecases.ReadingListServiceImpl;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReadingListServiceImplTest {

    @Mock
    private ReadingListRepository readingListRepository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private ReadingListServiceImpl readingListService;

    @Test
    void findReadingListById_shouldReturnEnrichedList_whenBooksExist() {
        UUID readingListId = UUID.randomUUID();
        Book book1 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());
        Book book2 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());
        ReadingList partialList = ReadingListTestUtils.createValidReadingListWithIdAndBookStubs(readingListId, List.of(book1, book2));

        when(readingListRepository.findById(readingListId)).thenReturn(Optional.of(partialList));
        when(bookService.getBookById(book1.getBookId())).thenReturn(Optional.of(book1));
        when(bookService.getBookById(book2.getBookId())).thenReturn(Optional.of(book2));

        Optional<ReadingList> result = readingListService.findReadingListById(readingListId);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().getBooks().size());
        verify(readingListRepository, times(1)).findById(readingListId);
        verify(bookService, times(2)).getBookById(any(UUID.class));
    }

    @Test
    void getReadingListsForUserSuccessful() {
        User user = UserTestUtils.createValidUser();
        Book book1 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());
        ReadingList list1 = ReadingListTestUtils.createValidReadingListForUserWithBooks(user, "List 1", List.of(book1));

        when(readingListRepository.findByUserId(user.getKeycloakUserId())).thenReturn(List.of(list1));
        when(bookService.getBookById(book1.getBookId())).thenReturn(Optional.of(book1));

        List<ReadingList> result = readingListService.getReadingListsForUser(user.getKeycloakUserId());

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBooks().size());
        verify(readingListRepository, times(1)).findByUserId(user.getKeycloakUserId());
        verify(bookService, times(1)).getBookById(book1.getBookId());
    }

    @Test
    void getBooksInReadingListSuccessful() {
        UUID readingListId = UUID.randomUUID();
        Book book1 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());
        ReadingList partialList = ReadingListTestUtils.createValidReadingListWithIdAndBookStubs(readingListId, List.of(book1));

        when(readingListRepository.findById(readingListId)).thenReturn(Optional.of(partialList));
        when(bookService.getBookById(book1.getBookId())).thenReturn(Optional.of(book1));

        List<Book> result = readingListService.getBooksInReadingList(readingListId);

        assertEquals(1, result.size());
        assertEquals(book1, result.get(0));
        verify(readingListRepository, times(1)).findById(readingListId);
        verify(bookService, times(1)).getBookById(book1.getBookId());
    }

    @Test
    void addBookToReadingListSuccessful() {
        UUID readingListId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        ReadingList readingList = ReadingListTestUtils.createValidReadingListWithId(readingListId);
        Book book = CatalogTestUtils.createValidBookWithId(bookId);

        when(readingListRepository.findById(readingListId)).thenReturn(Optional.of(readingList));
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(book));

        readingListService.addBookToReadingList(readingListId, bookId);

        verify(readingListRepository, times(1)).findById(readingListId);
        verify(bookService, times(1)).getBookById(bookId);
        verify(readingListRepository, times(1)).addBookToReadingList(eq(readingListId), eq(book));
    }
    
    @Test
    void createReadingListSuccessful() {
        ReadingList readingListToCreate = ReadingListTestUtils.createValidReadingList();
        when(readingListRepository.create(any(ReadingList.class))).thenReturn(readingListToCreate);
        
        ReadingList result = readingListService.createReadingList(readingListToCreate);
        
        assertEquals(readingListToCreate, result);
        verify(readingListRepository).create(readingListToCreate);
    }
    
    @Test
    void updateReadingListFails() {
        UUID readingListId = UUID.randomUUID();
        ReadingList updatedReadingList = ReadingListTestUtils.createValidReadingListWithId(readingListId);
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> readingListService.updateReadingList(updatedReadingList));
        
        verify(readingListRepository).findById(readingListId);
        verify(readingListRepository, never()).update(any());
    }

    @Test
    void deleteReadingListByIdSuccessful() {
        UUID readingListId = UUID.randomUUID();
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.of(ReadingListTestUtils.createValidReadingListWithId(readingListId)));
        
        readingListService.deleteReadingListById(readingListId);
        
        verify(readingListRepository).findById(readingListId);
        verify(readingListRepository).deleteById(readingListId);
    }
    
    @Test
    void removeBookFromReadingListSuccessful() {
        UUID readingListId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(readingListRepository.findById(readingListId)).thenReturn(Optional.of(ReadingListTestUtils.createValidReadingListWithId(readingListId)));
        
        readingListService.removeBookFromReadingList(readingListId, bookId);
        
        verify(readingListRepository).findById(readingListId);
        verify(readingListRepository).removeBookFromReadingList(readingListId, bookId);
    }
}
