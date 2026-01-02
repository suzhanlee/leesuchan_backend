-- 인덱스 추가 마이그레이션

-- account_number 컬럼 인덱스 추가 (이미 UNIQUE 제약조건이 있지만 명시적 인덱스)
CREATE INDEX idx_account_number ON account(account_number);

-- created_at 컬럼 인덱스 추가 (최신순 정렬 조회 최적화)
CREATE INDEX idx_account_created_at ON account(created_at);

-- reference_account_number 컬럼 인덱스 추가 (이체 내역 조회 최적화)
CREATE INDEX idx_activity_reference_account_number ON activity(reference_account_number);

-- created_at 컬럼 인덱스 추가 (거래 내역 최신순 조회 최적화)
CREATE INDEX idx_activity_created_at ON activity(created_at);