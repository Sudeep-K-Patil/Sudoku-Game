
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class Sudoku extends JFrame {

    // GUI Components
    private JTextField[][] cells;
    private JPanel gamePanel;
    private JPanel buttonPanel;

    // Game Data
    private int[][] solution;
    private int[][] puzzle;
    private String currentDifficulty;
    private String currentUser;

    // Constants
    private static final int GRID_SIZE = 9;
    private static final int BOX_SIZE = 3;
    private static final String SCORES_FILE = "user_scores.txt";
    private static final Map<String, Integer> DIFFICULTY_LEVELS = Map.of(
            "Easy", 30,
            "Medium", 45,
            "Hard", 55
    );

    public Sudoku(String username) {
        currentUser = username;
        cells = new JTextField[GRID_SIZE][GRID_SIZE];
        solution = new int[GRID_SIZE][GRID_SIZE];
        puzzle = new int[GRID_SIZE][GRID_SIZE];

        // Ask user to select difficulty
        String difficulty = chooseDifficulty();
        if (difficulty != null) {
            setupGame(difficulty);
        } else {
            System.exit(0);
        }
    }

    // Initialize the game with selected difficulty
    private void setupGame(String difficulty) {
        currentDifficulty = difficulty;
        setTitle("Sudoku - " + currentDifficulty + " - Player: " + currentUser);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        createGameBoard();
        createControlButtons();
        generateNewPuzzle();

        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    // Create the 9x9 Sudoku grid
    private void createGameBoard() {
        gamePanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 1, 1));
        gamePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col] = createCell(row, col);
                gamePanel.add(cells[row][col]);
            }
        }
    }

    // Create a single cell in the grid
    private JTextField createCell(int row, int col) {
        JTextField cell = new JTextField();
        cell.setHorizontalAlignment(JTextField.CENTER);
        cell.setFont(new Font("Arial", Font.BOLD, 20));

        // Add borders to separate 3x3 boxes
        if ((row % BOX_SIZE == 0 && row != 0) || (col % BOX_SIZE == 0 && col != 0)) {
            cell.setBorder(BorderFactory.createMatteBorder(
                    row % BOX_SIZE == 0 ? 3 : 1,
                    col % BOX_SIZE == 0 ? 3 : 1,
                    1, 1, Color.BLACK));
        }

        // Only allow single digits
        cell.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) || c == '0' || cell.getText().length() >= 1) {
                    evt.consume();
                }
            }
        });

        return cell;
    }

    // Create control buttons
    private void createControlButtons() {
        buttonPanel = new JPanel();
        addButton("Check Solution", e -> checkSolution());
        addButton("New Game", e -> generateNewPuzzle());
        addButton("Show Stats", e -> displayStats());
    }

    private void addButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        buttonPanel.add(button);
    }

    // Generate a new Sudoku puzzle
    private void generateNewPuzzle() {
        // Create a solved puzzle
        solution = generateSolvedGrid(new int[GRID_SIZE][GRID_SIZE], 0);

        // Copy solution and remove cells based on difficulty
        copyGrid(solution, puzzle);
        removeNumbers(DIFFICULTY_LEVELS.get(currentDifficulty));

        // Display the puzzle
        displayPuzzle();
    }

    // Generate a complete valid Sudoku solution
    private int[][] generateSolvedGrid(int[][] grid, int index) {
        if (index == GRID_SIZE * GRID_SIZE) {
            return grid;
        }

        int row = index / GRID_SIZE;
        int col = index % GRID_SIZE;

        if (grid[row][col] != 0) {
            return generateSolvedGrid(grid, index + 1);
        }

        // Try random numbers 1-9
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= GRID_SIZE; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        for (int num : numbers) {
            if (isValidPlacement(grid, row, col, num)) {
                grid[row][col] = num;
                int[][] result = generateSolvedGrid(grid, index + 1);
                if (result != null) {
                    return result;
                }
                grid[row][col] = 0;
            }
        }
        return null;
    }

    // Check if a number can be placed in a cell
    private boolean isValidPlacement(int[][] grid, int row, int col, int num) {
        // Check row and column
        for (int x = 0; x < GRID_SIZE; x++) {
            if (grid[row][x] == num || grid[x][col] == num) {
                return false;
            }
        }

        // Check 3x3 box
        int boxRow = row - row % BOX_SIZE;
        int boxCol = col - col % BOX_SIZE;
        for (int i = 0; i < BOX_SIZE; i++) {
            for (int j = 0; j < BOX_SIZE; j++) {
                if (grid[boxRow + i][boxCol + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    // Remove numbers from the puzzle based on difficulty
    private void removeNumbers(int count) {
        Random random = new Random();
        while (count > 0) {
            int row = random.nextInt(GRID_SIZE);
            int col = random.nextInt(GRID_SIZE);
            if (puzzle[row][col] != 0) {
                puzzle[row][col] = 0;
                count--;
            }
        }
    }

    // Display the current puzzle state
    private void displayPuzzle() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JTextField cell = cells[row][col];
                int value = puzzle[row][col];
                cell.setText(value == 0 ? "" : String.valueOf(value));
                cell.setEditable(value == 0);
                cell.setBackground(value == 0 ? Color.WHITE : Color.LIGHT_GRAY);
            }
        }
    }

    // Check if the current solution is correct
    private void checkSolution() {
        boolean isComplete = true;
        boolean isCorrect = true;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                String value = cells[row][col].getText();
                if (value.isEmpty()) {
                    isComplete = false;
                    break;
                }
                if (Integer.parseInt(value) != solution[row][col]) {
                    isCorrect = false;
                }
            }
            if (!isComplete) {
                break;
            }
        }

        if (!isComplete) {
            showMessage("Puzzle is not complete!");
        } else if (!isCorrect) {
            showMessage("Solution is incorrect!");
        } else {
            showMessage("Congratulations! You solved the puzzle!");
            saveScore();
            generateNewPuzzle();
        }
    }

    // Helper methods
    private String chooseDifficulty() {
        return (String) JOptionPane.showInputDialog(
                null,
                "Choose difficulty level:",
                "Difficulty Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                DIFFICULTY_LEVELS.keySet().toArray(),
                "Easy"
        );
    }

    private void copyGrid(int[][] from, int[][] to) {
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(from[i], 0, to[i], 0, GRID_SIZE);
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void saveScore() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORES_FILE, true))) {
            writer.println(currentUser + ":" + DIFFICULTY_LEVELS.get(currentDifficulty));
        } catch (IOException e) {
            showMessage("Error saving score");
        }
    }

    private void displayStats() {
        Map<String, Integer> scores = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String user = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    scores.merge(user, score, Integer::sum);
                }
            }
        } catch (IOException e) {
            showMessage("Error reading scores");
            return;
        }

        StringBuilder stats = new StringBuilder("Scores:\n");
        scores.forEach((user, score)
                -> stats.append(user).append(": ").append(score).append("\n"));
        showMessage(stats.toString());
    }
}
