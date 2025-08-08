package modules.readinglist.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ReadingListPersistenceMapper {

    public ReadingList toDomain(ReadingListEntity entity) {
        if (entity == null) {
            return null;
        }

        User partialUser = UserImpl.builder()
                .keycloakUserId(entity.getUserId())
                .build();
        
        List<Book> partialBooks = entity.getItems().stream()
                .map(itemEntity -> BookImpl.builder()
                        .bookId(itemEntity.getId().getBookId())
                        .build())
                .collect(Collectors.toList());

        return ReadingListImpl.builder()
                .readingListId(entity.getId())
                .user(partialUser)
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

        ReadingListEntity entity = ReadingListEntity.builder()
                .userId(domain.getUser().getKeycloakUserId())
                .name(domain.getName())
                .description(domain.getDescription())
                .creationDate(domain.getCreationDate())
                .items(new ArrayList<>())
                .build();

        List<ReadingListItemEntity> items = domain.getBooks().stream()
                .map(book -> {
                    ReadingListItemEntity item = new ReadingListItemEntity();
                    item.setId(new ReadingListItemId(domain.getReadingListId(), book.getBookId()));
                    item.setReadingList(entity);
                    return item;
                })
                .collect(Collectors.toList());

        entity.setItems(items);
        return entity;
    }

    public void updateEntityFromDomain(ReadingListEntity entity, ReadingList domain) {
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
    }

}