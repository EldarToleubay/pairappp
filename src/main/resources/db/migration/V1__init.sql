create table users (
    id uuid primary key,
    name varchar(120) not null,
    email varchar(180) not null unique,
    password_hash varchar(255) not null,
    created_at timestamptz not null
);

create table pairs (
    id uuid primary key,
    user_a_id uuid not null,
    user_b_id uuid not null,
    status varchar(20) not null,
    created_at timestamptz not null,
    constraint pairs_unique_users unique (user_a_id, user_b_id)
);

create table pair_invites (
    id uuid primary key,
    from_user_id uuid not null,
    code varchar(16) not null unique,
    expires_at timestamptz not null,
    used_at timestamptz
);

create table mood_requests (
    id uuid primary key,
    pair_id uuid not null,
    from_user_id uuid not null,
    to_user_id uuid not null,
    status varchar(20) not null,
    created_at timestamptz not null,
    expires_at timestamptz not null,
    answered_at timestamptz
);

create table mood_responses (
    id uuid primary key,
    request_id uuid not null unique,
    base_feeling varchar(20) not null,
    mode varchar(20) not null,
    avoid jsonb,
    note_preset varchar(20),
    valid_until timestamptz not null,
    created_at timestamptz not null
);

create index idx_pairs_user_a on pairs(user_a_id);
create index idx_pairs_user_b on pairs(user_b_id);
create index idx_invites_from_user on pair_invites(from_user_id);
create index idx_mood_requests_to_user on mood_requests(to_user_id, status);
create index idx_mood_requests_pair_created on mood_requests(pair_id, created_at);
create index idx_mood_responses_valid on mood_responses(valid_until);
