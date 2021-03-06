package library.ui.login;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import library.business.AuthenticationController;
import library.dataaccess.Auth;
import library.exceptions.LoginException;
import library.ui.settings.Preferences;
import library.util.LibraryAssistantUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController extends AuthenticationController implements Initializable {

    private final static Logger LOGGER = LogManager.getLogger(LoginController.class.getName());
    Preferences preference;
    @FXML
    private JFXTextField username;
    @FXML
    private JFXPasswordField password;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        preference = Preferences.getPreferences();
    }

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String uname = StringUtils.trimToEmpty(username.getText());
        String pword = password.getText();

        try {
            login(uname, pword);

            closeStage();
            loadMain(currentAuth);
            LOGGER.log(Level.INFO, "User successfully logged in {}", uname);
            System.out.println(currentAuth.name());
        } catch (LoginException | IOException e) {
            username.getStyleClass().add("wrong-credentials");
            password.getStyleClass().add("wrong-credentials");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        System.exit(0);
    }

    private void closeStage() {
        ((Stage) username.getScene().getWindow()).close();
    }

    void loadMain(Auth auth) throws IOException {
        Parent parent;

        if (auth == Auth.ADMIN) {
            parent = FXMLLoader.load(getClass().getResource("/library/ui/main/admin/admin.fxml"));
        } else {
            parent = FXMLLoader.load(getClass().getResource("/library/ui/main/librarian/librarian.fxml"));
        }

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("The Force");
        stage.setScene(new Scene(parent));
        stage.show();
        LibraryAssistantUtil.setStageIcon(stage);

        parent.requestFocus();
    }

}
