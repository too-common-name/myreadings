package modules.readinglist.infrastructure;

import common.InMemoryRepositoryTestProfile;
import io.quarkus.test.junit.TestProfile;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.infrastructure.persistence.in_memory.InMemoryReadingListRepository;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryReadingListRepositoryTest extends AbstractReadingListRepositoryTest {
    
    private InMemoryReadingListRepository inMemoryRepo;

    @BeforeEach
    void setUp() {
        inMemoryRepo = new InMemoryReadingListRepository();
        
        this.repository = inMemoryRepo;

        this.testUser1 = UserTestUtils.createValidUser();
        this.testBook1 = CatalogTestUtils.createValidBook();
        this.testList1 = this.repository.create(ReadingListTestUtils.createValidReadingListForUser(this.testUser1, "My In-Memory List"));
    }
}