**STEP 1 - installing**

1. Install the following packages:
```
sudo apt update
sudo apt install git 
sudo apt install default-jdk 
sudo apt install maven 
```

2. Clone the repository using

`git clone https://github.com/batreller/NESColConnect`

3. Install Docker using [documentation on their website](https://docs.docker.com/engine/install/ubuntu/)

**STEP 2 - setting everything up**
1. Create docker container with MySQL and Redis by running

`sudo docker compose up` from NESColConnect directory

2. Connect to the database and create 2 tables using the following commands:

```
create table nescol_student
(
    student_id varchar(255)         not null
        primary key,
    name       varchar(255)         null,
    registered tinyint(1) default 0 null,
    surname    varchar(255)         null
);

create table user
(
    id     bigint auto_increment
        primary key,
    secret varchar(64) not null
);
```

Fill the nescol_student table with some test students, this table used to prevent not NESCol students creating accounts in NESColConnect

**STEP 3 - starting web server**
1. Build java application using maven

`mvn clean package` from NESColConnect directory

2. Run it built application using

`java -jar target/NESColConnect-1.0-SNAPSHOT.jar`

**Done!**
