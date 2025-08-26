package org.modular.playground.readinglist.utils;

import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.domain.ReadingListImpl;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.utils.UserTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReadingListTestUtils {

    public static ReadingList createValidReadingList() {
        return createValidReadingListWithId(UUID.randomUUID());
    }

    public static ReadingList createValidReadingListWithId(UUID readingListId) {
        User user = UserTestUtils.createValidUser();
        return ReadingListImpl.builder()
                .readingListId(readingListId)
                .user(user)
                .name("Test Reading List")
                .description("A test reading list")
                .creationDate(LocalDateTime.now())
                .build();
    }

    public static ReadingList createValidReadingListWithName(String name) {
        User user = UserTestUtils.createValidUser();
        return ReadingListImpl.builder()
                .readingListId(UUID.randomUUID())
                .user(user)
                .name(name)
                .description("A test reading list")
                .creationDate(LocalDateTime.now())
                .build();
    }

    public static ReadingList createValidReadingListForUser(User user, String name) {
        return ReadingListImpl.builder()
                .readingListId(UUID.randomUUID())
                .user(user)
                .name(name)
                .description("A test reading list for a specific user")
                .creationDate(LocalDateTime.now())
                .build();
    }

    public static ReadingList createValidReadingListWithIdAndBookStubs(UUID readingListId, List<Book> books) {
        User user = UserTestUtils.createValidUser();
        return ReadingListImpl.builder()
                .readingListId(readingListId)
                .user(user)
                .name("Test List With Books")
                .description("A test list containing book stubs")
                .creationDate(LocalDateTime.now())
                .books(books)
                .build();
    }

    public static ReadingList createValidReadingListForUserWithBooks(User user, String name, List<Book> books) {
        return ReadingListImpl.builder()
                .readingListId(UUID.randomUUID())
                .user(user)
                .name(name)
                .description("A test reading list for a specific user with books")
                .creationDate(LocalDateTime.now())
                .books(books)
                .build();
    }

    public static ReadingListImpl.ReadingListImplBuilder from(ReadingList list) {
        return ReadingListImpl.builder()
                .readingListId(list.getReadingListId())
                .user(list.getUser())
                .name(list.getName())
                .description(list.getDescription())
                .creationDate(list.getCreationDate())
                .books(list.getBooks());
    }
}