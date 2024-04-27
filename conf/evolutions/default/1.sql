# --- !Ups
CREATE SCHEMA auth;

CREATE EXTENSION citext;
CREATE DOMAIN email_type AS citext
    CHECK ( value ~
            '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$' );

CREATE TABLE auth.user
(
    id           UUID    NOT NULL PRIMARY KEY,
    first_name   VARCHAR,
    last_name    VARCHAR,
    email        email_type unique,
    role_id      INT     NOT NULL,
    activated    BOOLEAN NOT NULL,
    avatar_url   VARCHAR,
    signed_up_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE auth.login_info
(
    id           BIGSERIAL NOT NULL PRIMARY KEY,
    provider_id  VARCHAR,
    provider_key VARCHAR,
    unique (provider_id, provider_key)
);

CREATE TABLE auth.user_login_info
(
    user_id       UUID   NOT NULL,
    login_info_id BIGINT NOT NULL,
    CONSTRAINT auth_user_login_info_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id) ON DELETE CASCADE,
    CONSTRAINT auth_user_login_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id) ON DELETE CASCADE,
    primary key (user_id, login_info_id)
);

CREATE TABLE auth.oauth1_info
(
    id            BIGSERIAL NOT NULL PRIMARY KEY,
    token         VARCHAR   NOT NULL,
    secret        VARCHAR   NOT NULL,
    login_info_id BIGINT    NOT NULL,
    CONSTRAINT auth_oauth1_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id) ON DELETE CASCADE
);

CREATE TABLE auth.oauth2_info
(
    id            BIGSERIAL NOT NULL PRIMARY KEY,
    access_token  VARCHAR   NOT NULL,
    token_type    VARCHAR,
    expires_in    INT,
    refresh_token VARCHAR,
    login_info_id BIGINT    NOT NULL,
    CONSTRAINT auth_oauth2_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id) ON DELETE CASCADE
);

CREATE TABLE auth.password_info
(
    hasher        VARCHAR NOT NULL,
    password      VARCHAR NOT NULL,
    salt          VARCHAR,
    login_info_id BIGINT  NOT NULL PRIMARY KEY,
    CONSTRAINT auth_password_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id) ON DELETE CASCADE
);

CREATE TABLE auth.token
(
    id      UUID        NOT NULL PRIMARY KEY,
    user_id UUID        NOT NULL,
    expiry  TIMESTAMPTZ NOT NULL,
    CONSTRAINT auth_token_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id) ON DELETE CASCADE
);

CREATE TABLE auth.openid_info
(
    id            VARCHAR(128)   NOT NULL PRIMARY KEY,
    login_info_id BIGINT NOT NULL,
    CONSTRAINT auth_openid_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id) ON DELETE CASCADE
);

CREATE TABLE auth.openid_attributes
(
    open_id    VARCHAR(128) NOT NULL REFERENCES auth.openid_info (id) ON DELETE CASCADE,
    key   VARCHAR NOT NULL,
    value VARCHAR NOT NULL
);

CREATE TABLE auth.google_totp_info
(
    id            SERIAL PRIMARY KEY,
    shared_key    VARCHAR(255) NOT NULL,
    login_info_id BIGINT REFERENCES auth.login_info (id) ON DELETE CASCADE
);

CREATE TABLE auth.google_totp_credentials
(
    id           SERIAL PRIMARY KEY,
    totp_info_id INTEGER REFERENCES auth.google_totp_info (id) ON DELETE CASCADE,
    qr_url       VARCHAR(255) NOT NULL
);

CREATE TABLE auth.totp_info_scratch_codes
(
    google_totp_info_id INTEGER REFERENCES auth.google_totp_info (id) ON DELETE CASCADE,
    password_info_id    INTEGER REFERENCES auth.password_info (login_info_id) ON DELETE CASCADE,
    PRIMARY KEY (google_totp_info_id, password_info_id)
);

CREATE TABLE auth.totp_cred_scratch_codes
(
    id                  SERIAL PRIMARY KEY,
    google_totp_cred_id INTEGER REFERENCES auth.google_totp_credentials (id) ON DELETE CASCADE,
    scratch_code        VARCHAR(255) NOT NULL
);

# --- !Downs
DROP TABLE auth.totp_cred_scratch_codes;
DROP TABLE auth.totp_info_scratch_codes;
DROP TABLE auth.google_totp_credentials;
DROP TABLE auth.google_totp_info;
DROP TABLE auth.openid_attributes;
DROP TABLE auth.openid_info;
DROP TABLE auth.token;
DROP TABLE auth.password_info;
DROP TABLE auth.oauth2_info;
DROP TABLE auth.oauth1_info;
DROP TABLE auth.user_login_info;
DROP TABLE auth.login_info;
DROP TABLE auth.user;
DROP DOMAIN email_type;
DROP EXTENSION citext;
DROP SCHEMA auth;