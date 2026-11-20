--liquibase formatted sql

--changeset hendi:003-seed-coa-header
INSERT INTO account (code, name, type) VALUES
  ('1000','Aset','ASSET'), ('2000','Liabilitas','LIABILITY'), ('3000','Ekuitas','EQUITY'),
  ('4000','Pendapatan','REVENUE'), ('5000','Beban','EXPENSE');
--rollback DELETE FROM account WHERE parent_id IS NULL;

--changeset hendi:004-seed-coa-detail
INSERT INTO account (code, name, type, parent_id)
SELECT v.code, v.name, v.type, p.id FROM (VALUES
  ('1101','Kas','ASSET','1000'),           ('1102','Bank','ASSET','1000'),
  ('1201','Piutang Usaha','ASSET','1000'), ('2101','Utang Usaha','LIABILITY','2000'),
  ('2105','PPN Keluaran','LIABILITY','2000'), ('3101','Modal Disetor','EQUITY','3000'),
  ('4101','Pendapatan Jasa','REVENUE','4000'), ('5101','Beban Gaji','EXPENSE','5000')
) AS v(code, name, type, parent_code)
JOIN account p ON p.code = v.parent_code;
--rollback DELETE FROM account WHERE parent_id IS NOT NULL;
