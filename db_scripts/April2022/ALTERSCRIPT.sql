ALTER TABLE CATEGORY ADD ACTIVE_FLAG VARCHAR2(1);

update  CATEGORY set ACTIVE_FLAG = 'Y' --where id = 7

update  CATEGORY set ACTIVE_FLAG = 'N' where id = 3
update  CATEGORY set ACTIVE_FLAG = 'N' where id = 4
update  CATEGORY set ACTIVE_FLAG = 'N' where id = 7
----------------

--sysnonym:
-- Run Against tangedcopublic and tangedcoadmin
CREATE SYNONYM CATEGORY_MAPPING FOR tangedcodemo.CATEGORY_MAPPING;

CREATE SYNONYM CATEGORY_MAPPING_ID_SEQ FOR tangedcodemo.CATEGORY_MAPPING_ID_SEQ;


