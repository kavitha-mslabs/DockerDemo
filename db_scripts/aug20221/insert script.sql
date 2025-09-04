UPDATE SUB_CATEGORY SET NAME = 'Fluctuation in Voltage' WHERE NAME = 'Voltage Fluctuations';

INSERT INTO SUB_CATEGORY (ID, NAME, CATEGORY_ID) VALUES 
(SUB_CATEGORY_ID_SEQ.nextval, 'Frequent Interruption',(SELECT ID FROM CATEGORY WHERE NAME = 'Voltage Related'));