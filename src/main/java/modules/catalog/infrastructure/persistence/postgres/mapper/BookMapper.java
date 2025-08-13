package modules.catalog.infrastructure.persistence.postgres.mapper;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.infrastructure.persistence.postgres.BookEntity;
import modules.catalog.web.dto.BookRequestDTO;
import modules.catalog.web.dto.BookResponseDTO;
import modules.catalog.web.dto.BookUpdateDTO;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "cdi", imports = UUID.class)
public interface BookMapper {

    @Mapping(target = "bookId", ignore = true)
    BookImpl toDomain(BookRequestDTO dto);

    BookResponseDTO toResponseDTO(Book book);

    List<BookResponseDTO> toResponseDTOs(List<Book> books);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "bookId", ignore = true)
    BookEntity toEntity(Book book);

    BookImpl toDomain(BookEntity entity);

    List<Book> toDomainList(List<BookEntity> entities);

    void updateEntityFromDomain(Book book, @MappingTarget BookEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "bookId", ignore = true)
    @Mapping(target = "isbn", ignore = true)
    void updateDomainFromDto(BookUpdateDTO dto, @MappingTarget BookImpl book);
}