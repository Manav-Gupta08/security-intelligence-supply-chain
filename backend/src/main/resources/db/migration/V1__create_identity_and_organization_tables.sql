-- Owning modules: identity (user_account) and organization (organization, membership, team, team_membership).

create table user_account (
    id           uuid        primary key,
    email        text        not null,
    display_name text        not null,
    status       text        not null,
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    version      bigint      not null default 0,
    constraint uq_user_account_email unique (email),
    constraint ck_user_account_email check (email = lower(email) and char_length(email) between 3 and 320),
    constraint ck_user_account_display_name check (char_length(display_name) between 1 and 200),
    constraint ck_user_account_status check (status in ('ACTIVE', 'DISABLED'))
);

create table organization (
    id           uuid        primary key,
    slug         text        not null,
    display_name text        not null,
    status       text        not null,
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    version      bigint      not null default 0,
    constraint uq_organization_slug unique (slug),
    constraint ck_organization_slug check (slug ~ '^[a-z0-9](?:[a-z0-9-]{0,62}[a-z0-9])?$'),
    constraint ck_organization_display_name check (char_length(display_name) between 1 and 200),
    constraint ck_organization_status check (status in ('ACTIVE', 'SUSPENDED'))
);

create table membership (
    id              uuid        primary key,
    organization_id uuid        not null references organization (id) on delete cascade,
    user_id         uuid        not null references user_account (id) on delete cascade,
    role            text        not null,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    version         bigint      not null default 0,
    constraint uq_membership_organization_user unique (organization_id, user_id),
    constraint ck_membership_role check (role in ('OWNER', 'ADMIN', 'SECURITY_ANALYST', 'DEVELOPER', 'VIEWER'))
);

create index ix_membership_user on membership (user_id);

create table team (
    id              uuid        primary key,
    organization_id uuid        not null references organization (id) on delete cascade,
    name            text        not null,
    external_ref    text,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    version         bigint      not null default 0,
    constraint uq_team_organization_name unique (organization_id, name),
    constraint uq_team_organization_id unique (organization_id, id),
    constraint ck_team_name check (char_length(name) between 1 and 100),
    constraint ck_team_external_ref check (char_length(external_ref) between 1 and 255)
);

-- Composite foreign keys guarantee the team and the member belong to the same organization.
create table team_membership (
    organization_id uuid        not null,
    team_id         uuid        not null,
    user_id         uuid        not null,
    created_at      timestamptz not null default now(),
    constraint pk_team_membership primary key (team_id, user_id),
    constraint fk_team_membership_team foreign key (organization_id, team_id)
        references team (organization_id, id) on delete cascade,
    constraint fk_team_membership_member foreign key (organization_id, user_id)
        references membership (organization_id, user_id) on delete cascade
);

create index ix_team_membership_member on team_membership (organization_id, user_id);
