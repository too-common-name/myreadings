package org.modular.playground.readinglist.infrastructure.persistence.postgres;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "reading_list_items")
@Data
@EqualsAndHashCode(callSuper = false)
public class ReadingListItemEntity extends PanacheEntityBase {

    @EmbeddedId
    private ReadingListItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("readingListId")
    @JoinColumn(name = "reading_list_id")
    private ReadingListEntity readingList;

}