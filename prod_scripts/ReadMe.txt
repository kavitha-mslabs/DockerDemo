Call the scripts in the below order

Step 1

If you have existing schema run the below file.

@@drop_script.sql

Run the above file thrice to make sure all are deleted. (First run due to table reference it may not remove all tables)

Step 2

Run the schema creation 

@@schema_setup.sql

Step 3

Run the insert script of general tables

@@insert_script.sql

Step 4

Run the insert script of section

@@section_insert.sql


Step 5

Run the insert script of section

@@insert_district.sql


Step 6

Run the insert script of call center user

@@call_center_user.sql


Step 7

Run the insert script of admin user

@@admin_user_insert.sql



Step 8

Run the views

@@create_view.sql



Step 9

Run the insert script of field worker

@@field_worker_insert.sql