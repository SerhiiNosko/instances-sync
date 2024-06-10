## Instances-Sync Application

## 1. How to use SQL Scripts

### First: SQL Script to fix mod-consortia table
There are two variables need to change to appropriate tenant names:
1. central_tenant - source tenant
2. member_tenant - target tenant
e.g.: central_tenant to consortium, member_tenant to college
 
#### Script
```sql
DO $$
BEGIN
    -- Delete existing records in <central_tenant>_mod_consortia where target_tenant_id matches the target tenant
    DELETE FROM <central_tenant>_mod_consortia.sharing_instance
    WHERE target_tenant_id = '<member_tenant>';

    -- Select records from the <member_tenant>_mod_inventory_storage.instance table where the "source" is 'CONSORTIUM-MARC'
    WITH record_from_target_tenant as (
        SELECT id FROM <member_tenant>_mod_inventory_storage.instance
        WHERE jsonb->>'source' = 'CONSORTIUM-MARC'
    )
    -- Insert each record into <central_tenant>_mod_consortia
    INSERT INTO <central_tenant>_mod_consortia.sharing_instance (id, instance_id, source_tenant_id, target_tenant_id, status)
    SELECT rs.id, rs.id, '<central_tenant>', '<member_tenant>', 'COMPLETED'
    FROM record_from_target_tenant as rs;
END $$;

```

#### Example

```sql
DO $$
BEGIN
    DELETE FROM consortium_mod_consortia.sharing_instance
    WHERE target_tenant_id = 'college';

    WITH record_from_target_tenant as (
        SELECT id FROM college_mod_inventory_storage.instance
        WHERE jsonb->>'source' = 'CONSORTIUM-MARC'
    )
    INSERT INTO consortium_mod_consortia.sharing_instance (id, instance_id, source_tenant_id, target_tenant_id, status)
    SELECT rs.id, rs.id, 'consortium', 'college', 'COMPLETED'
    FROM record_from_target_tenant as rs;
END $$;
```