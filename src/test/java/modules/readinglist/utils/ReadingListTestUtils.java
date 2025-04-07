package modules.readinglist.utils;

import modules.readinglist.domain.ReadingList;
import modules.readinglist.domain.ReadingListImpl;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import java.time.LocalDateTime;
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
}