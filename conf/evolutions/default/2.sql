# --- First database schema

# --- !Ups

alter table graph add column authorId bigint not null default 0;

# --- !Downs

alter table graph drop column authorId;
