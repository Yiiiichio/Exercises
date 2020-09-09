import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The version of JavaFX supports only Java 10 and above.
 * I couldn't find an older version of JavaFX available in the maven repository.
 * In order to run the application, need to update the JDK to at least JDK 10.
 */

// Use the command below in Terminal to run:
// mvn clean compile exec:java


public class Main extends Application {
    public static void main(String[] args) {
        //When we call launch(args) in the main method, JavaFx is going to create a UI thread for us.
        //The entry point into the UI thread is the @Orverride start() method below...
        //All UI events and rendering will happen in one single thread
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Crypto currency Prices");

        GridPane grid = createGrid();
        Map<String, Label> cryptoLabels = createCryptoPriceLabels();

        addLabelsToGrid(cryptoLabels, grid);

        double width = 300;
        double height = 250;

        StackPane root = new StackPane();

        root.getChildren().add(grid);

        primaryStage.setScene(new Scene(root, width, height));

        //Create pricesContainer object, which will be shared by UI thread and the worker thread
        PricesContainer pricesContainer = new PricesContainer();

        //Create priceUpdater thread
        PriceUpdater priceUpdater = new PriceUpdater(pricesContainer);

        //AnimationTimer will call the handle() callback on every frame that is shown to the user.
        //If the UI thread shows 30 frames per second, this handle is going to be called 30 times per minutes.
        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //get new prices from pricesContainer
                //using tryLock() on the shared object, which never allows for UI thread to be blocked
                //If i use pricesContainer.getLockObject().lock() instead of tryLock(), the UI responsiveness will decrease
                if (pricesContainer.getLockObject().tryLock()) {
                    try {
                        Label bitcoinLabel = cryptoLabels.get("BTC");
                        bitcoinLabel.setText(String.valueOf(pricesContainer.getBitcoinPrice()));

                        Label etherLabel = cryptoLabels.get("ETH");
                        etherLabel.setText(String.valueOf(pricesContainer.getEtherPrice()));

                        Label litecoinLabel = cryptoLabels.get("LTC");
                        litecoinLabel.setText(String.valueOf(pricesContainer.getLitecoinPrice()));

                        Label bitcoinCashLabel = cryptoLabels.get("BCH");
                        bitcoinCashLabel.setText(String.valueOf(pricesContainer.getBitcoinCashPrice()));

                        Label rippleLabel = cryptoLabels.get("XRP");
                        rippleLabel.setText(String.valueOf(pricesContainer.getRipplePrice()));
                    } finally {
                        pricesContainer.getLockObject().unlock();
                    }
                }
            }
        };

        animationTimer.start();

        priceUpdater.start();

        //After calling the show() method, the UI thread runs in an infinite loop responding to events
        //and updating the screen many times per second.
        //Since the UI thread has to response to events and show frames in real time,
        //it must never block or get suspended.
        primaryStage.show();
    }

    private void addWindowResizeListener(Stage stage, Rectangle background) {
        ChangeListener<Number> stageSizeListener = ((observable, oldValue, newValue) -> {
            background.setHeight(stage.getHeight());
            background.setWidth(stage.getWidth());
        });
        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener);
    }

    //A Label is basically a placeholder for some text to show in the UI
    //Create a Label for each asset and put them in a Map (the Map maps the Label's name to the Label object)
    private Map<String, Label> createCryptoPriceLabels() {
        Label bitcoinPrice = new Label("0");
        bitcoinPrice.setId("BTC");

        Label etherPrice = new Label("0");
        etherPrice.setId("ETH");

        Label liteCoinPrice = new Label("0");
        liteCoinPrice.setId("LTC");

        Label bitcoinCashPrice = new Label("0");
        bitcoinCashPrice.setId("BCH");

        Label ripplePrice = new Label("0");
        ripplePrice.setId("XRP");

        Map<String, Label> cryptoLabelsMap = new HashMap<>();
        cryptoLabelsMap.put("BTC", bitcoinPrice);
        cryptoLabelsMap.put("ETH", etherPrice);
        cryptoLabelsMap.put("LTC", liteCoinPrice);
        cryptoLabelsMap.put("BCH", bitcoinCashPrice);
        cryptoLabelsMap.put("XRP", ripplePrice);

        return cryptoLabelsMap;
    }

    //create a Grid element that I want to present to the user
    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        return grid;
    }

    //Iterate over all the map entries, and populate the GridPane
    private void addLabelsToGrid(Map<String, Label> labels, GridPane grid) {
        int row = 0;
        for (Map.Entry<String, Label> entry : labels.entrySet()) {
            String cryptoName = entry.getKey();
            Label nameLabel = new Label(cryptoName);
            nameLabel.setTextFill(Color.BLUE);
            //If mouse button is pressed down, text will turn red
            nameLabel.setOnMousePressed(event -> nameLabel.setTextFill(Color.RED));
            //If mouse is released, the text will turn blue
            nameLabel.setOnMouseReleased((EventHandler) event -> nameLabel.setTextFill(Color.BLUE));

            grid.add(nameLabel, 0, row);
            grid.add(entry.getValue(), 1, row);

            row++;
        }
    }

    @Override
    public void stop() {
        System.exit(0);
    }


    //This is the shared class to contain the assert prices.
    public static class PricesContainer {
        private Lock lockObject = new ReentrantLock();

        //Create variables for each crypto currency assets
        private double bitcoinPrice;
        private double etherPrice;
        private double litecoinPrice;
        private double bitcoinCashPrice;
        private double ripplePrice;

        public Lock getLockObject() {
            return lockObject;
        }

        public double getBitcoinPrice() {
            return bitcoinPrice;
        }

        public void setBitcoinPrice(double bitcoinPrice) {
            this.bitcoinPrice = bitcoinPrice;
        }

        public double getEtherPrice() {
            return etherPrice;
        }

        public void setEtherPrice(double etherPrice) {
            this.etherPrice = etherPrice;
        }

        public double getLitecoinPrice() {
            return litecoinPrice;
        }

        public void setLitecoinPrice(double litecoinPrice) {
            this.litecoinPrice = litecoinPrice;
        }

        public double getBitcoinCashPrice() {
            return bitcoinCashPrice;
        }

        public void setBitcoinCashPrice(double bitcoinCashPrice) {
            this.bitcoinCashPrice = bitcoinCashPrice;
        }

        public double getRipplePrice() {
            return ripplePrice;
        }

        public void setRipplePrice(double ripplePrice) {
            this.ripplePrice = ripplePrice;
        }
    }


    //This class takes PricesContainer into its constructor.
    //It simulates "sending network requests" by generating new prices
    public static class PriceUpdater extends Thread {
        private PricesContainer pricesContainer;
        //will need a Random to generate new price
        private Random random = new Random();

        //takes PricesContainer into its constructor
        public PriceUpdater(PricesContainer pricesContainer) {
            this.pricesContainer = pricesContainer;
        }

        //Give new price to each asset
        @Override
        public void run() {
            while (true) {
                pricesContainer.getLockObject().lock();

                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    pricesContainer.setBitcoinPrice(random.nextInt(20000));
                    pricesContainer.setEtherPrice(random.nextInt(2000));
                    pricesContainer.setLitecoinPrice(random.nextInt(500));
                    pricesContainer.setBitcoinCashPrice(random.nextInt(5000));
                    pricesContainer.setRipplePrice(random.nextDouble());
                } finally {
                    pricesContainer.getLockObject().unlock();
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}