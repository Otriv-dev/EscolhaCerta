-- Company contact confirmed by the owner. Flyway applies this once to existing databases.
UPDATE site_settings
SET whatsapp = '5511947067755',
    phone = CASE WHEN phone IS NULL OR TRIM(phone) = '' THEN '(11) 94706-7755' ELSE phone END
WHERE id = 1;
