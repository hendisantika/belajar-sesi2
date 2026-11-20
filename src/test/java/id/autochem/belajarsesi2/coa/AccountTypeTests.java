package id.autochem.belajarsesi2.coa;

import id.autochem.belajarsesi2.coa.model.Account;
import id.autochem.belajarsesi2.coa.model.AccountType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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
class AccountTypeTests {

    @ParameterizedTest
    @CsvSource({"1101,ASSET,true", "2101,LIABILITY,false", "3101,EQUITY,false",
            "4101,REVENUE,false", "5101,EXPENSE,true"})
    void typeAndNormalBalanceFromPrefix(String code, AccountType expected, boolean debitNormal) {
        assertThat(AccountType.fromCode(code)).isEqualTo(expected);
        assertThat(expected.isDebitNormal()).isEqualTo(debitNormal);
    }

    @ParameterizedTest
    @ValueSource(strings = {"110", "11011", "AB12", "6101"})
    void rejectInvalidCode(String code) {
        assertThatThrownBy(() -> Account.of(code, "Salah", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
