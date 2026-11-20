package id.autochem.belajarsesi2.coa.service;

import id.autochem.belajarsesi2.coa.model.Account;
import id.autochem.belajarsesi2.coa.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

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
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repo;             // constructor injection

    @Transactional
    public Account create(String code, String name, String parentCode) {
        if (repo.existsByCode(code)) {
            throw new IllegalArgumentException("Kode sudah ada: " + code);
        }
        Account parent = parentCode == null ? null : getByCode(parentCode);
        return repo.save(Account.of(code, name, parent));
    }

    @Transactional(readOnly = true)
    public Account get(Long id) {                     // findById
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Akun tidak ditemukan: id=" + id));
    }

    @Transactional(readOnly = true)
    public Account getByCode(String code) {
        return repo.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Akun tidak ditemukan: code=" + code));
    }

    @Transactional(readOnly = true)
    public List<Account> findAll() {                  // findAll
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Account> activeAccounts() {
        return repo.findByActiveTrueOrderByCode();
    }

    @Transactional
    public Account rename(String code, String newName) {
        Account account = getByCode(code);
        account.rename(newName);                      // dirty checking: tanpa save()
        return account;
    }

    @Transactional
    public void deactivate(String code) {
        getByCode(code).deactivate();
    }
}
