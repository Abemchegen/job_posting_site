INSERT INTO users (name, email, phonenumber, birthdate, password, role, company_id, verificationCode, emailVerificationExpiry, emailVerified)
VALUES ('Abem', 'abem@gmail.com', '0909112233', '2000-10-10', '$2a$10$qxLu8GMDIoqcgdTxnqW5quzAoDFq9IR5e5STj2VPkVzuK1BbykzM.', 'ADMIN', NULL, NULL, NULL, true );

INSERT INTO users (name, email, phonenumber, birthdate, password, role, company_id)
VALUES ('Abem', 'abem@gmail.com', '0909112233', '2000-10-10', '$2a$10$Nq58i28UKrS3d5.T9U2jZuWrxHmXqvbxuGj9/qR82rJXPVKJuTcyW', 'ADMIN', NULL);
ALTER TABLE jobapplication ALTER COLUMN coverletter TYPE TEXT;
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'jobapplication' AND column_name = 'coverletter';
UPDATE jobapplication
SET status = 'PENDING'
WHERE id = 1;
SELECT * from jobapplication
