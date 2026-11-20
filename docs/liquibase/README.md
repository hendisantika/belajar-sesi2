# Track B · Liquibase (Pengayaan, Step 9B–13B)

> **Satu aplikasi hanya memakai satu tool migrasi.** Aplikasi utama memakai **Flyway** (Track A).
> File di folder ini adalah referensi Track B dan **tidak** ikut ter-load oleh aplikasi.
> Untuk mencobanya, buat branch terpisah dan gunakan database bersih.

## Step 9B · Beralih ke Liquibase

1. `pom.xml`: ganti dependency migrasi
   - hapus `spring-boot-starter-flyway` dan `flyway-database-postgresql`
   - tambah `spring-boot-starter-liquibase` (Spring Boot 4)
2. `application.yml`: hapus blok `spring.flyway`, lalu tambahkan:

   ```yaml
   spring:
     liquibase:
       change-log: classpath:/db/changelog/db.changelog-master.yaml
     jpa:
       hibernate:
         ddl-auto: validate
   ```
3. Salin folder `src/main/resources/db/changelog/` dari sini ke project, lalu hapus `db/migration/`.
4. Gunakan database bersih: `docker compose down -v && docker compose up -d`.

## Step 10B–12B · Struktur changelog

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml          # daftar include, urutan = urutan eksekusi
└── changes/
    ├── 001-create-account.sql        # 001-create-account, 002-index-account-parent
    └── 002-seed-chart-of-accounts.sql# 003-seed-coa-header, 004-seed-coa-detail
```

Setiap changeset punya `id + author` dan blok `--rollback`. Header dan detail COA dipisah
menjadi dua changeset agar bisa di-rollback terpisah.

## Step 13B · Jalankan, verifikasi & rollback

```bash
./mvnw spring-boot:run
# ChangeSet .../001-create-account.sql::001-create-account::hendi ran successfully
# ChangeSet .../002-seed-chart-of-accounts.sql::004-seed-coa-detail::hendi ran ...

psql -h localhost -U accounting -d accounting \
  -c "select id, author, exectype, orderexecuted from databasechangelog"

# Uji rollback (butuh liquibase-maven-plugin + url, username, password)
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1   # hapus akun detail
./mvnw liquibase:status                                 # 004 kembali pending
./mvnw liquibase:update                                 # jalankan lagi
```

Konfigurasi plugin (tambahkan di `<build><plugins>`):

```xml
<plugin>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-maven-plugin</artifactId>
    <version>${liquibase.version}</version>
    <configuration>
        <changeLogFile>db/changelog/db.changelog-master.yaml</changeLogFile>
        <searchPath>src/main/resources</searchPath>
        <url>jdbc:postgresql://localhost:5432/accounting</url>
        <username>accounting</username>
        <password>${env.DB_PASS}</password>
    </configuration>
</plugin>
```

## Flyway vs Liquibase

| Aspek | Flyway | Liquibase |
|---|---|---|
| Format migrasi | SQL (atau Java) | YAML, XML, JSON, atau SQL |
| Unit perubahan | File berversi `V1_ddmmyyyy_hhmm__`, `V2_...__` | Changeset (id + author) dalam changelog |
| Urutan | Nomor versi di nama file | Urutan di master changelog |
| Rollback | Undo = edisi berbayar; biasanya roll-forward | Rollback bawaan (otomatis/eksplisit) |
| Per environment | `locations` per profile | `contexts` & `labels` |
| Tabel riwayat | `flyway_schema_history` | `DATABASECHANGELOG` + `DATABASECHANGELOGLOCK` |
