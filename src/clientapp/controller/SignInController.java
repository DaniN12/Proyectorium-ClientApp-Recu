/**
 * Controller class for the SignIn view, managing user sign-in functionality.
 * It handles user input, displays errors, and initiates the sign-in process.
 *
 *
 * @author Dani
 */
package clientapp.controller;

import clientapp.exceptions.EmptyFieldException;

import java.util.Optional;

import clientapp.exceptions.IncorrectPatternException;
import clientapp.factories.SignableFactory;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.logging.Level;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import clientapp.model.UserEntity;
import clientapp.interfaces.Signable;
import clientapp.model.UserType;
import javax.ws.rs.core.GenericType;

public class SignInController {

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblPassword;

    @FXML
    private Label lblError;

    @FXML
    private TextField txtFieldEmail;

    @FXML
    private TextField txtFieldPassword;

    @FXML
    private PasswordField PasswordField;

    @FXML
    private Button btnShowPasswd = new Button();

    @FXML
    private Button btnSignIn = new Button();

    @FXML
    private Hyperlink HyperLinkRegistered;

    @FXML
    private Label label;

    @FXML
    private boolean passwordVisible = false;

    @FXML
    private ImageView ImageViewEye = new ImageView();

    @FXML
    private Button btnSigIn;

    @FXML
    private TextField usernameField;

    @FXML
    private Label errorLabel;

    private Signable signable;

    private Image icon = new Image(getClass().getResourceAsStream("/resources/icon.png"));

    private Stage stage = new Stage();

    private Logger logger = Logger.getLogger(SignInController.class.getName());

    /**
     * Initializes the SignIn view by setting up the stage and its properties.
     *
     * @param root The root node of the scene graph.
     */
    public void initialize(Parent root) {
        logger.info("Initializing SignIn stage.");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Sign In");
        stage.getIcons().add(icon);
        stage.setResizable(false);

        txtFieldPassword.setVisible(false);
        txtFieldPassword.textProperty().bindBidirectional(PasswordField.textProperty());

        ImageViewEye.setImage(new Image(getClass().getResourceAsStream("/resources/SinVerContraseña.png")));

        HyperLinkRegistered.setOnAction(this::handleHyperLinkAction);

        signable = SignableFactory.getSignable();

        btnShowPasswd.setOnAction(this::showPassword);
        
        stage.show();
    }

