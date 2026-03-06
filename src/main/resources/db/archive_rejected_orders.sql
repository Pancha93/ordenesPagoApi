-- =====================================================
-- STORED PROCEDURE: Archivar Órdenes Rechazadas Antiguas
-- =====================================================
-- 
-- Propósito:
--   Marcar como archivadas (archived = true) todas las órdenes
--   que fueron rechazadas hace tiempo, para mantener limpia
--   la vista de órdenes activas.
--
-- Parámetros:
--   - p_cutoff_date: Fecha límite. Órdenes actualizadas antes
--     de esta fecha serán archivadas.
--
-- Ejemplo de uso:
--   CALL archive_old_rejected_orders('2025-01-01 00:00:00');
--   
--   Esto archivará todas las órdenes rechazadas que fueron
--   actualizadas antes del 1 de enero de 2025.
--
-- Seguridad:
--   - Maneja transacciones automáticamente (COMMIT si éxito, ROLLBACK si error)
--   - Valida datos de entrada
--   - No modifica órdenes que no sean REJECTED
--   - No borra datos, solo actualiza el flag archived
--
-- Autor: Sistema de Mantenimiento
-- Fecha: 2026-03-05
-- =====================================================
-- PROCEDIMIENTO: archive_old_rejected_orders
-- =====================================================
CREATE OR REPLACE PROCEDURE archive_old_rejected_orders(
    p_cutoff_date TIMESTAMP
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_affected_rows INTEGER := 0;  -- Contador de registros afectados
BEGIN
    -- =====================================================
    -- VALIDACIÓN DE PARÁMETROS
    -- =====================================================
    
    -- Validar que la fecha no sea NULL
    IF p_cutoff_date IS NULL THEN
        RAISE EXCEPTION 'El parámetro cutoff_date no puede ser NULL';
    END IF;
    
    -- Validar que la fecha no sea futura (protección contra errores)
    IF p_cutoff_date > NOW() THEN
        RAISE EXCEPTION 'El parámetro cutoff_date no puede ser una fecha futura: %', p_cutoff_date;
    END IF;
    
    -- Log de inicio
    RAISE NOTICE 'Iniciando archivado de órdenes rechazadas anteriores a: %', p_cutoff_date;
    
    -- =====================================================
    -- TRANSACCIÓN (Manejo automático por PostgreSQL)
    -- =====================================================
    -- El procedimiento maneja transacciones automáticamente:
    -- - Si completa exitosamente → COMMIT automático
    -- - Si hay un error → ROLLBACK automático
    
    -- Actualizar órdenes que cumplan las condiciones:
    -- 1. Estado = REJECTED
    -- 2. Fecha de actualización anterior al cutoff_date
    -- 3. No estén ya archivadas (archived = false)
    UPDATE orders
    SET 
        archived = TRUE,
        updated_at = NOW()  -- Actualizar timestamp de modificación
    WHERE 
        status = 'REJECTED'              -- Solo órdenes rechazadas
        AND updated_at < p_cutoff_date   -- Más antiguas que el cutoff
        AND archived = FALSE;            -- Que no estén ya archivadas
    
    -- Obtener número de registros afectados
    GET DIAGNOSTICS v_affected_rows = ROW_COUNT;
    
    -- Log de éxito
    RAISE NOTICE 'Archivado completado exitosamente. Órdenes archivadas: %', v_affected_rows;
    
    -- Retornar información al cliente
    RAISE INFO 'RESULTADO: % órdenes rechazadas fueron archivadas', v_affected_rows;
    
END;
$$;

-- =====================================================
-- COMENTARIOS Y DOCUMENTACIÓN
-- =====================================================

COMMENT ON PROCEDURE archive_old_rejected_orders(TIMESTAMP) IS 
    'Marca como archivadas las órdenes con estado REJECTED que fueron actualizadas antes de la fecha límite especificada. Maneja transacciones automáticamente (COMMIT al finalizar exitosamente, ROLLBACK en caso de error) y valida parámetros de entrada.';

-- =====================================================
-- VERIFICACIÓN
-- =====================================================
-- Para verificar que el procedimiento fue creado:
-- 
-- SELECT 
--     routine_name, 
--     routine_type,
--     data_type
-- FROM information_schema.routines
-- WHERE routine_name = 'archive_old_rejected_orders';
--
-- =====================================================
-- EJEMPLOS DE USO
-- =====================================================
--
-- 1. Archivar órdenes rechazadas de hace más de 6 meses:
--    CALL archive_old_rejected_orders(NOW() - INTERVAL '6 months');
--
-- 2. Archivar órdenes rechazadas anteriores a una fecha específica:
--    CALL archive_old_rejected_orders('2025-01-01 00:00:00');
--
-- 3. Archivar órdenes rechazadas de hace más de 1 año:
--    CALL archive_old_rejected_orders(NOW() - INTERVAL '1 year');
--
-- 4. Ver cuántas órdenes se archivarían SIN ejecutar el archivado:
--    SELECT COUNT(*) as ordenes_a_archivar
--    FROM orders
--    WHERE status = 'REJECTED'
--      AND updated_at < (NOW() - INTERVAL '6 months')
--      AND archived = FALSE;
--
-- =====================================================
-- PROGRAMACIÓN AUTOMÁTICA (Opcional)
-- =====================================================
-- Para ejecutar este procedimiento automáticamente cada mes,
-- puedes usar pg_cron (extensión de PostgreSQL):
--
-- -- Instalar extensión pg_cron (una sola vez)
-- CREATE EXTENSION pg_cron;
--
-- -- Programar ejecución mensual (primer día del mes a las 2 AM)
-- SELECT cron.schedule(
--     'archive-old-rejected-orders',
--     '0 2 1 * *',
--     $$CALL archive_old_rejected_orders(NOW() - INTERVAL '6 months')$$
-- );
--
-- -- Ver trabajos programados
-- SELECT * FROM cron.job;
--
-- -- Eliminar trabajo programado
-- SELECT cron.unschedule('archive-old-rejected-orders');
--
-- =====================================================
