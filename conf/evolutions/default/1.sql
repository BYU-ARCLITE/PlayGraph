# --- First database schema

# --- !Ups

create table graph (
  id                bigint not null auto_increment,
  startNode         bigint not null,
  primary key(id)
);

create table graphSession (
  id                bigint not null auto_increment,
  graph             bigint not null,
  state             text,
  started           bigint not null,
  finished          bigint not null,
  publicKey         varchar(255) not null,
  primary key(id)
);

create table node (
  id                bigint not null auto_increment,
  contentId         bigint not null,
  contentType       varchar(255) not null,
  transitions       longtext,
  primary key(id)
);

create table nodeContent (
  id                bigint not null auto_increment,
  content           longtext,
  primary key(id)
);

create table nodePool (
  id                bigint not null auto_increment,
  nodes             text,
  script            text,
  primary key(id)
);

create table `user` (
  id                bigint not null auto_increment,
  username          varchar(255) not null,
  password          varchar(255) not null,
  authKeys          text,
  primary key(id)
);

create table authToken (
  publicKey         varchar(255) not null,
  secretKey         varchar(255) not null,
  permission        varchar(128) not null,
  name              varchar(255) not null
);



# --- !Downs

drop table if exists graph;
drop table if exists graphSession;
drop table if exists node;
drop table if exists nodeContent;
drop table if exists nodePool;
drop table if exists `user`;
drop table if exists authToken;