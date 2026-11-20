package id.autochem.belajarsesi2.coa.model;

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
public enum AccountType {
    ASSET(true), LIABILITY(false), EQUITY(false), REVENUE(false), EXPENSE(true);

    private final boolean debitNormal;          // saldo normal di debit?

    AccountType(boolean debitNormal) {
        this.debitNormal = debitNormal;
    }

    public boolean isDebitNormal() {
        return debitNormal;
    }

    // Prefix kode: 1 Aset, 2 Liabilitas, 3 Ekuitas, 4 Pendapatan, 5 Beban
    public static AccountType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Kode akun wajib diisi");
        }
        return switch (code.charAt(0)) {
            case '1' -> ASSET;
            case '2' -> LIABILITY;
            case '3' -> EQUITY;
            case '4' -> REVENUE;
            case '5' -> EXPENSE;
            default -> throw new IllegalArgumentException("Prefix salah: " + code);
        };
    }
}
