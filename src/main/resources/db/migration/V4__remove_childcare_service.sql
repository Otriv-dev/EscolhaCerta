-- The company does not offer childcare. Preserve applied migration checksums.
DELETE FROM contents
WHERE kind = 'SERVICE' AND title = 'Cuidado com crianças';
