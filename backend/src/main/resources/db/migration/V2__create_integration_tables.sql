-- Owning module: integration.

create table integration_installation (
    id                       uuid        primary key,
    organization_id          uuid        not null references organization (id) on delete cascade,
    provider                 text        not null,
    provider_installation_id bigint      not null,
    provider_account_id      bigint      not null,
    provider_account_login   text        not null,
    status                   text        not null,
    created_at               timestamptz not null default now(),
    updated_at               timestamptz not null default now(),
    version                  bigint      not null default 0,
    constraint uq_integration_installation_provider unique (provider, provider_installation_id),
    constraint ck_integration_installation_provider check (provider in ('GITHUB')),
    constraint ck_integration_installation_ids check (provider_installation_id > 0 and provider_account_id > 0),
    constraint ck_integration_installation_login check (char_length(provider_account_login) between 1 and 100),
    constraint ck_integration_installation_status check (status in ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

create index ix_integration_installation_organization on integration_installation (organization_id);

-- Only signature-verified deliveries are stored. Raw payloads are not retained; the digest is kept as evidence.
create table webhook_delivery (
    id                       uuid        primary key,
    organization_id          uuid        references organization (id) on delete cascade,
    provider                 text        not null,
    provider_delivery_id     text        not null,
    provider_installation_id bigint,
    event_type               text        not null,
    event_action             text,
    payload_sha256           text        not null,
    processing_status        text        not null,
    received_at              timestamptz not null default now(),
    processed_at             timestamptz,
    failure_reason           text,
    constraint uq_webhook_delivery_provider_delivery unique (provider, provider_delivery_id),
    constraint ck_webhook_delivery_provider check (provider in ('GITHUB')),
    constraint ck_webhook_delivery_delivery_id check (char_length(provider_delivery_id) between 1 and 64),
    constraint ck_webhook_delivery_installation check (provider_installation_id > 0),
    constraint ck_webhook_delivery_event_type check (char_length(event_type) between 1 and 64),
    constraint ck_webhook_delivery_event_action check (char_length(event_action) between 1 and 64),
    constraint ck_webhook_delivery_digest check (payload_sha256 ~ '^[0-9a-f]{64}$'),
    constraint ck_webhook_delivery_status check (processing_status in ('RECEIVED', 'UNBOUND', 'PROCESSED', 'FAILED')),
    constraint ck_webhook_delivery_bound check (organization_id is not null or processing_status = 'UNBOUND'),
    constraint ck_webhook_delivery_failure_reason check (char_length(failure_reason) <= 1024)
);

create index ix_webhook_delivery_organization_received on webhook_delivery (organization_id, received_at desc);
