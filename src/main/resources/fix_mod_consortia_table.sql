DO $$
DECLARE
    target_tenant TEXT:= 'consortium';
    source_tenant TEXT:= 'college';
    record RECORD;
BEGIN
    -- Delete existing records in <source_tenant>_mod_consortia where target_tenant_id matches the target tenant
    DELETE FROM college_mod_consortia.sharing_instance
    WHERE target_tenant_id = target_tenant;

    -- Select records from the  <target_tenant>_mod_inventory_storage.instance table where the "source" is 'CONSORTIUM-MARC'
    WITH record_from_target_tenant as (
        SELECT id FROM consortium_mod_inventory_storage.instance
        WHERE jsonb->>'source' = 'CONSORTIUM-MARC'
    )
    -- Insert each record into <source_tenant>_mod_consortia
    INSERT INTO college_mod_consortia.sharing_instance (id, instance_id, source_tenant_id, target_tenant_id, status)
    SELECT rs.id, rs.id, source_tenant, target_tenant, 'COMPLETED'
    FROM record_from_target_tenant as rs;
END $$;