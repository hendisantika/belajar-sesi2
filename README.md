# belajar-sesi2 · Spring Boot Fundamentals, JPA & Flyway

Materi hands-on **Modul 01 · Sesi 2** (*Enterprise Java & Spring Boot Engineering*) yang
menyertai slide `Sesi 02 - Spring Boot Fundamentals, JPA & Flyway.pptx`.

Studi kasus: **Chart of Accounts (COA)**, yaitu entity `Account` di PostgreSQL yang skemanya dikelola Flyway.

| | |
|---|---|
| Java | 25 |
| Spring Boot | 4.2.0 (Maven) |
| Database | PostgreSQL 16 (Docker Compose) |
| Migrasi | Flyway (Track A, aktif) · Liquibase (Track B, referensi di `docs/liquibase`) |
| Test | JUnit 5 + Testcontainers |

## Tujuan Sesi

- Membuat dan menjalankan project Spring Boot berbasis Maven
- Mengatur package `model`, `service`, `repository`, `config` dengan **constructor injection**
- Menghubungkan ke PostgreSQL lewat `application.yml` + env var, **tanpa password di source code** (selain default lokal)
- Memetakan entity `Account` (id, code, name, type, parent, active) dengan anotasi JPA
- Menulis migrasi Flyway tabel COA dengan key & unique constraint
- Memakai `JpaRepository` (`save`, `findById`, `findAll`) lewat service/runner

## Struktur Project

```
src/main/java/id/autochem/belajarsesi2
├── BelajarSesi2Application.java
└── coa/
    ├── CoaSmokeRunner.java          # CommandLineRunner, hanya profile dev
    ├── model/       Account, AccountType
    ├── repository/  AccountRepository
    └── service/     AccountService
src/main/resources
├── application.yml                  # datasource via env var, ddl-auto: validate
├── application-dev.yml              # log SQL untuk lokal
└── db/migration/
    ├── V1__create_account.sql
    └── V2__seed_chart_of_accounts.sql
compose.yaml                         # PostgreSQL lokal
docs/liquibase/                      # Track B (pengayaan)
```

| Package | Isi |
|---|---|
| `model` | Entity & enum domain |
| `repository` | Akses data (Spring Data JPA) |
| `service` | Aturan bisnis & `@Transactional` |
| `config` | Bean & properti konfigurasi (mulai sesi berikutnya) |

## Step by Step

| Step | Materi | File |
|---|---|---|
| 1 | Generate project (web, data-jpa, postgresql, flyway, validation, lombok) | `pom.xml` |
| 2 | PostgreSQL dengan Docker Compose | `compose.yaml` |
| 3 | Konfigurasi datasource & JPA | `application.yml`, `application-dev.yml` |
| 4 | Enum `AccountType` + saldo normal | `coa/model/AccountType.java` |
| 5 | Entity `Account`: pemetaan kolom | `coa/model/Account.java` |
| 6 | Entity `Account`: aturan bisnis (`of`, `rename`, `deactivate`) | `coa/model/Account.java` |
| 7 | Repository (derived query) | `coa/repository/AccountRepository.java` |
| 8 | Service (constructor injection, transaksi) | `coa/service/AccountService.java` |
| 9A | Migrasi V1: tabel account | `db/migration/V1__create_account.sql` |
| 10A | Seed COA (V2) | `db/migration/V2__seed_chart_of_accounts.sql` |
| 11A | Jalankan & verifikasi | lihat di bawah |
| 9B–13B | Track Liquibase | `docs/liquibase/README.md` |
| 14 | Uji `ddl-auto: validate` | lihat di bawah |

### Aturan bisnis COA

- Kode akun **4 digit**; digit pertama menentukan tipe:
  `1` Aset · `2` Liabilitas · `3` Ekuitas · `4` Pendapatan · `5` Beban
- Saldo normal debit: `ASSET`, `EXPENSE`; kredit: `LIABILITY`, `EQUITY`, `REVENUE`
- Tipe akun anak **harus sama** dengan induknya
- Kode harus unik; akun induk harus sudah ada
- Akun tanpa induk = akun header

## Menjalankan

```bash
# 1. Database lokal
docker compose up -d              # docker compose down -v -> database bersih

# 2. Aplikasi (profile dev aktif otomatis via spring-boot-maven-plugin)
./mvnw spring-boot:run
```

Log yang diharapkan:

