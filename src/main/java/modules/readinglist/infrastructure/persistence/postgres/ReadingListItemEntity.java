package modules.readinglist.infrastructure.persistence.postgres;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reading_list_items")
@Data
public class ReadingListItemEntity {

    @EmbeddedId
    private ReadingListItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("readingListId")
    @JoinColumn(name = "reading_list_id")
    private ReadingListEntity readingList;

    @Column(name = "book_id")
    private UUID bookId;
}