create database if not exists potato;
create database if not exists scheduler;
grant all on potato.* to 'walter'@'%';
grant all on scheduler.* to 'walter'@'%';