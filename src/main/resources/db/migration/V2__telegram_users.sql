alter table users alter column email drop not null;
alter table users alter column password_hash drop not null;

alter table users add column telegram_user_id bigint unique;
alter table users add column telegram_username varchar(64);
alter table users add column first_name varchar(120);
alter table users add column last_name varchar(120);
