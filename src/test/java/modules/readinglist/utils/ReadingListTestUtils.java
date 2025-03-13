package modules.readinglist.utils;

import modules.readinglist.domain.ReadingList;
import modules.readinglist.domain.ReadingListImpl;
import modules.user.domain.User;
import modules.user.utils.UserTestUtils;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReadingListTestUtils {

    public static ReadingList createValidReadingList() {
        return createValidReadingListWithId(UUID.randomUUID());
    }

    public static ReadingList createValidReadingListWithId(UUID readingListId) {
        User user = UserTestUtils.createValidUser();
        return new ReadingListImpl.ReadingListBuilder()
                .readingListId(readingListId)
                .user(user)
                .name("Test Reading List")
                .description("A test reading list")
                .creationDate(LocalDateTime.now())
                .build();
    }

    public static ReadingList createValidReadingListWithName(String name) {
        User user = UserTestUtils.createValidUser();
        return new ReadingListImpl.ReadingListBuilder()
                .readingListId(UUID.randomUUID())
                .user(user)
                .name(name)
                .description("A test reading list")
                .creationDate(LocalDateTime.now())
                .build();
    }

    public static ReadingList createValidReadingListForUser(User user, String name) {
        return new ReadingListImpl.ReadingListBuilder()
                .readingListId(UUID.randomUUID())
                .user(user)
                .name(name)
                .description("A test reading list for a specific user")
                .creationDate(LocalDateTime.now())
                .build();
    }
}