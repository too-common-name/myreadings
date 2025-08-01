package modules.catalog.infrastructure.persistence.postgres;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BookPersistenceMapper {

    public BookEntity toEntity(Book book) {
        if (book == null) {
            return null;
        }
        return BookEntity.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .authors(book.getAuthors())
                .publicationDate(book.getPublicationDate())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .pageCount(book.getPageCount())
                .coverImageId(book.getCoverImageId())
                .originalLanguage(book.getOriginalLanguage())
                .genre(book.getGenre())
                .build();
    }

    public Book toDomain(BookEntity entity) {
        
        if (entity == null) {
            return null;
        }

        return BookImpl.builder()
                .bookId(entity.getBookId())
                .isbn(entity.getIsbn())
                .title(entity.getTitle())
                .authors(entity.getAuthors())
                .publicationDate(entity.getPublicationDate())
                .publisher(entity.getPublisher())
                .description(entity.getDescription())
                .pageCount(entity.getPageCount())
                .coverImageId(entity.getCoverImageId())
                .originalLanguage(entity.getOriginalLanguage())
                .genre(entity.getGenre())
                .build();
    }
}