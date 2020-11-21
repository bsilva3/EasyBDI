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
