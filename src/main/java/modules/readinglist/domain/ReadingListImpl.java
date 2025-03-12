package modules.readinglist.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import common.annotations.Generated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import modules.user.domain.User;
import modules.catalog.domain.Book;
import java.util.List;

public class ReadingListImpl implements ReadingList {

    private final UUID readingListId;
    @NotNull
    private final User user;
    @NotBlank
    @Size(max = 30)
    private final String name;
    @Size(max = 200)
    private final String description;
    private final LocalDateTime creationDate;
    private final List<Book> books; // Can be ReadingListItem if we want to model custom fields

    private ReadingListImpl(UUID readingListId, User user, String name, String description,
            LocalDateTime creationDate, List<Book> books) {
        this.readingListId = readingListId;
        this.user = user;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.books = books;
    }

    public static class ReadingListBuilder {
        private UUID readingListId;
        private User user;
        private String name;
        private String description;
        private LocalDateTime creationDate;
        private List<Book> books = java.util.Collections.emptyList();

        public ReadingListBuilder readingListId(UUID readingListId) {
            this.readingListId = readingListId;
            return this;
        }

        public ReadingListBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ReadingListBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ReadingListBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ReadingListBuilder creationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public ReadingListBuilder books(List<Book> books) {
            this.books = books;
            return this;
        }

        public ReadingList build() {
            return new ReadingListImpl(readingListId, user, name, description, creationDate, books);
        }
    }

    @Generated
    @Override
    public UUID getReadingListId() {
        return readingListId;
    }

    @Generated
    @Override
    public User getUser() {
        return user;
    }

    @Generated
    @Override
    public String getName() {
        return name;
    }

    @Generated
    @Override
    public String getDescription() {
        return description;
    }

    @Generated
    @Override
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    @Generated
    @Override
    public List<Book> getBooks() {
        return books;
    }
}
