/* Delete 'Conductor Stringed' reason from 'DangerousPoles' table as it is moved to 'Conductor Snapping' */
delete from closure_reason where id=40

/* Delete 'Issue Attended' reason from 'DangerousPoles' table as it seems to be a Duplicate reason */
delete from closure_reason where id=43