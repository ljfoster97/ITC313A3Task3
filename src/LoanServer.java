import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Server program that calculates loan repayments from
 * client data and sends the results back to the client.
 */
public class LoanServer extends Application {
    // Required fields.
    private static int clientNumber;
    // Port number as field instead of being hardcoded so that the option
    // to specify ports can be added later.
    private final int portNumber = 9999;
    ServerSocket serverSocket;
    private TextArea textArea;
    private Loan loan;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Create a new TextArea since we will be printing large amounts of text.
        textArea = new TextArea();
        textArea.setEditable(false);

        // Create a new ScrollPane and add the TextArea to it
        // so that past calculations can be viewed.
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(textArea);

        // Disable horizontal scrolling on the TextArea.
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Automatically scroll to the bottom whenever the TextArea is updated.
        textArea.textProperty().addListener((observableValue, s, t1) -> textArea.setScrollTop(Double.MAX_VALUE));

        // Create a new Scene and add the ScrollPane that contains the TextArea.
        Scene scene = new Scene(scrollPane, 480, 184);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Append timestamp when the program starts.
        textArea.appendText("Loan server started at " + new Date() + '\n');

        // Start the SocketServerThread to listen for clients.
        Thread socketServerThread = new Thread(new SocketServerThread());
        // Terminate this thread when the program exits.
        socketServerThread.setDaemon(true);
        socketServerThread.start();
    }

    /**
     * Thread for receiving data, performing calculations and sending back to the client.
     */
    private class LoanServerThread extends Thread {
        Socket socket;
        int count;

        // Constructor.
        public LoanServerThread(Socket s, int c) {
            super("LoanServerThread");
            this.socket = s;
            this.count = c;
            // Print client info with timestamp for each thread/client instance.
            printClientInfo();
        }

        @Override
        public void run() {
            // Create a new Loan object.
            loan = new Loan();

            // Try/Catch block to get the IO streams from the client socket.
            try (DataInputStream inputStream =
                         new DataInputStream(socket.getInputStream());
                 DataOutputStream outputStream =
                         new DataOutputStream(socket.getOutputStream())) {
                // Read the data input from the client.
                loan.setLoanAmount(inputStream.readDouble());
                loan.setLoanTerm(inputStream.readInt());
                loan.setInterestRate(inputStream.readDouble());

                // Create new formatter object for the numerical data.
                DecimalFormat df = new DecimalFormat("0.00");

                // Display the raw data that was received.
                // This isn't necessary but is useful for testing.
                textArea.appendText("Loan amount: $"
                        + df.format(loan.getLoanAmount()) + '\n');
                textArea.appendText("Number of years: "
                        + loan.getLoanTerm() + '\n');
                textArea.appendText("Annual interest rate: "
                        + df.format(loan.getInterestRate())
                        + "%\n");

                // Calculate the monthly and total repayments.
                double monthlyPayment = loan.getMonthlyPayment();
                double totalPayment = loan.getTotalPayment();

                // Send the results back to the client.
                outputStream.writeDouble(monthlyPayment);
                outputStream.writeDouble(totalPayment);

                // Display the results on the server as well.
                // For testing only.
                textArea.appendText("Monthly payment: $"
                        + df.format(monthlyPayment) + '\n');
                textArea.appendText("Total payment: $"
                        + df.format(totalPayment) + "\n\n");

                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Method to display client details.
         */
        private void printClientInfo() {
            textArea.appendText("\nStarting thread for Client " + ++ clientNumber
                    + " at " + new Date() + '\n');
            textArea.appendText("Client " + clientNumber + "'s host name is "
                    + socket.getInetAddress().getHostName()
                    + '\n');
            textArea.appendText("Client " + clientNumber + "'s IP address is "
                    + socket.getRemoteSocketAddress().toString()
                    + '\n');
        }
    }

    /**
     * This thread listens on the specified port
     * and then starts a new thread for each individual clients loan calculations.
     */
    private class SocketServerThread extends Thread {
        // Keep track of clients/number of calculations.
        int count = 0;

        @Override
        public void run() {
            try {
                // Declare socket;
                Socket socket;
                // Initialize new ServerSocket with the specified port number.
                serverSocket = new ServerSocket(portNumber);
                Platform.runLater(() -> textArea.appendText("Listening on port #"
                        + serverSocket.getLocalPort()));
                // There is likely a better way to do this,
                // but in this scenario we want to accept all clients.
                while (true) {
                    socket = serverSocket.accept();
                    // Increment counter.
                    count++;
                    // Start a new LoanServerThread.
                    Thread clientThread = new Thread(
                            new LoanServerThread(socket, count));
                    // Terminate this thread when the program exits.
                    clientThread.setDaemon(true);
                    clientThread.start();
                }
            } catch(IOException e) {
                e.printStackTrace();
                //ALERT
            }
        }
    }
}
