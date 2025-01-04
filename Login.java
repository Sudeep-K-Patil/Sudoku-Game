import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Hashtable;

public class Login {
    private static final String CREDENTIALS_FILE = "credentials.txt";
    private Hashtable<String, String> credentials;
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public Login() {
        credentials = new Hashtable<>();
        loadCredentials();
        createLoginWindow();
    }
    
    private void loadCredentials() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CREDENTIALS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    credentials.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }
    }

    private void saveCredentials() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CREDENTIALS_FILE))) {
            for (Object key : credentials.keySet()) {
                writer.println(key + ":" + credentials.get(key));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving credentials");
        }
    }

    private void createLoginWindow() {
        frame = new JFrame("Sudoku Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(createButton("Login", this::handleLogin));
        panel.add(createButton("Register", this::handleRegister));

        frame.add(panel);
        frame.setVisible(true);
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!isValidInput(username, password)) {
            return;
        }

        if (credentials.containsKey(username)) {
            JOptionPane.showMessageDialog(frame, "Username already exists!");
            return;
        }

        credentials.put(username, password);
        saveCredentials();
        JOptionPane.showMessageDialog(frame, "Registration successful!");
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (credentials.containsKey(username) && credentials.get(username).equals(password)) {
            frame.dispose();
            new Sudoku(username);
        } else {
            JOptionPane.showMessageDialog(frame, "Invalid username or password");
        }
    }

    private boolean isValidInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both username and password");
            return false;
        }
        if (username.length() < 3 || password.length() < 6) {
            JOptionPane.showMessageDialog(frame, "Username must be at least 3 characters and password at least 6 characters");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}
