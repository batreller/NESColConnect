CREATE DATABASE nescol_connect;
CREATE USER 'nescol_connect_user'@'localhost' IDENTIFIED BY 'ncon';
GRANT ALL PRIVILEGES ON nescol_connect.* TO 'nescol_connect_user'@'localhost';
FLUSH PRIVILEGES;
ALTER USER 'nescol_connect_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'ncon';
