CREATE SEQUENCE CATEGORY_MAPPING_ID_SEQ
  INCREMENT BY 1
  START WITH 1
  MINVALUE 1 NOMAXVALUE NOCYCLE;


CREATE TABLE CATEGORY_MAPPING (
	ID   					NUMBER(10) CONSTRAINT NN_CATEGORY_MAPPING_ID NOT NULL, 
	NAME					VARCHAR2(100) CONSTRAINT NN_CATEGORY_MAPPING_NAME NOT NULL,
	RESCUE_ID				NUMBER(10) NOT NULL CONSTRAINT FK_CATEGORY_MAPPING_RESCUE_ID REFERENCES RESCUE ON DELETE CASCADE,
	CATEGORY_ID				NUMBER(10) NOT NULL CONSTRAINT FK_CATEGORY_ID REFERENCES CATEGORY ON DELETE CASCADE,
	TYPE_CODE				VARCHAR2(5) CONSTRAINT NN_CATEGORY_TYPE_CODE NOT NULL,
	TYPE_CODE_INDEX   		NUMBER(3),
    CREATED_ON				TIMESTAMP DEFAULT SYSDATE NOT NULL,
   	UPDATED_ON				TIMESTAMP DEFAULT SYSDATE NOT NULL
);
     
ALTER TABLE CATEGORY_MAPPING ADD CONSTRAINT PK_CATEGORY_MAPPING_ID PRIMARY KEY (ID);

------ Insert Script --------------

INSERT INTO CATEGORY_MAPPING (ID, NAME, RESCUE_ID,  CATEGORY_ID, TYPE_CODE, TYPE_CODE_INDEX, CREATED_ON, UPDATED_ON) 
VALUES (CATEGORY_MAPPING_ID_SEQ.nextval, 'Billing Related', (select RESCUE_ID from category where name='Billing Related'),
(select ID from category where name='Billing Related'), 'BL', 3, sysdate, sysdate);

INSERT INTO CATEGORY_MAPPING (ID, NAME, RESCUE_ID,  CATEGORY_ID, TYPE_CODE, TYPE_CODE_INDEX, CREATED_ON, UPDATED_ON) 
VALUES (CATEGORY_MAPPING_ID_SEQ.nextval, 'Meter Related', (select RESCUE_ID from category where name='Meter Related'),
(select ID from category where name='Meter Related'), 'ME', 2, sysdate, sysdate);

INSERT INTO CATEGORY_MAPPING (ID, NAME, RESCUE_ID,  CATEGORY_ID, TYPE_CODE, TYPE_CODE_INDEX, CREATED_ON, UPDATED_ON) 
VALUES (CATEGORY_MAPPING_ID_SEQ.nextval, 'Power Failure', (select RESCUE_ID from category where name='Power Failure'),
(select ID from category where name='Power Failure'), 'PF', 0, sysdate, sysdate);

INSERT INTO CATEGORY_MAPPING (ID, NAME, RESCUE_ID,  CATEGORY_ID, TYPE_CODE, TYPE_CODE_INDEX, CREATED_ON, UPDATED_ON) 
VALUES (CATEGORY_MAPPING_ID_SEQ.nextval, 'Voltage Related', (select RESCUE_ID from category where name='Voltage Related'),
(select ID from category where name='Voltage Related'), 'VF', 1, sysdate, sysdate);

INSERT INTO CATEGORY_MAPPING (ID, NAME, RESCUE_ID,  CATEGORY_ID, TYPE_CODE, TYPE_CODE_INDEX, CREATED_ON, UPDATED_ON) 
VALUES (CATEGORY_MAPPING_ID_SEQ.nextval, 'Fire', (select RESCUE_ID from category where name='Fire'),
(select ID from category where name='Fire'), 'FI', 4, sysdate, sysdate);

INSERT INTO CATEGORY_MAPPING (ID, NAME, RESCUE_ID,  CATEGORY_ID, TYPE_CODE, TYPE_CODE_INDEX, CREATED_ON, UPDATED_ON) 
VALUES (CATEGORY_MAPPING_ID_SEQ.nextval, 'Posting Danger to Human Life', (select RESCUE_ID from category where name='Posting Danger to Human Life'),
(select ID from category where name='Posting Danger to Human Life'), 'TH', 5, sysdate, sysdate);

INSERT INTO CATEGORY_MAPPING (ID, NAME, RESCUE_ID,  CATEGORY_ID, TYPE_CODE, TYPE_CODE_INDEX, CREATED_ON, UPDATED_ON) 
VALUES (CATEGORY_MAPPING_ID_SEQ.nextval, 'Theft of Power', (select RESCUE_ID from category where name='Theft of Power'),
(select ID from category where name='Theft of Power'), 'TE', 6, sysdate, sysdate);

INSERT INTO CATEGORY_MAPPING (ID, NAME, RESCUE_ID,  CATEGORY_ID, TYPE_CODE, TYPE_CODE_INDEX, CREATED_ON, UPDATED_ON) 
VALUES (CATEGORY_MAPPING_ID_SEQ.nextval, 'Service Related Complaint', (select RESCUE_ID from category where name='Service Related Complaint'),
(select ID from category where name='Service Related Complaint'), 'SC', 7, sysdate, sysdate);

-----

update CATEGORY_MAPPING set type_code = 'AC' where category_id=21 
update CATEGORY_MAPPING set type_code = 'Application Related Complaint' where category_id=21 
