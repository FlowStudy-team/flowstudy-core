DROP TABLE IF EXISTS fs_code_template;
DROP TABLE IF EXISTS fs_code_run_case_result;
DROP TABLE IF EXISTS fs_code_run;
DROP TABLE IF EXISTS fs_judge_case_result;
DROP TABLE IF EXISTS fs_submission;
DROP TABLE IF EXISTS fs_problem_testcase;
DROP TABLE IF EXISTS fs_problem;
DROP TABLE IF EXISTS fs_chapter;
DROP TABLE IF EXISTS fs_article;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(128),
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    avatar_url VARCHAR(512),
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    status TINYINT NOT NULL DEFAULT 1,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_user_username UNIQUE (username),
    CONSTRAINT uk_sys_user_email UNIQUE (email)
);

CREATE TABLE fs_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(512),
    cover_url VARCHAR(512),
    author_id BIGINT,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE fs_chapter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content_md CLOB NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    estimated_minutes INT,
    status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE fs_problem (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description_md CLOB NOT NULL,
    difficulty VARCHAR(32) NOT NULL DEFAULT 'EASY',
    input_description CLOB,
    output_description CLOB,
    support_languages VARCHAR(255) NOT NULL DEFAULT 'java,cpp,go,python',
    time_limit_ms INT NOT NULL DEFAULT 1000,
    memory_limit_mb INT NOT NULL DEFAULT 256,
    status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    submit_count BIGINT NOT NULL DEFAULT 0,
    accepted_count BIGINT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE fs_problem_testcase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    input_text CLOB NOT NULL,
    expected_output CLOB NOT NULL,
    is_sample TINYINT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE fs_code_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    language VARCHAR(32) NOT NULL,
    template_code CLOB NOT NULL,
    judge_wrapper_code CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_fs_code_template_problem_language UNIQUE (problem_id, language)
);

CREATE TABLE fs_submission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    language VARCHAR(32) NOT NULL,
    code CLOB NOT NULL,
    judge_code CLOB,
    submit_mode VARCHAR(32) NOT NULL DEFAULT 'FULL_PROGRAM',
    status VARCHAR(64) NOT NULL DEFAULT 'PENDING',
    score INT NOT NULL DEFAULT 0,
    time_used_ms INT,
    memory_used_kb INT,
    compile_message CLOB,
    runtime_message CLOB,
    trace_id VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fs_judge_case_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    testcase_id BIGINT,
    case_index INT NOT NULL,
    status VARCHAR(64) NOT NULL,
    time_used_ms INT,
    memory_used_kb INT,
    input_text CLOB,
    actual_output CLOB,
    expected_output CLOB,
    error_message CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_fs_judge_case_submission_index UNIQUE (submission_id, case_index)
);

CREATE TABLE fs_code_run (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    language VARCHAR(32) NOT NULL,
    code CLOB NOT NULL,
    judge_code CLOB,
    submit_mode VARCHAR(32) NOT NULL DEFAULT 'FULL_PROGRAM',
    status VARCHAR(64) NOT NULL DEFAULT 'PENDING',
    time_used_ms INT,
    memory_used_kb INT,
    compile_message CLOB,
    runtime_message CLOB,
    trace_id VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fs_code_run_case_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_id BIGINT NOT NULL,
    testcase_id BIGINT,
    case_index INT NOT NULL,
    status VARCHAR(64) NOT NULL,
    time_used_ms INT,
    memory_used_kb INT,
    input_text CLOB,
    actual_output CLOB,
    expected_output CLOB,
    error_message CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_fs_code_run_case_run_index UNIQUE (run_id, case_index)
);
