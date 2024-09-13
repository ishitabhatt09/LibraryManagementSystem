import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class LibraryManagementSystem {

    // Database connection configuration
    private static final String URL = "jdbc:mysql://localhost:3306/library_management";
    private static final String USER = "root";  // Your MySQL username
    private static final String PASSWORD = "";  // Your MySQL password

    public static void main(String[] args) {
        Connection connection = getConnection();

        // Example use case:
        // 1. Login as admin or user
        if (login("admin", "admin123", connection)) {
            System.out.println("Login successful!");

            // 2. Add a book (admin operation)
            addBook("The Great Gatsby", "F. Scott Fitzgerald", connection);

            // 3. Check book availability
            boolean isAvailable = isBookAvailable(1, connection);
            System.out.println("Is book available: " + isAvailable);
        } else {
            System.out.println("Login failed. Please check your credentials.");
        }
    }

    // Establish a connection to the database
    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }
        return connection;
    }

    // User login functionality
    public static boolean login(String username, String password, Connection connection) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String role = resultSet.getString("role");
                System.out.println("Logged in as: " + role);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Add a new book (Admin operation)
    public static void addBook(String title, String author, Connection connection) {
        String query = "INSERT INTO books (title, author) VALUES (?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("A new book was added: " + title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Check if a book is available
    public static boolean isBookAvailable(int bookId, Connection connection) {
        String query = "SELECT available FROM books WHERE id = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, bookId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("available");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Issue a book (User operation)
    public static void issueBook(int userId, int bookId, Connection connection) {
        if (isBookAvailable(bookId, connection)) {
            LocalDate issueDate = LocalDate.now();
            LocalDate returnDate = issueDate.plusDays(15);

            String query = "INSERT INTO transactions (user_id, book_id, issue_date, return_date) VALUES (?, ?, ?, ?)";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, userId);
                preparedStatement.setInt(2, bookId);
                preparedStatement.setDate(3, Date.valueOf(issueDate));
                preparedStatement.setDate(4, Date.valueOf(returnDate));

                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Book issued successfully! Return date: " + returnDate);
                    updateBookAvailability(bookId, false, connection);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Book is not available for issuing.");
        }
    }

    // Return a book (User operation)
    public static void returnBook(int userId, int bookId, Connection connection) {
        String query = "UPDATE transactions SET return_date = ? WHERE user_id = ? AND book_id = ?";
        try {
            LocalDate returnDate = LocalDate.now();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDate(1, Date.valueOf(returnDate));
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3, bookId);

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Book returned successfully!");
                updateBookAvailability(bookId, true, connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update book availability
    public static void updateBookAvailability(int bookId, boolean availability, Connection connection) {
        String query = "UPDATE books SET available = ? WHERE id = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBoolean(1, availability);
            preparedStatement.setInt(2, bookId);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Book availability updated to: " + availability);
            }
        } catch (SQLException e) {
            e.printStackTrace();
 }
}
}