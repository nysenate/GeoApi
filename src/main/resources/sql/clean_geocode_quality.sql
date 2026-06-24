DELETE FROM public.geocache
WHERE quality = 'ZIP' OR quality = 'CITY';

UPDATE public.geocache
SET quality = 'HOUSE'
WHERE quality = 'POINT';

-- Rename to match the merged Accuracy enum used in code.
ALTER TABLE public.geocache
    RENAME COLUMN quality TO accuracy;
