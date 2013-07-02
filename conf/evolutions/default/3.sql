# --- First database schema

# --- !Ups

alter table node add column settings text not null;

# --- !Downs

alter table node drop column settings;
