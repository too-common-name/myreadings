package modules.catalog.infrastructure.persistence.postgres;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "book")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "book_id")
    private UUID bookId;

    private String isbn;

    @Column(nullable = false)
    private String title;

    @ElementCollection
    private List<String> authors;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    private String publisher;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "page_count")
    private int pageCount;

    @Column(name = "cover_image_id")
    private String coverImageId;

    @Column(name = "original_language")
    private String originalLanguage;
    
}