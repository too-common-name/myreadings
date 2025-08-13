package modules.readinglist.infrastructure.persistence.postgres.mapper;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.infrastructure.persistence.postgres.ReadingListEntity;
import modules.readinglist.infrastructure.persistence.postgres.ReadingListItemEntity;
import modules.readinglist.web.dto.ReadingListRequestDTO;
import modules.readinglist.web.dto.ReadingListResponseDTO;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi", imports = {UUID.class, LocalDateTime.class})
public interface ReadingListMapper {

    @Mapping(target = "readingListId", ignore = true) 
    @Mapping(target = "creationDate", expression = "java(LocalDateTime.now())")
    @Mapping(target = "books", expression = "java(new java.util.ArrayList<>())")
    ReadingListImpl toDomain(ReadingListRequestDTO dto, User user);

    @Mapping(source = "id", target = "readingListId")
    @Mapping(source = "userId", target = "user", qualifiedByName = "mapUserIdToUserStub")
    @Mapping(source = "items", target = "books", qualifiedByName = "mapItemsToBookStubs")
    ReadingListImpl toDomain(ReadingListEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) 
    @Mapping(source = "user.keycloakUserId", target = "userId")
    @Mapping(target = "items", ignore = true)
    ReadingListEntity toEntity(ReadingList domain);

    @Mapping(source = "books", target = "books", qualifiedByName = "mapBooksToBookIds")
    ReadingListResponseDTO toResponseDTO(ReadingList readingList);

    List<ReadingListResponseDTO> toResponseDTOs(List<ReadingList> readingLists);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "readingListId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "books", ignore = true)
    void updateFromDto(ReadingListRequestDTO dto, @MappingTarget ReadingListImpl readingList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateEntityFromDomain(ReadingList domain, @MappingTarget ReadingListEntity entity);


    @Named("mapBooksToBookIds")
    default List<UUID> mapBooksToBookIds(List<Book> books) {
        if (books == null) {
            return Collections.emptyList();
        }
        return books.stream().map(Book::getBookId).collect(Collectors.toList());
    }

    @Named("mapUserIdToUserStub")
    default User mapUserIdToUserStub(UUID userId) {
        if (userId == null) return null;
        return UserImpl.builder().keycloakUserId(userId).build();
    }

    @Named("mapItemsToBookStubs")
    default List<Book> mapItemsToBookStubs(List<ReadingListItemEntity> items) {
        if (items == null) return Collections.emptyList();
        return items.stream()
            .map(item -> BookImpl.builder().bookId(item.getId().getBookId()).build())
            .collect(Collectors.toList());
    }
}