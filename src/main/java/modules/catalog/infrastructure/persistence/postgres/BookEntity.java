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
@Table(name = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "book_id")
    private UUID bookId;

    @Column(name = "isbn", nullable = false, unique = true)
    private String isbn;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    private List<String> authors;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "publisher", length = 255)
    private String publisher;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "page_count")
    private int pageCount;

    @Column(name = "cover_image_id", length = 255)
    private String coverImageId;

    @Column(name = "original_language", length = 50)
    private String originalLanguage;
    
}