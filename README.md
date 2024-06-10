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
15:23:48.958 [main] INFO  org.folio.Main - Provided okapiUrl: http://localhost:9130
15:23:48.960 [main] INFO  org.folio.Main - Provided tenant: diku
15:23:48.960 [main] INFO  org.folio.Main - Provided username: diku_admin
15:23:48.960 [main] INFO  org.folio.Main - Provided password: ***
15:23:48.960 [main] INFO  org.folio.Main - Provided chunkSize: 10
15:23:50.019 [main] INFO  org.folio.Main - Retrieved total number of instances matched date filter: 36
15:23:50.019 [main] INFO  org.folio.Main - Calculated total pages: 4 with chunk size: 10
15:23:50.019 [main] INFO  org.folio.Main - Calculated total chunks: 4 with chunk size: 10
15:23:50.019 [main] INFO  org.folio.Main - Started retrieving data for chunk: 1
15:23:50.213 [main] INFO  org.folio.Main - Started retrieving data for chunk: 2
15:23:50.247 [main] INFO  org.folio.Main - Started retrieving data for chunk: 3
15:23:50.283 [main] INFO  org.folio.Main - Started retrieving data for chunk: 4
15:23:50.351 [main] INFO  org.folio.Main - Total retrieved instances: 36 into 4 chunks
15:23:50.431 [main] INFO  org.folio.RestClient - Update 10 instances operation completes with status code: 201
15:23:50.431 [main] INFO  org.folio.Main - Chunk 1 has been processed
15:23:50.541 [main] INFO  org.folio.RestClient - Update 10 instances operation completes with status code: 201
15:23:50.541 [main] INFO  org.folio.Main - Chunk 2 has been processed
15:23:50.669 [main] INFO  org.folio.RestClient - Update 10 instances operation completes with status code: 201
15:23:50.669 [main] INFO  org.folio.Main - Chunk 3 has been processed
15:23:50.783 [main] INFO  org.folio.RestClient - Update 6 instances operation completes with status code: 201
15:23:50.783 [main] INFO  org.folio.Main - Chunk 4 has been processed

Process finished with exit code 0

```