DELETE FROM CUSTOMER;

INSERT INTO CUSTOMER VALUES(1, 'Alice', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2',
'alice@example.com', '1998-04-10', '東京都中央区1-1-1');
INSERT INTO CUSTOMER VALUES(2, 'Bob', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2',
'bob@example.com', '1988-05-10', '東京都杉並区2-2-2');
INSERT INTO CUSTOMER VALUES(3, 'Carol', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2',
'carol@example.com', '1993-06-10', '東京都文教区3-3-3');
INSERT INTO CUSTOMER VALUES(4, 'Dave', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2',
'dave@example.com', '1990-07-10', '東京都品川区4-4-4');
INSERT INTO CUSTOMER VALUES(5, 'Ellen', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2',
'ellen@example.com', '1999-08-10', '東京都中野区5-5-5');

-- パスワードは"password"