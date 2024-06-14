DO $$
BEGIN
    -- There are two variable need to change to appropriate tenant names:
    -- 1. central_tenant - source tenant
    -- 2. member_tenant - target tenant
    -- e.g: central_tenant to consortium, member_tenant to college
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
    SELECT gen_random_uuid(), rs.id, '<central_tenant>', '<member_tenant>', 'COMPLETED'
    FROM record_from_target_tenant as rs;
END $$;