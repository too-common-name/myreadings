package modules.catalog.infrastructure;

import common.JpaRepositoryTestProfile;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import modules.catalog.core.usecases.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
@TestProfile(JpaRepositoryTestProfile.class)
@TestTransaction
public class JpaBookRepositoryTest extends AbstractBookRepositoryTest {

    @Inject
    BookRepository jpaRepository;

    @Override
    protected BookRepository getRepository() {
        return jpaRepository;
    }
    
    @BeforeEach
    @Override
    void setUp() {
        super.setUp();
    }
}