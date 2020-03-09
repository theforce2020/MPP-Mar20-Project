package library.ui.main;

import com.jfoenix.controls.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import library.alert.AlertMaker;
import library.business.AdminController;
import library.business.LibrarianController;
import library.exceptions.CheckException;
import library.model.Book;
import library.model.LibraryMember;
import library.ui.callback.BookReturnCallback;
import library.ui.issuedlist.IssuedListController;
import library.ui.main.toolbar.ToolbarController;
import library.util.LibraryAssistantUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController implements Initializable, BookReturnCallback {

    private static final String BOOK_NOT_AVAILABLE = "Not Available";
    private static final String NO_SUCH_BOOK_AVAILABLE = "No Such Book Available";
    private static final String NO_SUCH_MEMBER_AVAILABLE = "No Such Member Available";
    private static final String BOOK_AVAILABLE = "Available";
    LibrarianController librarianController = new LibrarianController();
    AdminController adminController = new AdminController();
    ObservableList<CountStat> list = FXCollections.observableArrayList();
    @FXML
    private TableColumn<String, String> fieldCol;
    @FXML
    private TableColumn<String, Long> countCol;
    private Boolean isReadyForSubmission = false;
    //    @FXML
//    private HBox book_info;
//    @FXML
//    private HBox member_info;
    @FXML
    private TextField bookIDInput;
    @FXML
    private Text bookName;
    @FXML
    private Text bookAuthor;
    @FXML
    private Text bookStatus;
    @FXML
    private TextField memberIDInput;
    @FXML
    private Text memberName;
    @FXML
    private Text memberMobile;
    @FXML
    private JFXTextField bookID;
    @FXML
    private StackPane rootPane;
    @FXML
    private JFXHamburger hamburger;
    @FXML
    private JFXDrawer drawer;
    @FXML
    private Text memberNameHolder;
    @FXML
    private Text memberEmailHolder;
    @FXML
    private Text memberContactHolder;
    @FXML
    private Text bookNameHolder;
    @FXML
    private Text bookAuthorHolder;
    @FXML
    private Text bookPublisherHolder;
    @FXML
    private Text issueDateHolder;
    @FXML
    private Text numberDaysHolder;
    @FXML
    private Text fineInfoHolder;
    @FXML
    private AnchorPane rootAnchorPane;
    @FXML
    private JFXButton renewButton;
    @FXML
    private JFXButton submissionButton;
    @FXML
    private HBox submissionDataContainer;
    @FXML
    private Tab bookIssueTab;
    @FXML
    private Tab renewTab;
    @FXML
    private JFXTabPane mainTabPane;
    @FXML
    private JFXButton btnIssue;
    @FXML
    private TableView<CountStat> tableView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (drawer != null) {
            fieldCol.setCellValueFactory(new PropertyValueFactory<>("field"));
            countCol.setCellValueFactory(new PropertyValueFactory<>("count"));

            initDrawer();
            initStats();
        } else {
            initComponents();
        }
    }

    private void initStats() {
        list.clear();

        long memCount = adminController.getAllLibraryMembers().size();
        long bookCount = adminController.getAllBooks().size();

        list.add(new CountStat("Members", memCount));
        list.add(new CountStat("Books", bookCount));

        tableView.setItems(list);
    }

    @FXML
    private void loadBookInfo(ActionEvent event) {
        clearBookCache();

        String id = bookIDInput.getText();
        Book book = adminController.getBook(id);

        if (book == null) {
            bookName.setText(NO_SUCH_BOOK_AVAILABLE);
            return;
        } else {
            memberIDInput.requestFocus();
        }

        if (book.isAvailable()) {
            bookStatus.getStyleClass().add("not-available");
        } else {
            bookStatus.getStyleClass().remove("not-available");
        }

        bookName.setText(book.getTitle());
        bookAuthor.setText(book.getAuthors().get(0).getName());
        String status = book.isAvailable() ? BOOK_AVAILABLE : "Book has been issued";

        bookStatus.setText(status);


    }

    void clearBookCache() {
        bookName.setText("");
        bookAuthor.setText("");
        bookStatus.setText("");
    }

    void clearMemberCache() {
        memberName.setText("");
        memberMobile.setText("");
    }

    @FXML
    private void loadMemberInfo(ActionEvent event) {
        clearMemberCache();

        String id = memberIDInput.getText();
        LibraryMember member = adminController.getMember(id);
        if (member != null) {
            memberName.setText(member.getName());
            memberMobile.setText(member.getTelephone());
            btnIssue.requestFocus();
        } else {
            memberName.setText(NO_SUCH_MEMBER_AVAILABLE);
        }
    }

    @FXML
    private void loadIssueOperation(ActionEvent event) {
        if (checkForIssueValidity()) {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Invalid Input", null);
            return;
        }
        if (bookStatus.getText().equals(BOOK_NOT_AVAILABLE)) {
            JFXButton btn = new JFXButton("Okay!");
            JFXButton viewDetails = new JFXButton("View Details");
            viewDetails.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
                String bookToBeLoaded = bookIDInput.getText();
                bookID.setText(bookToBeLoaded);
                bookID.fireEvent(new ActionEvent());
                mainTabPane.getSelectionModel().select(renewTab);
            });
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn, viewDetails), "Already issued book", "This book is already issued. Cant process issue request");
            return;
        }

        String memberID = memberIDInput.getText();
        String isbn = bookIDInput.getText();

        Book book = adminController.getBook(isbn);
        LibraryMember member = adminController.getMember(memberID);

        if (book == null) {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Collections.singletonList(btn), "Invalid Book ISBN", "No Book with this ISBN exists.");
            return;
        }

        if (member == null) {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Collections.singletonList(btn), "Invalid Member ID", "No Member with this ID exists.");
            return;
        }

        JFXButton yesButton = new JFXButton("YES");
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
            try {
                librarianController.checkOutBook(isbn, memberID);

                JFXButton button = new JFXButton("Done!");
                button.setOnAction((actionEvent) -> {
                    bookIDInput.requestFocus();
                });
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "Book Issue Complete", null);
            } catch (CheckException e) {
                JFXButton button = new JFXButton("Okay.I'll Check");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "Issue Operation Failed", null);
                e.printStackTrace();
            }

            clearIssueEntries();
        });

        JFXButton noButton = new JFXButton("NO");
        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
            JFXButton button = new JFXButton("That's Okay");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "Issue Cancelled", null);
            clearIssueEntries();
        });


        AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(yesButton, noButton), "Confirm Issue",
                String.format("Are you sure want to issue the book '%s' to '%s' ?", book.getTitle(), member.getName()));
    }

    @FXML
    private void loadBookInfo2(ActionEvent event) {
//        clearEntries();
//        ObservableList<String> issueData = FXCollections.observableArrayList();
//        isReadyForSubmission = false;
//
//        try {
//            String id = bookID.getText();
//            String myQuery = "SELECT ISSUE.bookID, ISSUE.memberID, ISSUE.issueTime, ISSUE.renew_count,\n"
//                    + "MEMBER.name, MEMBER.mobile, MEMBER.email,\n"
//                    + "BOOK.title, BOOK.author, BOOK.publisher\n"
//                    + "FROM ISSUE\n"
//                    + "LEFT JOIN MEMBER\n"
//                    + "ON ISSUE.memberID=MEMBER.ID\n"
//                    + "LEFT JOIN BOOK\n"
//                    + "ON ISSUE.bookID=BOOK.ID\n"
//                    + "WHERE ISSUE.bookID='" + id + "'";
//            ResultSet rs = databaseHandler.execQuery(myQuery);
//            if (rs.next()) {
//                memberNameHolder.setText(rs.getString("name"));
//                memberContactHolder.setText(rs.getString("mobile"));
//                memberEmailHolder.setText(rs.getString("email"));
//
//                bookNameHolder.setText(rs.getString("title"));
//                bookAuthorHolder.setText(rs.getString("author"));
//                bookPublisherHolder.setText(rs.getString("publisher"));
//
//                Timestamp mIssueTime = rs.getTimestamp("issueTime");
//                Date dateOfIssue = new Date(mIssueTime.getTime());
//                issueDateHolder.setText(LibraryAssistantUtil.formatDateTimeString(dateOfIssue));
//                Long timeElapsed = System.currentTimeMillis() - mIssueTime.getTime();
//                Long days = TimeUnit.DAYS.convert(timeElapsed, TimeUnit.MILLISECONDS) + 1;
//                String daysElapsed = String.format("Used %d days", days);
//                numberDaysHolder.setText(daysElapsed);
//                Float fine = LibraryAssistantUtil.getFineAmount(days.intValue());
//                if (fine > 0) {
//                    fineInfoHolder.setText(String.format("Fine : %.2f", LibraryAssistantUtil.getFineAmount(days.intValue())));
//                } else {
//                    fineInfoHolder.setText("");
//                }
//
//                isReadyForSubmission = true;
//                disableEnableControls(true);
//                submissionDataContainer.setOpacity(1);
//            } else {
//                JFXButton button = new JFXButton("Okay.I'll Check");
//                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "No such Book Exists in Issue Database", null);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    @FXML
    private void loadSubmissionOp(ActionEvent event) {
//        if (!isReadyForSubmission) {
//            JFXButton btn = new JFXButton("Okay!");
//            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Please select a book to submit", "Cant simply submit a null book :-)");
//            return;
//        }
//
//        JFXButton yesButton = new JFXButton("YES, Please");
//        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent ev) -> {
//            String id = bookID.getText();
//            String ac1 = "DELETE FROM ISSUE WHERE BOOKID = '" + id + "'";
//            String ac2 = "UPDATE BOOK SET ISAVAIL = TRUE WHERE ID = '" + id + "'";
//
//            if (databaseHandler.execAction(ac1) && databaseHandler.execAction(ac2)) {
//                JFXButton btn = new JFXButton("Done!");
//                btn.setOnAction((actionEvent) -> {
//                    bookID.requestFocus();
//                });
//                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Book has been submitted", null);
//                disableEnableControls(false);
//                submissionDataContainer.setOpacity(0);
//            } else {
//                JFXButton btn = new JFXButton("Okay.I'll Check");
//                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Submission Has Been Failed", null);
//            }
//        });
//        JFXButton noButton = new JFXButton("No, Cancel");
//        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent ev) -> {
//            JFXButton btn = new JFXButton("Okay!");
//            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Submission Operation cancelled", null);
//        });
//
//        AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(yesButton, noButton), "Confirm Submission Operation", "Are you sure want to return the book ?");
    }

    @FXML
    private void loadRenewOp(ActionEvent event) {
//        if (!isReadyForSubmission) {
//            JFXButton btn = new JFXButton("Okay!");
//            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Please select a book to renew", null);
//            return;
//        }
//        JFXButton yesButton = new JFXButton("YES, Please");
//        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
//            String ac = "UPDATE ISSUE SET issueTime = CURRENT_TIMESTAMP, renew_count = renew_count+1 WHERE BOOKID = '" + bookID.getText() + "'";
//            System.out.println(ac);
//            if (databaseHandler.execAction(ac)) {
//                JFXButton btn = new JFXButton("Alright!");
//                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Book Has Been Renewed", null);
//                disableEnableControls(false);
//                submissionDataContainer.setOpacity(0);
//            } else {
//                JFXButton btn = new JFXButton("Okay!");
//                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Renew Has Been Failed", null);
//            }
//        });
//        JFXButton noButton = new JFXButton("No, Don't!");
//        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
//            JFXButton btn = new JFXButton("Okay!");
//            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Renew Operation cancelled", null);
//        });
//        AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(yesButton, noButton), "Confirm Renew Operation", "Are you sure want to renew the book ?");
    }

    private Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }

    @FXML
    private void handleMenuClose(ActionEvent event) {
        getStage().close();
    }

    @FXML
    private void handleMenuAddBook(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/ui/addbook/add_book.fxml"), "Add New Book", null);
    }

    @FXML
    private void handleMenuAddMember(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/ui/addmember/member_add.fxml"), "Add New Member", null);
    }

    @FXML
    private void handleMenuViewBook(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/ui/listbook/book_list.fxml"), "Book List", null);
    }

    @FXML
    private void handleAboutMenu(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/ui/about/about.fxml"), "About Me", null);
    }

    @FXML
    private void handleMenuSettings(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/ui/settings/settings.fxml"), "Settings", null);
    }

    @FXML
    private void handleMenuViewMemberList(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/ui/listmember/member_list.fxml"), "Member List", null);
    }

    @FXML
    private void handleIssuedList(ActionEvent event) {
        Object controller = LibraryAssistantUtil.loadWindow(getClass().getResource("/library/ui/issuedlist/issued_list.fxml"), "Issued Book List", null);
        if (controller != null) {
            IssuedListController cont = (IssuedListController) controller;
            cont.setBookReturnCallback(this);
        }
    }

    @FXML
    private void handleMenuFullScreen(ActionEvent event) {
        Stage stage = getStage();
        stage.setFullScreen(!stage.isFullScreen());
    }

    private void initDrawer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/library/ui/main/toolbar/toolbar.fxml"));
            VBox toolbar = loader.load();
            drawer.setSidePane(toolbar);
            ToolbarController controller = loader.getController();
            controller.setBookReturnCallback(this);

            drawer.open();
            drawer.toFront();
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
//        HamburgerSlideCloseTransition task = new HamburgerSlideCloseTransition(hamburger);
//        task.setRate(-1);
//        hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
//            drawer.toggle();
//        });
//        drawer.setOnDrawerOpening((event) -> {
//            task.setRate(task.getRate() * -1);
//            task.play();
//            drawer.toFront();
//        });
//        drawer.setOnDrawerClosed((event) -> {
//            drawer.toBack();
//            task.setRate(task.getRate() * -1);
//            task.play();
//        });
    }

    private void clearEntries() {
        memberNameHolder.setText("");
        memberEmailHolder.setText("");
        memberContactHolder.setText("");

        bookNameHolder.setText("");
        bookAuthorHolder.setText("");
        bookPublisherHolder.setText("");

        issueDateHolder.setText("");
        numberDaysHolder.setText("");
        fineInfoHolder.setText("");

        disableEnableControls(false);
        submissionDataContainer.setOpacity(0);
    }

    private void disableEnableControls(Boolean enableFlag) {
        if (enableFlag) {
            renewButton.setDisable(false);
            submissionButton.setDisable(false);
        } else {
            renewButton.setDisable(true);
            submissionButton.setDisable(true);
        }
    }

    private void clearIssueEntries() {
        bookIDInput.clear();
        memberIDInput.clear();
//        bookName.setText("");
//        bookAuthor.setText("");
//        bookStatus.setText("");
//        memberMobile.setText("");
//        memberName.setText("");
//        enableDisableGraph(true);
    }

    private void initGraphs() {
        bookIssueTab.setOnSelectionChanged((Event event) -> {
            clearIssueEntries();
        });
    }

    private boolean checkForIssueValidity() {
        bookIDInput.fireEvent(new ActionEvent());
        memberIDInput.fireEvent(new ActionEvent());
        return bookIDInput.getText().isEmpty() || memberIDInput.getText().isEmpty()
                || memberName.getText().isEmpty() || bookName.getText().isEmpty()
                || bookName.getText().equals(NO_SUCH_BOOK_AVAILABLE) || memberName.getText().equals(NO_SUCH_MEMBER_AVAILABLE);
    }

    @Override
    public void loadBookReturn(String bookID) {
        this.bookID.setText(bookID);
        mainTabPane.getSelectionModel().select(renewTab);
        loadBookInfo2(null);
        getStage().toFront();
    }

    @FXML
    private void handleIssueButtonKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadIssueOperation(null);
        }
    }

    private void initComponents() {
        mainTabPane.tabMinWidthProperty().bind(rootAnchorPane.widthProperty().divide(mainTabPane.getTabs().size()).subtract(15));
    }

    @FXML
    private void handleMenuOverdueNotification(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/ui/notifoverdue/overdue_notification.fxml"), "Notify Users", null);
    }

}