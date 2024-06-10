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

## 1. How to use API calls to update instances
### How to execute application
Application is packages in jar file that can be used as following:
```java -DokapiUrl=http://localhost:9130 -Dtenant=diku -Dusername=diku_admin -Dpassword=admin -jar instances-sync-1.0.jar```

### List of input system properties

| Property                        | Default | Description              |
|---------------------------------|---------|--------------------------|
| `okapiUrl`                      | `NA`    | Okapi url                |
| `tenant`                        | `NA`    | Tenant to process        |
| `username`                      | `NA`    | Admin username for login |
| `password`                      | `NA`    | Admin password for login |
| `chunkSize`                     | `1000`  | Chunk size to process    |

### Example of execution
```
14:47:58.732 [main] INFO  org.folio.Main - Provided okapiUrl: http://localhost:9130
14:47:58.733 [main] INFO  org.folio.Main - Provided tenant: diku
14:47:58.733 [main] INFO  org.folio.Main - Provided username: diku_admin
14:47:58.733 [main] INFO  org.folio.Main - Provided password: ***
14:47:58.733 [main] INFO  org.folio.Main - Using default chunkSize: 1000
14:47:59.189 [main] INFO  org.folio.Main - Retrieved total number of instances matched date filter: 36
14:47:59.189 [main] INFO  org.folio.Main - Calculated total pages: 1 with chunk size: 1000
14:47:59.189 [main] INFO  org.folio.Main - Started processing 1 chunk with chunk size: 1000
14:47:59.392 [main] INFO  org.folio.RestClient - Update 36 instances operation completes with status code: 201
14:47:59.393 [main] INFO  org.folio.Main - Chunk 1 has been processed

```