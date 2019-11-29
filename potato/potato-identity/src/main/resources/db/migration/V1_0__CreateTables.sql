create table user_detail (
    id int(11) NOT NULL AUTO_INCREMENT,
    user_id char(36) NOT NULL,
    first_name varchar(250) NOT NULL,
    last_name varchar(250) NOT NULL,
    email varchar(250) NOT NULL,
    phone_number varchar(250) NOT NULL,
    create_time timestamp,
    update_time timestamp,
    primary key(id),
    key user_id (user_id),
    key user_details_userid_updattime (user_id, update_time),
    key user_id_deleted (update_time)
) engine=InnoDB default charset=utf8;


create table address (
      id int(11) NOT NULL AUTO_INCREMENT,
      user_id char(36) NOT NULL,
      city varchar(25) NOT NULL,
      address_line_1 varchar(250) NOT NULL,
      address_line_2 varchar(250) DEFAULT NULL,
      zip_code char(6) NOT NULL,
      create_time timestamp,
      update_time timestamp,
      primary key(id),
      key user_id (user_id),
      key addresses_user_id_deleted (user_id, update_time)
) engine=InnoDB default charset=utf8;