-- =====================================================
-- TRIGGER: Auditoría Automática de Cambios de Estado
-- =====================================================
-- 
-- Propósito:
--   Registrar automáticamente en order_status_log cada vez
--   que cambie el estado (status) de una orden.
--
-- Tablas involucradas:
--   - orders (tabla principal)
--   - order_status_log (tabla de auditoría)
--
-- Flujo:
--   1. Se actualiza una orden (UPDATE)
--   2. Si el status cambió, el trigger se dispara
--   3. Se inserta un registro en order_status_log con:
--      - ID de la orden
--      - Estado anterior
--      - Estado nuevo
--      - Usuario que hizo el cambio
--      - Timestamp del cambio
--
-- Autor: Sistema de Auditoría
-- Fecha: 2026-03-05
-- =====================================================

-- Primero, eliminar el trigger si existe (para poder re-ejecutar el script)
DROP TRIGGER IF EXISTS audit_order_status_change ON orders;

-- Eliminar la función si existe
DROP FUNCTION IF EXISTS fn_audit_order_status_change();

-- =====================================================
-- FUNCIÓN: fn_audit_order_status_change
-- =====================================================
-- Esta función contiene la lógica que se ejecutará
-- cuando el trigger se dispare.
-- =====================================================
CREATE OR REPLACE FUNCTION fn_audit_order_status_change()
RETURNS TRIGGER AS $$
BEGIN
    -- Solo insertar si el status realmente cambió
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        
        INSERT INTO order_status_log (
            order_id,
            previous_status,
            new_status,
            changed_by,
            changed_at
        ) VALUES (
            NEW.id,                          -- ID de la orden actualizada
            OLD.status,                      -- Estado anterior
            NEW.status,                      -- Estado nuevo
            COALESCE(
                NEW.approved_by,             -- Si fue aprobado/rechazado, usar approved_by
                NEW.created_by               -- Sino, usar el creador de la orden
            ),                               -- Usuario que realizó el cambio
            NOW()                            -- Timestamp actual del cambio
        );
        
        -- Log opcional para debugging (comentar en producción)
        RAISE NOTICE 'Auditoría registrada: Orden % cambió de % a %', 
            NEW.id, OLD.status, NEW.status;
    END IF;
    
    -- Retornar NEW para que la actualización continúe
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- TRIGGER: audit_order_status_change
-- =====================================================
-- Se dispara DESPUÉS de cada UPDATE en la tabla orders.
-- Ejecuta la función fn_audit_order_status_change.
-- =====================================================
CREATE TRIGGER audit_order_status_change
    AFTER UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION fn_audit_order_status_change();

-- =====================================================
-- VERIFICACIÓN Y TESTING
-- =====================================================
-- Para verificar que el trigger fue creado correctamente:
-- 
-- SELECT 
--     trigger_name, 
--     event_manipulation, 
--     event_object_table, 
--     action_timing
-- FROM information_schema.triggers
-- WHERE trigger_name = 'audit_order_status_change';
--
-- =====================================================
-- EJEMPLO DE PRUEBA
-- =====================================================
-- Para probar el trigger manualmente:
--
-- 1. Crear una orden de prueba:
--    INSERT INTO orders (description, amount, status, created_by, archived, created_at, updated_at)
--    VALUES ('Test Order', 1000.00, 'PENDING', 1, false, NOW(), NOW());
--
-- 2. Actualizar el status:
--    UPDATE orders SET status = 'APPROVED', approved_by = 1 WHERE id = 1;
--
-- 3. Verificar que se creó el log:
--    SELECT * FROM order_status_log WHERE order_id = 1;
--
-- =====================================================

COMMENT ON TRIGGER audit_order_status_change ON orders IS 
    'Registra automáticamente en order_status_log cada cambio de estado de una orden';

COMMENT ON FUNCTION fn_audit_order_status_change() IS 
    'Función que implementa la lógica de auditoría para cambios de estado en órdenes';
