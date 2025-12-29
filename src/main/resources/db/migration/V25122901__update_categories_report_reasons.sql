-- 1. faq_categories
ALTER TABLE faq_categories
    ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0;

-- 2. inquiry_categories
ALTER TABLE inquiry_categories
    ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0;

-- 3. report_reasons
ALTER TABLE report_reasons
    ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0;

-- 4. withdrawal_reasons
ALTER TABLE withdrawal_reasons
    ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0;