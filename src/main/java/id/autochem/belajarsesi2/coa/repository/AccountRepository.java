package id.autochem.belajarsesi2.coa.repository;

import id.autochem.belajarsesi2.coa.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi2
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 21/11/26
 * Time: 02.46
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByCode(String code);        // where code = ?

    boolean existsByCode(String code);                // cek duplikat

    List<Account> findByActiveTrueOrderByCode();      // akun aktif, urut kode
}
// Gratis dari JpaRepository: save, findById, findAll, count, deleteById