```
Successfully validated 2 migrations
Migrating schema "public" to version "1 - create account"
Migrating schema "public" to version "2 - seed chart of accounts"
Successfully applied 2 migrations to schema "public", now at version v2
CoaSmokeRunner : Tersimpan id=14 code=1103
CoaSmokeRunner : findById: Kas Kecil
CoaSmokeRunner : Jumlah akun: 14
```

Saat restart, Flyway mencetak `Schema "public" is up to date` dan runner tidak membuat data duplikat.

### Konfigurasi lewat environment variable

| Env var | Default (lokal) |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/accounting` |
| `DB_USER` | `accounting` |
| `DB_PASS` | `secret` |

```bash
# Contoh: port 5432 sudah terpakai
DB_URL=jdbc:postgresql://localhost:55432/accounting ./mvnw spring-boot:run

# JAR production: tanpa profile dev, password dari env
./mvnw package
DB_PASS=******** java -jar target/belajar-sesi2-0.0.1-SNAPSHOT.jar
```

### Verifikasi di PostgreSQL

```bash
# Riwayat migrasi
psql -h localhost -U accounting -d accounting \
  -c "select version, description, success from flyway_schema_history"

# Hierarki COA
psql -h localhost -U accounting -d accounting -c "
  select c.code, c.name, c.type, p.code as parent
    from account c left join account p on p.id = c.parent_id
   order by c.code"
```

Hasil: 13 akun dari seed (5 header + 8 detail) ditambah `1103 Kas Kecil` dari runner.

### Step 14 · Uji `ddl-auto: validate`

Eksperimen di database bersih: hapus kolom `updated_at` dari `V1__create_account.sql`, lalu jalankan aplikasi.

```
APPLICATION FAILED TO START
org.hibernate.tool.schema.spi.SchemaManagementException:
    Schema-validation: missing column [updated_at] in table [account]
```

Startup yang gagal di sini adalah **fitur**: ketidakcocokan entity dan skema ketahuan sebelum production.
Solusi yang benar adalah menambah **migrasi baru** (`V3__add_updated_at_to_account.sql`), bukan mengedit
migrasi lama dan bukan mengganti `ddl-auto` ke `update`. Kembalikan file V1 setelah eksperimen.

## Test

```bash
./mvnw test        # butuh Docker (Testcontainers menjalankan PostgreSQL 16)
```

- `AccountTypeTests`: tipe dari prefix kode, saldo normal, kode tidak valid
- `AccountServiceTests`: Flyway V1 & V2 tercatat, 13 akun seed, `save`/`findById`/`findAll`,
  kode duplikat, tipe beda dengan induk, induk tidak ada, akun nonaktif
- `BelajarSesi2ApplicationTests`: context load + validasi skema

## Best Practice Flyway

| Lakukan | Hindari |
|---|---|
| Satu perubahan logis per file | Mengedit file yang sudah dijalankan (checksum mismatch) |
| Nama deskriptif: `V7__add_index_journal_date` | `ddl-auto: update` di production |
| Review file migrasi di Pull Request | Mengedit `flyway_schema_history` manual |
| Uji migrasi dari nol di CI (Testcontainers) | `clean` di environment selain lokal |
| Backup sebelum migrasi besar di production | Migrasi berat yang mengunci tabel besar tanpa rencana |

## Checklist & Deliverable

- [ ] Project ter-generate & `./mvnw compile` berhasil
- [ ] PostgreSQL berjalan via Docker Compose
- [ ] `application.yml` memakai env var & `ddl-auto: validate`
- [ ] Enum `AccountType` & entity `Account` lengkap
- [ ] Repository & service create akun berjalan
- [ ] Track A: V1 & V2 sukses, 13 akun tersimpan
- [ ] Track B: changeset tercatat, rollback & update ulang OK (pengayaan)
- [ ] Uji validate: error dipahami & diperbaiki dengan migrasi baru

**Deliverable:** Spring Boot project + koneksi PostgreSQL + COA entity/repository + migration SQL dan hasil query.

**Sesi berikutnya (03):** RESTful COA API, Validation & Error Handling.

## Referensi

- [Flyway Documentation](https://documentation.red-gate.com/flyway)
- [Liquibase Documentation](https://docs.liquibase.com)
- [Spring Boot: Database Migration](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [Evolutionary Database Design](https://martinfowler.com/articles/evodb.html)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
