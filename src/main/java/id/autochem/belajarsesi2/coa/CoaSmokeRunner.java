package id.autochem.belajarsesi2.coa;

import id.autochem.belajarsesi2.coa.model.Account;
import id.autochem.belajarsesi2.coa.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
class CoaSmokeRunner implements CommandLineRunner {

    private static final String KAS_KECIL = "1103";

    private final AccountService accounts;

    @Override
    public void run(String... args) {
        // save: idempotent agar restart aplikasi tidak gagal karena kode duplikat
        Account kasKecil = accounts.activeAccounts().stream()
                .filter(a -> a.getCode().equals(KAS_KECIL))
                .findFirst()
                .orElseGet(() -> accounts.create(KAS_KECIL, "Kas Kecil", "1000"));
        log.info("Tersimpan id={} code={}", kasKecil.getId(), kasKecil.getCode());

        log.info("findById: {}", accounts.get(kasKecil.getId()).getName());   // findById
        log.info("Jumlah akun: {}", accounts.findAll().size());               // findAll
        accounts.activeAccounts().forEach(a ->
                log.info("  {} {} [{}]", a.getCode(), a.getName(), a.getType()));
    }
}
// Hanya jalan di profile dev; hapus/nonaktifkan setelah API tersedia (Sesi 3)
