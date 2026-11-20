package id.autochem.belajarsesi2.coa;

import id.autochem.belajarsesi2.TestcontainersConfiguration;
import id.autochem.belajarsesi2.coa.model.Account;
import id.autochem.belajarsesi2.coa.model.AccountType;
import id.autochem.belajarsesi2.coa.repository.AccountRepository;
import id.autochem.belajarsesi2.coa.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi2
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 21/11/26
 * Time: 02.47
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional          // setiap test di-rollback, data seed tetap 13 akun
class AccountServiceTests {

    @Autowired
    AccountService service;

    @Autowired
    AccountRepository repo;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void flywayAppliedTwoMigrations() {
        List<String> versions = jdbc.queryForList(
                "select version from flyway_schema_history where success order by installed_rank",
                String.class);
        assertThat(versions).containsExactly("1", "2");
    }

    @Test
    void seedContainsThirteenAccountsInHierarchy() {
        assertThat(repo.count()).isEqualTo(13);
        Account kas = repo.findByCode("1101").orElseThrow();
        assertThat(kas.getType()).isEqualTo(AccountType.ASSET);
        assertThat(kas.getParent().getCode()).isEqualTo("1000");
        assertThat(kas.isHeader()).isFalse();
    }

    @Test
    void createSaveFindByIdAndFindAll() {
        Account saved = service.create("1103", "Kas Kecil", "1000");

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getType()).isEqualTo(AccountType.ASSET);
        assertThat(service.get(saved.getId()).getName()).isEqualTo("Kas Kecil");
        assertThat(service.findAll()).hasSize(14);
    }

    @Test
    void rejectDuplicateCode() {
        assertThatThrownBy(() -> service.create("1101", "Kas Lagi", "1000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kode sudah ada");
    }

    @Test
    void rejectTypeDifferentFromParent() {
        assertThatThrownBy(() -> service.create("2199", "Salah Induk", "1000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipe harus sama dengan induk");
    }

    @Test
    void rejectUnknownParent() {
        assertThatThrownBy(() -> service.create("1199", "Tanpa Induk", "9999"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deactivatedAccountExcludedFromActiveList() {
        service.deactivate("1201");
        repo.flush();
        assertThat(service.activeAccounts())
                .extracting(Account::getCode)
                .doesNotContain("1201")
                .isSorted();
    }
}
