-- Drop the existing vector_store table and recreate with correct dimensions
-- This SQL script fixes the dimension mismatch issue

-- Drop the existing table (this will delete all existing vector data)
DROP TABLE IF EXISTS vector_store CASCADE;

-- The table will be automatically recreated by Spring AI with the correct dimensions (768)
-- when the application starts next time
