create table if not exists member (
    id bigint auto_increment not null primary key,
    email varchar(255),
    password varchar(255)
);

create table if not exists plan (
    id bigint auto_increment not null primary key,
    end_date date,
    start_date date,
    status tinyint,
    title varchar(255),
    member_id bigint not null,
    foreign key (member_id) references member(id)
);

create table if not exists repetition (
    id bigint auto_increment not null primary key,
    rep_option int not null,
    rep_value varchar(255)
);

create table if not exists group_table (
   id bigint auto_increment not null primary key,
   title varchar(255),
   rep_id bigint not null,
   foreign key (rep_id) references repetition(id),
   plan_id bigint not null,
   foreign key (plan_id) references plan(id)
);


create table if not exists checkbox (
    id bigint auto_increment not null primary key,
    title varchar(255),
    date_key date,
    checked bit(1),
    group_id bigint not null,
    foreign key (group_id) references group_table(id)
);