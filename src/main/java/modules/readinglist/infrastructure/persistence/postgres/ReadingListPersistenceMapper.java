package modules.readinglist.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;

import java.util.stream.Collectors;
import java.util.List;

@ApplicationScoped
public class ReadingListPersistenceMapper {

    public ReadingList toDomain(ReadingListEntity entity) {
        if (entity == null) {
            return null;
        }

        List<Book> partialBooks = entity.getItems().stream()
                .map(itemEntity -> BookImpl.builder()
                        .bookId(itemEntity.getId().getBookId()) 
                        .build()) 
                .collect(Collectors.toList());

        return ReadingListImpl.builder()
                .readingListId(entity.getId())
                .userId(entity.getUserId()) 
                .name(entity.getName())
                .description(entity.getDescription())
                .creationDate(entity.getCreationDate())
                .books(partialBooks) 
                .build();
    }

    public ReadingListEntity toEntity(ReadingList domain) {
        if (domain == null) {
            return null;
        }

        var entity = ReadingListEntity.builder()
                .id(domain.getReadingListId())
                .userId(domain.getUserId())
                .name(domain.getName())
                .description(domain.getDescription())
                .creationDate(domain.getCreationDate())
                .build();

        
        List<ReadingListItemEntity> items = domain.getBooks().stream()
                .map(book -> {
                    var item = new ReadingListItemEntity();
                    item.setId(new ReadingListItemId(domain.getReadingListId(), book.getBookId()));
                    item.setReadingList(entity);      
                    return item;
                })
                .collect(Collectors.toList());

        entity.setItems(items);
        return entity;
    }
}