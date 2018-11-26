package gui;

import finalproject.Snake;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

/* *****************************************
 * * CSCI205 - Software Engineering and Design
 * * Fall 2018
 * *
 * * Name: Matt McLaughlin
 * * Date: Nov 9, 2018
 * * Time: 6:09:13 PM
 * *
 * * Project: csci205
 * * Package:
 * * File: GameController
 * * Description:
 * *
 * * ****************************************
 *  */
/**
 *  *
 *   * @author mpm022
 *
 */
public class GameController implements EventHandler<ActionEvent> {//implements EventHandler<ActionEvent>{

    private GameView theView;
    private GameModel theModel;
    //private Snake theSnake;
    private SnakeTask theTask;
    private GameGrid grid;
    private Thread th;

    public GameController(GameView theView, GameModel theModel) {
        this.theView = theView;
        this.theModel = theModel;
        //this.theSnake = theModel.getSnake();
        grid = new GameGrid(50, 15);

        VBox rootNode = theView.getRootNode();
        Label keyPressed = theView.getLbl();
        this.theView.getPlayBtn().setOnAction(this);
        this.theView.getOptionsBtn().setOnAction(this);
        this.theView.getLdrBoardBtn().setOnAction(this);
        this.theView.getBackBtn().setOnAction(this);

        rootNode.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.W) && theModel.getSnake().getDirection() != "up") {
                    theModel.getSnake().setDirection("down");
                }
                if (ke.getCode().equals(KeyCode.A) && theModel.getSnake().getDirection() != "right") {
                    theModel.getSnake().setDirection("left");
                }
                if (ke.getCode().equals(KeyCode.S) && theModel.getSnake().getDirection() != "down") {
                    theModel.getSnake().setDirection("up");
                }
                if (ke.getCode().equals(KeyCode.D) && theModel.getSnake().getDirection() != "left") {
                    theModel.getSnake().setDirection("right");
                }
                if (theModel.getSnake().getDie() == true) {
                    theTask.cancel();
                }

                //keyPressed.setText("Key Pressed: " + ke.getCode());
            }
        });

    }

    //Yuxuan's all-in-one event handler
    public void handle(ActionEvent event) {
        try {
            int x = 1;//

            Object source = event.getSource();
            if (source == theView.getPlayBtn()) {
                gameWindow();
            }
            if (source == theView.getOptionsBtn()) {
                options();
            }
            if (source == theView.getLdrBoardBtn()) {
                Label ldrBoardTxt = new Label("Leaderboard goes here!");
                //File ldrBoardFile = new File(something.txt);

                theView.getRootNode().getChildren().clear();
                theView.getRootNode().getChildren().add(theView.getBackBtn());
                theView.getBackBtn().setAlignment(Pos.TOP_LEFT);
                //theView.getRootNode().getChildren().add(ldrBoardFile);

                theView.getRootNode().getChildren().add(ldrBoardTxt);
            }
            if (source == theView.getBackBtn()) {
                backToMain();
            }
        } catch (Exception ex) {
        }

    }

    private void options() {
        Label optionsTxt = new Label("Options go here!");
        theView.getRootNode().getChildren().clear();
        theView.getRootNode().getChildren().add(theView.getBackBtn());
        theView.getRootNode().getChildren().add(theView.getMapSize());
        theView.getRootNode().getChildren().add(theView.getMapSizeIn());
        theView.getRootNode().getChildren().add(theView.getSnakeSpeed());
        theView.getRootNode().getChildren().add(theView.getSnakeSpeedIn());
        theView.getBackBtn().setAlignment(Pos.TOP_LEFT);
        theView.getRootNode().getChildren().add(optionsTxt);
    }

    private void gameWindow() {
        theView.getRootNode().getChildren().clear();
        theView.getRootNode().getChildren().add(theView.getBackBtn());
        theView.getBackBtn().setAlignment(Pos.TOP_LEFT);
        theView.getRootNode().getChildren().add(grid.getPane());

        theModel.refreshModel();
        grid.setIsFood(false);
        //theSnake = theModel.getSnake();
        theTask = new SnakeTask(theView, theModel);
        th = new Thread(theTask);
        th.setDaemon(true);
        th.start();
    }

    private void backToMain() {
        theView.getRootNode().getChildren().clear();
        theView.getRootNode().setAlignment(Pos.CENTER);
        theView.getRootNode().setSpacing(20);
        theView.getRootNode().getChildren().addAll(
                theView.getGameTitle(), theView.getHowTo(),
                theView.getPlayBtn(),
                theView.getOptionsBtn(),
                theView.getLdrBoardBtn());
        theTask.cancel();
    }

    private void UpdateGui(int score, Snake theSnake) {
        /*
        this.theView.getGrid().clearGrid();
        this.theView.getGrid().addSnake(theSnake);
        this.theView.getGrid().paintSnake();*/
        grid.clearGrid();
        grid.addSnake(theSnake);
        grid.paintSnake();
        grid.generateFood();
        grid.paintFood();

        theView.getRootNode().getChildren().clear();
        theView.getRootNode().getChildren().add(theView.getBackBtn());
        theView.getRootNode().getChildren().add(theView.getCurrentScore());
        theView.getRootNode().getChildren().add(theView.getScoreShown());
        this.theView.getScoreShown().setText(Integer.toString(score));
        theView.getBackBtn().setAlignment(Pos.TOP_LEFT);
        theView.getRootNode().getChildren().add(grid.getPane());
    }

    //on the game map, set up keyboard event handler
    //out of map, use removeEventHandler()
    class SnakeTask extends Task<Integer> {

        private int score;
        private GameView theView;
        private GameModel theModel;
        private Snake theSnake;

        public SnakeTask(GameView theView, GameModel theModel) {
            this.theView = theView;
            this.theModel = theModel;
            this.score = 0;
            this.theSnake = theModel.getSnake();
            theSnake.setMapSize(grid.getsize());
        }

        @Override
        protected Integer call() throws Exception {
            while (this.theSnake.getDie() != true) {
                theSnake.move();
                score = theSnake.getSnake().size();
                Thread.sleep(theSnake.getSpeed());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        GameController.this.UpdateGui(score, theSnake);
                    }
                });
            }
            return score;
        }

        public void changeDirection(String direction) {
            theSnake.setDirection(direction);
        }

    }

}
