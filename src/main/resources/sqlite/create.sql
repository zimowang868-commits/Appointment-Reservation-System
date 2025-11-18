CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Patients (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (Time, Username)
);

CREATE TABLE Appointments (
    ID int,
    Time date,
    Username_Caregivers varchar(255) REFERENCES Caregivers,
    Username_Vaccines varchar(255) REFERENCES Vaccines,
    Username_Patients varchar(255) REFERENCES Patients,
    PRIMARY KEY (ID, Time)
);