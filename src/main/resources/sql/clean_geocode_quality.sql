DELETE FROM public.geocache
WHERE quality = 'ZIP' OR quality = 'CITY';

UPDATE public.geocache
SET quality = 'HOUSE'
WHERE quality = 'POINT';
