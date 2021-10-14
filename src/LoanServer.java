import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Logger;

public class LoanServer extends Application {
    private TextArea textArea;
    private final int portNumber = 9999;
    private Loan loan;
    private static int clientNumber;

    ServerSocket serverSocket;

//    @Override
//    public void start(Stage stage) throws Exception {
//
//        textArea = new TextArea();
//        textArea.setEditable(false);
//        ScrollPane scrollPane = new ScrollPane();
//        scrollPane.setContent(textArea);
//        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//
//
//
//        Scene scene = new Scene(scrollPane, 500, 275);
//        stage.setScene(scene);
//
//        stage.show();
//    }



    @Override
    public void start(Stage stage) throws Exception {
        boolean listening = true;

        textArea = new TextArea();
        textArea.setEditable(false);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(textArea);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Automatically scroll to the bottom when text is updated.
        textArea.textProperty().addListener((observableValue, s, t1) -> textArea.setScrollTop(Double.MAX_VALUE));


        Scene scene = new Scene(scrollPane, 480, 184);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();


        textArea.appendText("Loan server started at " + new Date() + '\n');
        System.out.println("foo");

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.setDaemon(true);
        socketServerThread.start();
    }

    private class LoanServerThread extends Thread {
        Socket socket = null;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        int count;


        public LoanServerThread(Socket s, int c) {
            super("LoanServerThread");
            this.socket = s;
            this.count = c;
            printClientInfo();
        }

        @Override
        public void run() {
            loan = new Loan();

            try (DataInputStream inputStream =
                         new DataInputStream(socket.getInputStream());
                 DataOutputStream outputStream =
                         new DataOutputStream(socket.getOutputStream())) {
                    //read input from client
                    loan.setLoanAmount(inputStream.readDouble());
                    loan.setLoanTerm(inputStream.readInt());
                    loan.setInterestRate(inputStream.readDouble());

                    DecimalFormat df = new DecimalFormat("0.00");
                    textArea.appendText("Loan amount: $"
                            + df.format(loan.getLoanAmount()) + '\n');
                    textArea.appendText("Number of years: "
                            + loan.getLoanTerm() + '\n');
                    textArea.appendText("Annual interest rate: "
                            + df.format(loan.getInterestRate())
                            + "%\n");

                    //compute payments
                    double monthlyPayment = loan.getMonthlyPayment();
                    double totalPayment = loan.getTotalPayment();

                    //send results back to client
                    outputStream.writeDouble(monthlyPayment);
                    outputStream.writeDouble(totalPayment);

                    textArea.appendText("Monthly payment: $"
                            + df.format(monthlyPayment) + '\n');
                    textArea.appendText("Total payment: $"
                            + df.format(totalPayment) + "\n\n");

                    socket.close();
            }
            catch (IOException ioe) {
                System.err.println(ioe);
            }
        }

        private void printClientInfo() {
            textArea.appendText("Starting thread for Client " + ++clientNumber
                    + " at " + new Date() + '\n');
            textArea.appendText("Client " + clientNumber + "'s host name is "
                    + socket.getInetAddress().getHostName()
                    + '\n');
            textArea.appendText("Client " + clientNumber + "'s IP address is "
                    + socket.getRemoteSocketAddress().toString()
                    + '\n');
        }

    }

    private class SocketServerThread extends Thread {
        int count = 0;

        @Override
        public void run() {
            try {
                Socket socket = null;

                serverSocket = new ServerSocket(portNumber);
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        textArea.appendText("Listening on: "
                                + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    socket = serverSocket.accept();
                    count++;

                    //Start another thread
                    //to prevent blocked by empty dataInputStream
                    Thread acceptedThread = new Thread(
                            new LoanServerThread(socket, count));
                    acceptedThread.setDaemon(true); //terminate the thread when program end
                    acceptedThread.start();

                }
            } catch (IOException e) {
                e.printStackTrace();
                //ALERT
            }

        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
