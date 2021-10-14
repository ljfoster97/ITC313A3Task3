import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.IIOException;
import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DecimalFormat;


public class LoanClient extends Application {
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private double loanAmount, interestRate, monthlyPayment, totalPayment;
    private int loanTerm;

    private TextArea textArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        Label labelRate = new Label("Annual Interest Rate");
        TextField textFieldRate = new TextField();

        Label labelTerm = new Label("Term");
        TextField textFieldTerm = new TextField();


        Label labelLoan = new Label("Loan Amount");
        TextField textFieldLoan = new TextField();

        Button btSubmit = new Button("Submit");

        btSubmit.setOnAction(actionEvent -> {
            if (!textFieldLoan.getText().isEmpty()
                    && !textFieldRate.getText().isEmpty()
                    && !textFieldTerm.getText().isEmpty()) {
                try {
                    loanAmount = Double.parseDouble(textFieldLoan.getText().trim());
                    interestRate = Double.parseDouble(textFieldRate.getText().trim());
                    loanTerm = Integer.parseInt(textFieldTerm.getText().trim());
                    dataTransmission();
                }
                catch(NumberFormatException e) {
                    System.err.println(e);
                    alert("Invalid Input.",
                            "Fields can only contain numerical values.",
                            Alert.AlertType.WARNING);
                }
            }
            else if (textFieldLoan.getText().isEmpty()
                    && textFieldRate.getText().isEmpty()
                    && textFieldTerm.getText().isEmpty()) {
                alert("Invalid Input.",
                        "Fields cannot be empty.",
                        Alert.AlertType.WARNING);

            }
        });


        textArea = new TextArea();

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25,25,25,25));

        gridPane.add(labelRate,0,0);
        gridPane.add(textFieldRate,1,0);
        gridPane.add(labelTerm,0,1);
        gridPane.add(textFieldTerm,1,1);
        gridPane.add(labelLoan,0,2);
        gridPane.add(textFieldLoan,1,2);
        gridPane.add(btSubmit,1,3);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(gridPane,textArea);

        Scene scene = new Scene(vBox, 500, 275);
        stage.setScene(scene);

        stage.show();
    }

    private void dataTransmission() {
        int portNum = 9999;
        try (Socket clientSocket = new Socket("localhost", portNum)) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            textArea.appendText("Loan amount: $" + decimalFormat.format(loanAmount) + '\n');
            textArea.appendText("Number of years: " + loanTerm + '\n');
            textArea.appendText("Annual interest rate: " + decimalFormat.format(interestRate)
                    + "%\n");

            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            outputStream.writeDouble(loanAmount);
            outputStream.writeInt(loanTerm);
            outputStream.writeDouble(interestRate);
            outputStream.flush();

            inputStream = new DataInputStream(clientSocket.getInputStream());
            monthlyPayment = inputStream.readDouble();
            totalPayment = inputStream.readDouble();

            textArea.appendText("Monthly payment: $" + decimalFormat.format(monthlyPayment) + '\n');
            textArea.appendText("Total payment: $" + decimalFormat.format(totalPayment) + "\n\n");
        }
        catch(IOException e) {
            alert("Connection Error.",
                    "A fatal error occurred while connecting to the server.",
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Simple function to create DialogWindows.
     * @param title Title of the window.
     * @param message Message to be displayed.
     * @param alertType Alert.AlertType.<Type of alert to display>
     */
    public void alert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
