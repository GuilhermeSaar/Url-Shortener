CREATE TABLE urls (
    id BIGINT NOT NULL,
    code VARCHAR(6) NOT NULL,
    url_original TEXT NOT NULL,
    click_count BIGINT DEFAULT 0 NOT NULL,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_urls PRIMARY KEY (id),
    CONSTRAINT uk_urls_code UNIQUE (code)
);
