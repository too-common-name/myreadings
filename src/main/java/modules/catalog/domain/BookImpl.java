package modules.catalog.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public class BookImpl implements Book {

    private final UUID bookId;
    @NotBlank
    private final String isbn;
    @NotBlank
    @Size(max = 255)
    private final String title;
    private final List<String> authors;
    @PastOrPresent
    private final LocalDate publicationDate;
    private final String publisher;
    @Size(max = 500)
    private final String description;
    @Min(0)
    private final int pageCount;
    private final String coverImageId;
    private final String originalLanguage;

    private BookImpl(UUID bookId, String isbn, String title, List<String> authors,
            LocalDate publicationDate, String publisher, String description, int pageCount,
            String coverImageId, String originalLanguage) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.publicationDate = publicationDate;
        this.publisher = publisher;
        this.description = description;
        this.pageCount = pageCount;
        this.coverImageId = coverImageId;
        this.originalLanguage = originalLanguage;
    }

    public static class BookBuilder {
        private UUID bookId;
        private String isbn;
        private String title;
        private List<String> authors;
        private LocalDate publicationDate;
        private String publisher;
        private String description;
        private int pageCount;
        private String coverImageId;
        private String originalLanguage;

        public BookBuilder bookId(UUID bookId) {
            this.bookId = bookId;
            return this;
        }

        public BookBuilder isbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public BookBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BookBuilder authors(List<String> authors) {
            this.authors = authors;
            return this;
        }

        public BookBuilder publicationDate(LocalDate publicationDate) {
            this.publicationDate = publicationDate;
            return this;
        }

        public BookBuilder publisher(String publisher) {
            this.publisher = publisher;
            return this;
        }

        public BookBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BookBuilder pageCount(int pageCount) {
            this.pageCount = pageCount;
            return this;
        }

        public BookBuilder coverImageId(String coverImageId) {
            this.coverImageId = coverImageId;
            return this;
        }

        public BookBuilder originalLanguage(String originalLanguage) {
            this.originalLanguage = originalLanguage;
            return this;
        }

        public Book build() {
            return new BookImpl(bookId, isbn, title, authors, publicationDate, publisher, description,
                    pageCount, coverImageId, originalLanguage);
        }
    }

    
    public UUID getBookId() {
        return bookId;
    }

    
    public String getIsbn() {
        return isbn;
    }

    
    public String getTitle() {
        return title;
    }

    
    public List<String> getAuthors() {
        return authors;
    }

    
    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    
    public String getPublisher() {
        return publisher;
    }

    
    public String getDescription() {
        return description;
    }

    
    public int getPageCount() {
        return pageCount;
    }

    
    public String getCoverImageId() {
        return coverImageId;
    }

    
    public String getOriginalLanguage() {
        return originalLanguage;
    }

}
