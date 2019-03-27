/*
 * Copyright 2017 NKI/AvL
 *
 * This file is part of PALGARecoder.
 *
 * PALGARecoder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PALGARecoder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PALGARecoder. If not, see <http://www.gnu.org/licenses/>
 */

package recoder.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import recoder.data.in.InputData;
import recoder.data.in.InputDataFactory;
import recoder.gui.resourcemanagement.ResourceManager;
import recoder.settings.RunParameters;
import recoder.utils.LogTracker;
import recoder.utils.TextAreaAppender;

/**
 * Graphical User Interface
 */
public class PALGARecoderGUI extends Application {
    private static final Logger logger = LogManager.getLogger(PALGARecoderGUI.class.getName());
    private static final ResourceManager resourceManager = new ResourceManager();

    private static final int sceneWidth = 800;
    private static final int sceneHeight = 500;

    private TextArea logArea;

    private RunParameters runParameters=getDefaultParameters();

    public static void main(String ... args) {
        launch(args);
    }

    /**
     * Create the main GUI window
     *
     * @param primaryStage the primary stage
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("PALGA Protocol Data Recoder");

        // create the components
        Node topPane = setupTopPane();
        Node centerPane = setupCenterPane();
        Node bottomPane = setupBottomPane();

        // add them to a borderpane
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topPane);
        borderPane.setCenter(centerPane);
        borderPane.setBottom(bottomPane);

        // create the scene, set the scene
        Scene scene = new Scene(borderPane, sceneWidth, sceneHeight);
        primaryStage.setScene(scene);

        // create an icon
        primaryStage.getIcons().add(resourceManager.getResourceImage("icon.png"));
        // add the stylesheet
        scene.getStylesheets().add(resourceManager.getResourceStyleSheet("style.css"));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Create top pane
     *
     * @return the Node which will be added to the borderpane
     */
    private Node setupTopPane() {
        final int imageSpan = 4;

        GridPane gridPane = new GridPane();
        // give the pane an id
        gridPane.setId("topPane");
        gridPane.getStyleClass().add("fillBackground");
        // add spacings for when we add new components and add some padding
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        int rowNr = 0;
        // create an antonie image view, which will be our left-most item in our grid
        ImageView imv = new ImageView();
        imv.setFitHeight(80);
        imv.setFitWidth(170);
        imv.setImage(resourceManager.getResourceImage("healthri.png"));

        // add the image view to an HBox, as this will allow us to create some padding for this component
        HBox hb = new HBox();
        hb.setPadding(new Insets(0, 30, 0, 0));
        hb.getChildren().add(imv);
        // add the hbox to the grid, allowing it to span 4 columns and 5 rows
        gridPane.add(hb, 0, rowNr, imageSpan, imageSpan+1);

        // create a title
        Text sceneTitle = new Text("PALGA Protocol Data Recoder");
        sceneTitle.setId("title");
        gridPane.add(sceneTitle, imageSpan + 1, rowNr, 2, 1);

        return gridPane;
    }

    /**
     * Create center pane which contains the log area
     *
     * @return the Node which will be added to the borderpane
     */
    private Node setupCenterPane(){
        // center pane currently contains only logarea, which we add to an hbox an return
        HBox hBox = new HBox();
        hBox.getStyleClass().add("fillBackground");
        createLogArea();
        hBox.getChildren().addAll(logArea);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    /**
     * create the log area
     */
    private void createLogArea(){
        logArea = new TextArea();
        logArea.setPadding(new Insets(5, 5, 5, 5));
        logArea.setPrefWidth(sceneWidth);
        logArea.setEditable(false);
        logArea.setId("logArea");
        logArea.setText(StaticTexts.getWelcomeText());
        logArea.setWrapText(true);
        TextAreaAppender.setTextArea(logArea);
    }

    /**
     * Create the bottom pane, which contains buttons to e.g. run and exit the program
     *
     * @return the Node which will be added to the borderpane
     */
    private Node setupBottomPane(){
        HBox hBox = new HBox();
        hBox.getStyleClass().add("fillBackground");

        hBox.setPadding(new Insets(20, 12, 20, 12));
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.BASELINE_RIGHT);

        Button buttonClear = new Button("Clear");
        buttonClear.setPrefSize(100, 20);
//        buttonClear.setOnAction(event -> logArea.setText(StaticTexts.getWelcomeText()));

        // add some buttons and tell what to do when the button is clicked
        Button buttonRun = new Button("Run");
        buttonRun.setPrefSize(100, 20);
        buttonRun.setOnAction(event -> startTask());

        Button buttonExit = new Button("Exit");
        buttonExit.setPrefSize(100, 20);
        buttonExit.setOnAction(event -> System.exit(0));

        // Give Help and About their own hbox, which we align to the right
        HBox rightBox = new HBox();
        rightBox.setAlignment(Pos.BASELINE_RIGHT);

        Hyperlink aboutHyperlink = new Hyperlink("About");
        aboutHyperlink.getStyleClass().add("hyperlink");
        aboutHyperlink.setOnAction(event -> logArea.setText(StaticTexts.getAboutText()));

        Hyperlink helpHyperlink = new Hyperlink("Help");
        helpHyperlink.getStyleClass().add("hyperlink");
        aboutHyperlink.setOnAction(event -> AboutWindow.showAbout());

        // add to boxes
        rightBox.getChildren().addAll(helpHyperlink, aboutHyperlink);
        hBox.getChildren().addAll(buttonClear, buttonRun, buttonExit, rightBox);

        // give the right button a margin to push it to the center of the page
        HBox.setMargin(buttonExit, new Insets(0,150,0,0));

        return hBox;
    }


    /**
     * default parameters used the first time the wizard is started
     * @return the default parameters
     */
    private static RunParameters getDefaultParameters(){
        return new RunParameters();
    }

    /**
     *
     * Called after the runbutton is clicked.
     * Starts a Thread to do the actual work.
     */
    private void startTask(){
        logArea.clear();
        GUIWizard GUIWizard = new GUIWizard();
        try {
            if(GUIWizard.startWizard(runParameters)) {
                runParameters = GUIWizard.getRunParameters();
                new Thread(new WorkTask()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logArea.appendText("");
    }


    /**
     * does the work
     */
    private class WorkTask extends Task {
        WorkTask(){

        }

        /**
         * creates the captionoverwriter, generates the codebook items, creates the codebook,
         * saves the codebook and write the conflicting captions to a file
         * @return null
         */
        @Override
        public Void call() {
            try {

                // clear the logtracker, as the messages stored there are run specific and should therefore be cleared
                LogTracker.clearLog();
                InputData inputData = InputDataFactory.getInputData(runParameters);
                if(inputData!=null) {
                    inputData.translate();
                    inputData.writeOutput();

                    Platform.runLater(() -> logger.log(Level.INFO, "Done."));
                }
                logger.log(Level.INFO, "Finished!");
            } catch (Exception e) {
                logger.error("A severe error occurred:\n" + e.getMessage() + "\n");
                e.printStackTrace();
            }
            return null;
        }
    }
}
