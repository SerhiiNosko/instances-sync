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
    SELECT rs.id, rs.id, '<central_tenant>', '<member_tenant>', 'COMPLETE'
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
    SELECT rs.id, rs.id, 'consortium', 'college', 'COMPLETE'
    FROM record_from_target_tenant as rs;
END $$;
```

## 2. How to use API calls to update instances
### How to execute application
This java command line application touches the central tenant instance ids by fetching them and updating them with the same data. This update produces a kafka message which will cause the shadow instances for all member tenants to receive the central tenant's instance data. Make sure to run this java command line program only after all member tenants have received the PSQL update to mod-consortia above.
Application is packages in jar file that can be used as following:
```shell
java -DokapiUrl=http://localhost:9130 -Dtenant=<central tenant id> -Dusername=<username> -Dpassword=<password> -jar instances-sync-1.0.jar
```
instances-sync-1.0.jar is located in the target folder, so before running this command it is needed to change directory to target.

### List of input system properties

| Property                        | Default | Description                   |
|---------------------------------|---------|-------------------------------|
| `okapiUrl`                      | `NA`    | Okapi url                     |
| `tenant`                        | `NA`    | The central tenant to process |
| `username`                      | `NA`    | Admin username for login      |
| `password`                      | `NA`    | Admin password for login      |
| `chunkSize`                     | `1000`  | Chunk size to process         |

### Example of execution
```
15:39:21.132 [main] INFO  org.folio.Main - Provided okapiUrl: http://localhost:9130
15:39:21.135 [main] INFO  org.folio.Main - Provided tenant: diku
15:39:21.135 [main] INFO  org.folio.Main - Provided username: diku_admin
15:39:21.135 [main] INFO  org.folio.Main - Provided password: ***
15:39:21.135 [main] INFO  org.folio.Main - Provided chunkSize: 10
15:39:22.199 [main] INFO  org.folio.Main - Retrieved total number of instances matched date filter: 36
15:39:22.199 [main] INFO  org.folio.Main - Calculated total chunks: 4 with chunk size: 10
15:39:22.199 [main] INFO  org.folio.Main - Started retrieving data for chunk: 1
15:39:22.403 [main] INFO  org.folio.Main - Started retrieving data for chunk: 2
15:39:22.448 [main] INFO  org.folio.Main - Started retrieving data for chunk: 3
15:39:22.495 [main] INFO  org.folio.Main - Started retrieving data for chunk: 4
15:39:22.531 [main] INFO  org.folio.Main - Total retrieved instances: 36 into 4 chunks
15:39:22.589 [main] INFO  org.folio.RestClient - Update 10 instances operation completes with status code: 201
15:39:22.589 [main] INFO  org.folio.Main - Chunk 1 has been processed
15:39:22.699 [main] INFO  org.folio.RestClient - Update 10 instances operation completes with status code: 201
15:39:22.699 [main] INFO  org.folio.Main - Chunk 2 has been processed
15:39:22.804 [main] INFO  org.folio.RestClient - Update 10 instances operation completes with status code: 201
15:39:22.804 [main] INFO  org.folio.Main - Chunk 3 has been processed
15:39:23.004 [main] INFO  org.folio.RestClient - Update 6 instances operation completes with status code: 201
15:39:23.004 [main] INFO  org.folio.Main - Chunk 4 has been processed

Process finished with exit code 0

```