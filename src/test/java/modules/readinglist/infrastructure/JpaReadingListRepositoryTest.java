package modules.readinglist.infrastructure;

import common.JpaRepositoryTestProfile;
import common.TransactionalTestHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
@TestProfile(JpaRepositoryTestProfile.class)
public class JpaReadingListRepositoryTest extends AbstractReadingListRepositoryTest {

    @Inject
    ReadingListRepository jpaReadingListRepository;

    @Inject
    TransactionalTestHelper helper;

    @BeforeEach
    void setUp() {
        this.repository = jpaReadingListRepository;
        
        this.testUser1 = helper.saveUser(UserTestUtils.createValidUser());
        this.testBook1 = helper.saveBook(CatalogTestUtils.createValidBook());
        this.testList1 = helper.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(this.testUser1, "My JPA List"));
    }

    @AfterEach
    void tearDown() {
        var allLists = helper.findReadingListsByUserId(testUser1.getKeycloakUserId());
        if (allLists != null) {
             allLists.forEach(list -> helper.deleteReadingList(list.getReadingListId()));
        }
        
        if (testList1 != null) {
            helper.deleteReadingList(testList1.getReadingListId());
        }
        if (testBook1 != null) {
            helper.deleteBook(testBook1.getBookId());
        }
        if (testUser1 != null) {
            helper.deleteUser(testUser1.getKeycloakUserId());
        }
    }
}