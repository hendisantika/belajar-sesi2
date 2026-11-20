package id.autochem.belajarsesi2.coa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

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
@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // dipakai Hibernate, bukan kode kita
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                  // BIGSERIAL

    @Column(nullable = false, unique = true, length = 20)
    private String code;                              // '1101'

    @Column(nullable = false, length = 150)
    private String name;                              // 'Kas'

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;                         // 'ASSET'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Account parent;                           // akun induk

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private Long version;                             // optimistic lock

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // Factory: satu-satunya cara membuat akun baru yang valid
    public static Account of(String code, String name, Account parent) {
        if (code == null || !code.matches("\\d{4}")) {
            throw new IllegalArgumentException("Kode akun harus 4 digit: " + code);
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama akun wajib diisi");
        }
        Account a = new Account();
        a.code = code;
        a.name = name;
        a.type = AccountType.fromCode(code);          // tipe dari prefix kode
        if (parent != null && parent.type != a.type) {
            throw new IllegalArgumentException("Tipe harus sama dengan induk");
        }
        a.parent = parent;
        return a;
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Nama akun wajib diisi");
        }
        this.name = newName;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isHeader() {
        return parent == null;
    }
}
