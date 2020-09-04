-- schema name for constraint data views: constraint_data_schema
-- view name for foreign keys: foreign_keys_relations
-- view name for primary keys: primary_keys

-- MYSQL ---------------------- (a database is basically the same as a schema in mysql)

-- GET foreign keys:

use <database>;
CREATE VIEW foreign_keys_relations AS 
SELECT COLUMN_NAME,
INFORMATION_SCHEMA.TABLE_CONSTRAINTS.TABLE_SCHEMA, 
INFORMATION_SCHEMA.TABLE_CONSTRAINTS.TABLE_NAME, 
REFERENCED_TABLE_SCHEMA, REFERENCED_TABLE_NAME,
INFORMATION_SCHEMA.KEY_COLUMN_USAGE.REFERENCED_COLUMN_NAME as REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
ON INFORMATION_SCHEMA.TABLE_CONSTRAINTS.CONSTRAINT_NAME = INFORMATION_SCHEMA.KEY_COLUMN_USAGE.CONSTRAINT_NAME
WHERE CONSTRAINT_TYPE = 'FOREIGN KEY';

-- GET primary keys

CREATE VIEW primary_keys AS
SELECT kc.column_name, kc.table_name, kc.table_schema
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kc
USING(constraint_name,table_schema,table_name)
WHERE tc.constraint_type='PRIMARY KEY';

-- POSTGRESQL ----------------

-- get FOREIGN KEYS

CREATE SCHEMA IF NOT EXISTS constraint_data_schema;

create view constraint_data_schema.foreign_keys_relations as
SELECT
    tc.table_schema::varchar, 
    tc.constraint_name::varchar, 
    tc.table_name::varchar, 
    kcu.column_name::varchar, 
    ccu.table_schema::varchar AS REFERENCED_TABLE_SCHEMA,
    ccu.table_name::varchar AS REFERENCED_TABLE_NAME,
    ccu.column_name::varchar AS REFERENCED_COLUMN_NAME 
FROM 
    information_schema.table_constraints AS tc 
    JOIN information_schema.key_column_usage AS kcu
      ON tc.constraint_name = kcu.constraint_name
      AND tc.table_schema = kcu.table_schema
    JOIN information_schema.constraint_column_usage AS ccu
      ON ccu.constraint_name = tc.constraint_name
      AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY';

-- get PRIMARY KEYS

CREATE VIEW constraint_data_schema.primary_keys AS
SELECT information_schema.key_column_usage.table_schema::varchar, 
information_schema.key_column_usage.table_name::varchar, 
information_schema.key_column_usage.column_name::varchar
FROM information_schema.key_column_usage 
JOIN information_schema.table_constraints 
ON information_schema.table_constraints.constraint_name = information_schema.key_column_usage.constraint_name
WHERE information_schema.table_constraints.constraint_type = 'PRIMARY KEY';



