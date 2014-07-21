-- create campsite database user and grant database privileges
create user 'campsite' identified by 'p4ssw0rd';
grant usage on *.* to 'campsite'@'%' identified by 'p4ssw0rd';
grant usage on *.* to 'campsite'@'localhost' identified by 'p4ssw0rd';
grant all privileges on campsite.* to 'campsite'@'%';
grant all privileges on campsite_acl.* to 'campsite'@'%';
