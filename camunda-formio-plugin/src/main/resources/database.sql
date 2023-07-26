create table if not exists formio_submission (
     submission_id varchar(255) not null,
     instance_id varchar(255) not null,
     task_id varchar(255) not null,
     submission_name varchar(255) not null,
     value varchar(255) null,
     created_on timestamp,
     created_on varchar(255),
     created_from varchar(255),
     updated_on timestamp,
     updated_by varchar(255),
     updated_from varchar(255),
     constraint pk_formio_submission primary key (submission_id)
);

create table if not exists formio_submission_history (
     submission_history_id varchar(255) not null,
     submission_id varchar(255) not null,
     instance_id varchar(255) not null,
     task_id varchar(255) not null,
     submission_name varchar(255) not null,
     value varchar(255) null,
     created_on timestamp,
     created_on varchar(255),
     created_from varchar(255),
     updated_on timestamp,
     updated_by varchar(255),
     updated_from varchar(255),
     constraint pk_formio_submission_history primary key (submission_history_id)
);

alter table formio_submission
    add column if not exists loop_counter int null;

alter table formio_submission_history
    add column if not exists loop_counter int null;

