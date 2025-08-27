package org.modular.playground.readinglist.infrastructure.persistence.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReadingListItemIdEntity implements Serializable {

    @Column(name = "reading_list_id")
    private UUID readingListId;

    @Column(name = "book_id")
    private UUID bookId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReadingListItemIdEntity that = (ReadingListItemIdEntity) o;
        return Objects.equals(readingListId, that.readingListId) &&
               Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readingListId, bookId);
    }
}