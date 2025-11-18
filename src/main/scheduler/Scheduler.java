package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) throws SQLException {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        try {
            currentPatient = new Patient.PatientBuilder(username, salt, hash).build();
            currentPatient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            currentCaregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            currentCaregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }

        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        if (tokens.length != 2) {
            System.out.println("Error: Please try again!");
            return;
        }

        String date = tokens[1];
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String usernames = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY username";
        String doses = "SELECT Name, Doses FROM Vaccines";
        try {
            Date d = Date.valueOf(date);
            PreparedStatement caregiverNames = con.prepareStatement(usernames);
            PreparedStatement selectedDoses = con.prepareStatement(doses);

            caregiverNames.setDate(1, d);
            ResultSet caregiverSet = caregiverNames.executeQuery();
            ResultSet dosesSet = selectedDoses.executeQuery();

            if (!caregiverSet.next()) {
                System.out.println("Error: No Caregivers available for this day");
                return;
            }
            System.out.print(caregiverSet.getString(1));

            while (caregiverSet.next()) {
                System.out.print(caregiverSet.getString(1) + " ");
                while (dosesSet.next()) {
                    System.out.println(dosesSet.getString(1) + ":" + dosesSet.getInt(2));
                }
            }
        } catch (SQLException e) {
            System.out.println("Please try again!");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }

    private static void reserve(String[] tokens) throws SQLException {
        if (currentCaregiver != null) {
            System.out.println("Please login as a patient!");
            return;
        }
        if (currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        if (tokens.length != 3) {
            System.out.println("Error: Please try again!");
            return;
        }

        String date = tokens[1];
        String vaccine = tokens[2];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine currentVaccine = null;
        try {
            currentVaccine = new Vaccine.VaccineGetter(vaccine).get();
            doses = currentVaccine.getAvailableDoses();
        } catch (SQLException e) {
            System.out.println("Error: Please try again");
            e.printStackTrace();
        }
        if (doses == 0) {
            System.out.println("Not enough available doses");
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String caregiverNames = null;
        String username = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY username";
        int ID =  date.hashCode() + currentPatient.hashCode();
        try {
            PreparedStatement statementCaregiver = con.prepareStatement(username);
            statementCaregiver.setString(1, date);
            ResultSet resultSetCaregiver = statementCaregiver.executeQuery();
            if (!resultSetCaregiver.next()){
                System.out.println("There is no caregiver");
                return;
            }
            caregiverNames = resultSetCaregiver.getString(1);
            System.out.println("Appointment ID: " + ID + ", Caregiver Name: " + caregiverNames);
        } catch (SQLException e) {
            System.out.println("Error appears when assign careGiver");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }

        updateTable(ID, currentVaccine, vaccine, date, caregiverNames);
    }

    public static void updateTable(int ID, Vaccine currentVaccine, String vaccine, String date, String caregiverNames) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String patient = currentPatient.getUsername();
        String appointment = "INSERT INTO Appointments VALUES (?, ?, ?, ?, ?)";
        String availability = "DELETE FROM Availabilities WHERE Time = ? AND Username = ?";

        PreparedStatement updateAppoint = con.prepareStatement(appointment);
        PreparedStatement updateAvail = con.prepareStatement(availability);
        try {
            updateAppoint.setInt(1, ID);
            updateAppoint.setString(2, vaccine);
            updateAppoint.setString(3, date);
            updateAppoint.setString(4, patient);
            updateAppoint.setString(5, caregiverNames);
            updateAppoint.executeUpdate();

            updateAvail.setString(1, date);
            updateAvail.setString(2, caregiverNames);
            updateAvail.executeUpdate();

            // update the number of vaccine
            currentVaccine.decreaseAvailableDoses(1);
        } catch (SQLException e) {
            System.out.println("Error: Please try again!");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
    }

    private static void cancel(String[] tokens) {
        if (currentCaregiver == null || currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        int ID = Integer.parseInt(tokens[1]);
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String IDSQL = "SELECT Username_Caregivers, Time, Username_Vaccine FROM Appointments WHERE ID = ?";
        String availSQL = "INSERT INTO Availabilities VALUES (?, ?)";
        String deleteSQL = "DELETE FROM Appointments WHERE app_id = ?";
        try {
            PreparedStatement appointment = con.prepareStatement(IDSQL);
            appointment.setInt(1, ID);
            ResultSet result = appointment.executeQuery();

            String caregiver = null;
            Date date = null;
            String currentVaccine = null;
            if (result.next()) {
                caregiver = result.getString(1);
                date = result.getDate(2);
                currentVaccine = result.getString(3);
            }

            Vaccine vaccine = new Vaccine.VaccineGetter(currentVaccine).get();
            vaccine.increaseAvailableDoses(1);

            PreparedStatement add = con.prepareStatement(availSQL);
            add.setDate(1, date);
            add.setString(2, caregiver);
            add.executeUpdate();

            PreparedStatement delete = con.prepareStatement(deleteSQL);
            delete.setInt(1, ID);
            delete.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Please try again!");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        System.out.println("successfully cancel the Appointment " + ID);
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) throws SQLException {
        if (tokens.length != 1) {
            System.out.println("Please try again!");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String caregiverSQL = "SELECT ID, Username_Vaccines, Time, Username_Patients FROM Appointments WHERE Username_Caregivers = ? ORDER BY ID";
        String patientSQL = "SELECT ID, Username_Vaccines, Time, Username_Caregivers FROM Appointments Username_Patients = ? ORDER BY ID";

        PreparedStatement caregiver = con.prepareStatement(caregiverSQL);
        PreparedStatement patient = con.prepareStatement(patientSQL);
        caregiver.setString(1, currentCaregiver.getUsername());
        ResultSet theQuery = caregiver.executeQuery();
        patient.setString(1, currentPatient.getUsername());
        if (currentCaregiver != null) {
            try {
                while (theQuery.next()) {
                    System.out.print("Appointment ID: " + theQuery.getInt(1) + " ");
                    System.out.print("Vaccine: " + theQuery.getString(2) + " ");
                    System.out.print("Date: " + theQuery.getString(3) + " ");
                    System.out.print("Patient: " + theQuery.getString(4) + " ");
                    System.out.println();
                }
            } catch (SQLException e) {
                System.out.println("Please try again!");
                e.printStackTrace();
            }
        } else if(currentPatient != null) {
            try {
                theQuery = patient.executeQuery();
                while (theQuery.next()) {
                    System.out.print("Appointment ID: " + theQuery.getInt(1) + " ");
                    System.out.print("Vaccine: " + theQuery.getString(2) + " ");
                    System.out.print("Date: " + theQuery.getString(3) + " ");
                    System.out.print("Caregiver:: " + theQuery.getString(4) + " ");
                    System.out.println();
                }
            } catch (SQLException e) {
                System.out.println("Please try again!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Please login first!");
        }
    }

    private static void logout(String[] tokens) {
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        if (tokens.length != 1) {
            System.out.println("Please try again!");
        }
        currentPatient = null;
        currentCaregiver = null;
        System.out.println("Successfully logged out!");
    }
}