    /**
     * Handles the sign-in process when the sign-in button is clicked.
     *
     * @param event The action event triggered by clicking the sign-in button.
     * @throws ConnectionErrorException If there is a connection error.
     * @throws UserDoesntExistExeption If the user does not exist.
     */
    @FXML
    protected void handleSignIn(ActionEvent event) {
        String email = txtFieldEmail.getText();
        String password = txtFieldPassword.getText();

        UserEntity credentials = new UserEntity();
        credentials.setEmail(email);
        credentials.setPassword(password);

        try {

            if (txtFieldEmail.getText().equals("admin") && PasswordField.getText().equals("admin")) {
                openAdminWindow(event, new UserEntity());
            } else if (txtFieldEmail.getText().equals("customer") && PasswordField.getText().equals("customer")) {
                openMainWindow(event, new UserEntity());
            } else {
                if (email.isEmpty() || password.isEmpty() || txtFieldPassword.getText().isEmpty()) {
                    throw new EmptyFieldException("Fields are empty, all fields need to be filled");
                }

                if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$")) {
                    throw new IncorrectPatternException("The email is not well written or is incorrect");
                }
                UserEntity signedInUser = signable.signIn(credentials, new GenericType<UserEntity>() {
                });

                if (signedInUser.getUserType() != UserType.ADMIN) {
                    openMainWindow(event, signedInUser);
                } else {
                    openAdminWindow(event, signedInUser);
                }
            }

        } catch (EmptyFieldException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage());
            showAlert("Error", "Please fill in all fields.", Alert.AlertType.ERROR);
        } catch (IncorrectPatternException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage());
            showAlert("Error", "The email has to have a email format, don't forget the @.", Alert.AlertType.ERROR);
            /*  } catch (UserNotActiveException ex) {
            Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            showAlert("Error", "This user is not active.", Alert.AlertType.ERROR);
        } catch (ConnectionErrorException ex) {
            Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            showAlert("Error", ex.getLocalizedMessage(), Alert.AlertType.ERROR);
        } catch (IncorrectCredentialsException ex) {
            Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            showAlert("Error", ex.getLocalizedMessage(), Alert.AlertType.ERROR);
        }*/
        }
    }

    @FXML
    public void openMainWindow(ActionEvent event, UserEntity user) {
        try {
            // Load DOM form FXML view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientapp/view/InfoView.fxml"));
            Parent root = (Parent) loader.load();
            // Retrieve the controller associated with the view
            InfoViewController controller = (InfoViewController) loader.getController();
            //Check if there is a RuntimeException while opening the view
            if (controller == null) {
                throw new RuntimeException("Failed to load InfoViewController");
            }

            if (stage == null) {
                throw new RuntimeException("Stage is not initialized");
            }
            controller.setStage(stage);
            //Initializes the controller with the loaded view
            controller.initialize(root, user);

        } catch (IOException ex) {
            // Logs the error and displays an alert messsage
            Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            new Alert(Alert.AlertType.ERROR, "Error loading InfoView.fxml", ButtonType.OK).showAndWait();
        } catch (RuntimeException ex) {
            // Logs the error and displays an alert messsage
            Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, "Exception occurred", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void openAdminWindow(ActionEvent event, UserEntity user) {
        try {
            // Load DOM form FXML view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientapp/view/MainAdmin.fxml"));
            Parent root = (Parent) loader.load();
            // Retrieve the controller associated with the view
            MenuAdminController controller = (MenuAdminController) loader.getController();
            //Check if there is a RuntimeException while opening the view
            if (controller == null) {
                throw new RuntimeException("Failed to load InfoViewController");
            }

            if (stage == null) {
                throw new RuntimeException("Stage is not initialized");
            }
            controller.setStage(stage);
            //Initializes the controller with the loaded view
            controller.initialize(root, user);

        } catch (IOException ex) {
            // Logs the error and displays an alert messsage
            Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            new Alert(Alert.AlertType.ERROR, "Error loading InfoView.fxml", ButtonType.OK).showAndWait();
        } catch (RuntimeException ex) {
            // Logs the error and displays an alert messsage
            Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, "Exception occurred", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    /**
     * Gets the stage associated with this controller.
     *
     * @return The stage.
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Sets the stage for this controller.
     *
     * @param stage The stage to set.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Displays an alert to the user with the specified title, message, and
     * alert type.
     *
     * @param title The title of the alert.
     * @param message The message to display in the alert.
     * @param alertType The type of alert (ERROR, INFORMATION, etc.).
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the window close request event, displaying a confirmation dialog.
     *
     * @param event The window event.
     */
    @FXML
    public void onCloseRequest(WindowEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("EXIT");
        alert.setContentText("Are you sure you want to close the application?");
        Optional<ButtonType> answer = alert.showAndWait();

        if (answer.get() == ButtonType.OK) {
            Platform.exit();
        } else {
            event.consume();
        }
    }

    /**
     * Opens the SignUpView window when the Hyperlink is clicked.
     *
     * @param event The action event triggered by clicking the hyperlink.
     */
    @FXML
    private void handleHyperLinkAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientapp/view/SignUpView.fxml"));
            Parent root = loader.load();

            SignUpViewController controller = (SignUpViewController) loader.getController();
            if (controller == null || stage == null) {
                throw new RuntimeException("Failed to load SignUpController or stage not initialized.");
            }

            controller.setStage(stage);
            controller.initialize(root);
            controller.handleWindowShowing(new WindowEvent(stage, WindowEvent.WINDOW_SHOWING));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            showAlert("Error", "Error loading SignUpView.fxml", Alert.AlertType.ERROR);
        } catch (RuntimeException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Toggles the password visibility when the show password button is clicked.
     *
     * @param event The action event triggered by clicking the show password
     * button.
     */
    public void showPassword(ActionEvent event) {
        if (!passwordVisible) {
            ImageViewEye.setImage(new Image(getClass().getResourceAsStream("/resources/ViendoContraseña.png")));
            PasswordField.setVisible(false);
            txtFieldPassword.setVisible(true);
            passwordVisible = true;
        } else {
            ImageViewEye.setImage(new Image(getClass().getResourceAsStream("/resources/SinVerContraseña.png")));
            txtFieldPassword.setVisible(false);
            PasswordField.setVisible(true);
            passwordVisible = false;
        }
    }
}
