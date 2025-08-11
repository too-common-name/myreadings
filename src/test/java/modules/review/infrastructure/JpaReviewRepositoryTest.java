package modules.review.infrastructure;

import common.JpaRepositoryTestProfile;
import common.TransactionalTestHelper;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import modules.catalog.core.domain.Book;
import modules.catalog.utils.CatalogTestUtils;
import modules.review.core.usecases.repositories.ReviewRepository;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import java.util.function.Supplier;

@QuarkusTest
@TestProfile(JpaRepositoryTestProfile.class)
public class JpaReviewRepositoryTest extends AbstractReviewRepositoryTest {

    @Inject
    ReviewRepository jpaReviewRepository;
    
    @Inject
    TransactionalTestHelper helper;

    @Override
    protected ReviewRepository getRepository() {
        return jpaReviewRepository;
    }

    @Override
    protected User createAndSaveUser() {
        return helper.saveUser(UserTestUtils.createValidUser());
    }

    @Override
    protected Book createAndSaveBook() {
        return helper.saveBook(CatalogTestUtils.createValidBook());
    }

    @Override
    protected void runTransactionalStep(Runnable step) {
        QuarkusTransaction.requiringNew().run(step::run);
    }
    
    @Override
    protected <T> T runTransactionalStep(Supplier<T> step) {
        return QuarkusTransaction.requiringNew().call(step::get);
    }
}