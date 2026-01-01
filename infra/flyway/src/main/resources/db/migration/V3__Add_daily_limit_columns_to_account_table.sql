-- Account 테이블에 일일 한도 관련 컬럼 추가
ALTER TABLE account ADD COLUMN daily_withdraw_amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE account ADD COLUMN daily_transfer_amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE account ADD COLUMN last_withdraw_date DATE NULL;
ALTER TABLE account ADD COLUMN last_transfer_date DATE NULL;
