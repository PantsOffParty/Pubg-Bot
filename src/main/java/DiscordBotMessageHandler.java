import Database.DatabaseConnector;
import Pubg.Api.Client.PubgApiClient;
import Util.ConfigHandler;
import Util.Edge;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

import static java.awt.Color.*;

/*
TODO break if message received into function calls and methods
TODO Add documentation to everything
TODO Fix color selection. Again. Sanhokt only.
TODO PATHING 1.Color grab buildings 2.Lines between them
TODO Pubg API - Do something useful with stats
*/
public class DiscordBotMessageHandler extends ListenerAdapter {

    //Class Variables
    private final String tempDir = System.getProperty("java.io.tmpdir"); //Stores output images
    private Point currentCoordinates = new Point(); //Stores Current Point for win recording
    private Map<String, Point> currentCoordinatesMap = new HashMap<>();
    private char currentMap = ' ';  //Stores current map character
    private Random rand = new Random(); //Random generator for coordinate generation
    private boolean waitingForWinConfirmation = false; //Do we check to see if the next message is confirming a win
    private boolean waitingForCoordinatesSelection = false; //Multi drop win selection is active
    private Vector<Point> unvisitedBuildings = new Vector<>();
    private PubgApiClient apiClient = new PubgApiClient();
    private DatabaseConnector db = new DatabaseConnector();
    private Vector<Edge> myEdges = new Vector<>();
    private Vector<Point> visitedBuildings = new Vector<>();

    //Stuff for Strategy generation. pulled out so it doesn't rerun every time a message is received
    private final String[] strat = new String[]{
            "Fast and Loose",
            "Hyper-aggressive",
            "Mounted Combat",
            "Play It Safe",
            "Slow and Steady",
            "Run and Gun",
            "Grenadier's Gamble",
            "Shorts and Shotties",
            "Long-Range Overwatch",
            "Amphibious Assault",
            "Have Gay Sex",
            "Breach and Clear",
            "Chase All Shots",
            "Hold the High Ground",
            "Hold the Low Ground",
            "Hold Down the Fort",
            "Crates are Key",
            "Stay on the Roads",
            "Spread Out",
            "Keep Friends Close",
            "Make 'em Bleed",
            "Use your Fuckin' Brains, Retards",
            "Mountain Goat",
            "One Gun Salute"
    };

    private final int STRATNUM = strat.length;
    private final static Map<String, String> helpMap; //Map to store command list and action

    static {
        helpMap = new HashMap<>();
        helpMap.put("!ping", "Check if the bot is online.");
        helpMap.put("!win", "Save winning map position to the server.");
        helpMap.put("!strategy", "Be given a random strategy for how to play out the next round.");
        helpMap.put("!drop (e,m,s) [#] OR !", "Be given a random position to drop in the next round.");
        helpMap.put("!help", "View all possible bot commands.");
        helpMap.put("!allwin (e,m,s)", "Display a map of all starting coordinates that resulted in a win for a given map.");
        helpMap.put("!stop", "Stops all instances of God Bot.");
        helpMap.put("!path (e,m,s)", "Draws the most efficient starting path.");

    }

    //Logs Bot into Discord and gets ready to receive Messages
    DiscordBotMessageHandler() {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(ConfigHandler.getBotConfig("bot.token"));
        builder.addEventListener(this);
        try {
            builder.buildAsync();
        } catch (LoginException e) {
            System.err.println("Unable to login.");
        }
    }

    //Repsonds to discord commands received
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        //Ignores message if the author is a bot
        if (event.getAuthor().isBot()) {
            return;
        }

        //Outputs message author and content to terminal
        System.out.println("We received a message from " +
                event.getAuthor().getName() + ": " +
                event.getMessage().getContentDisplay()
        );


        //Message Text, already raw and lowercase
        //Unless being used for stats, then retain case
        String messageText = "";
        if (!event.getMessage().getContentRaw().toLowerCase().contains("!stats")) {
            messageText = event.getMessage().getContentRaw().toLowerCase();
        } else {
            messageText = event.getMessage().getContentRaw();
        }

        //Stops all instances of God Bot
        if (messageText.equals("!stop")) {
            event.getChannel().sendMessage("Shutting down...").queue();
            System.exit(0);
        }

        //For confirming a !win command.
        if (waitingForWinConfirmation) {
            waitingForWinConfirmation = false;
            if (messageText.equals("y")) {
                exportWinningDropCoordinates();
                event.getChannel().sendMessage("Winning coordinates have been saved!").queue();
                return;
            }
            event.getChannel().sendMessage("No win confirmation was given.").queue();
        }

        /*
         * Select which coordinates to use when winning a multi drop.
         */
        if (waitingForCoordinatesSelection) {
            if (currentCoordinatesMap.keySet().contains(messageText)) {
                currentCoordinates = currentCoordinatesMap.get(messageText);
                exportWinningDropCoordinates();
                waitingForCoordinatesSelection = false;
                event.getChannel().sendMessage("Coordinates saved.").queue();
                return;
            } else if (messageText.equals("!exit")) {
                event.getChannel().sendMessage("No coordinate selection made.").queue();
                waitingForCoordinatesSelection = false;
                return;
            } else {
                event.getChannel().
                        sendMessage(messageText + " is not a valid selection. Try again or type !exit to cancel coordinate capture.").queue();
                return;
            }
        }

        //OG Test if Bot is working
        if (messageText.equals("!ping")) {
            event.getChannel().sendMessage("Pong!").queue();
        }

        //Stores winning coordinates in file after confirmation
        if (messageText.equals("!win")) {
            if (currentCoordinatesMap.size() == 1) {
                currentCoordinates = currentCoordinatesMap.get("1");
                event.getChannel().sendMessage(" (Y,N) - Confirm you want to save a win at coordinates (" +
                        currentCoordinates.getX() + ", " + currentCoordinates.getY() + ")").queue();
                event.getChannel().sendFile(getLastOutputFile()).queue();
                waitingForWinConfirmation = true;
            } else {
                event.getChannel().sendMessage("Which drop position would you like to save? " + currentCoordinatesMap.keySet()).queue();
                waitingForCoordinatesSelection = true;
            }
        }

        //Outputs random strategy to discord
        if (messageText.equals("!strategy")) {
            int strategy = rand.nextInt(STRATNUM);
            String message1 = "Optimal Strategy: " + strat[strategy];
            event.getChannel().sendMessage(message1).queue();
        }

        //Generates random drop on chosen map and outputs file in discord
        if (messageText.contains("!drop") || messageText.equals("!")) {
            String cmdSplit[] = messageText.split(" ", 3);
            BufferedImage img;
            if (cmdSplit.length == 1 || messageText.equals("!")) {
                img = getImageFromResource("PUBGMAP1.jpg");
                currentMap = 's';
            } else {
                switch (cmdSplit[1]) {
                    case "s":
                        img = getImageFromResource("PUBGMAP1.jpg");
                        currentMap = 's';
                        break;
                    case "m":
                        img = getImageFromResource("PUBGMAP2.jpg");
                        currentMap = 'm';
                        break;
                    case "e":
                        img = getImageFromResource("PUBGMAP3.jpg");
                        currentMap = 'e';
                        break;
                    default:
                        img = getImageFromResource("PUBGMAP1.jpg");
                        currentMap = 's';
                        break;
                }
            }
            assert img != null;

            if (cmdSplit.length == 3) {
                int dropCount = Integer.parseInt(cmdSplit[2]);
                generateDropPosition(img, dropCount);
            } else {
                int dropCount = 1;
                generateDropPosition(img, dropCount);
            }
            event.getChannel().sendFile(writeOutputFile(img)).queue();
        }

        //Regurgitate all winning coordinates for a given map
        if (messageText.contains("!allwin")) {
            String mapKey = "s";
            if (messageText.split(" ").length != 1) {
                mapKey = messageText.split(" ")[1];
            }
            try {
                event.getChannel().sendFile(writeOutputFile(getAllWinCoordinatesImage(mapKey))).queue();
            } catch (IOException e) {
                event.getChannel().sendMessage("You have not captured a win for this map because you suck, RYAN.").queue();
            }
        }

        //Marks a starting path for our intrepid adventurers from the drop point
        if (messageText.contains("!path")) {
            String cmdSplit[] = messageText.split(" ", 3);
            BufferedImage img;

            if (cmdSplit.length == 1 || messageText.equals("!")) {
                img = getImageFromResource("PUBGMAP1.jpg");
                currentMap = 's';
            } else {
                switch (cmdSplit[1]) {
                    case "s":
                        img = getImageFromResource("PUBGMAP1.jpg");
                        currentMap = 's';
                        break;
                    case "m":
                        img = getImageFromResource("PUBGMAP2.jpg");
                        currentMap = 'm';
                        break;
                    case "e":
                        img = getImageFromResource("PUBGMAP3.jpg");
                        currentMap = 'e';
                        break;
                    default:
                        img = getImageFromResource("PUBGMAP1.jpg");
                        currentMap = 's';
                        break;
                }
                assert img != null;
            }

            assert img != null;
            generatePathPositionImage(img);
            event.getChannel().sendFile(writeOutputFile(img)).queue();
            //Debugging
            String vectorSize = "This vector holds " + String.valueOf(unvisitedBuildings.size()) + " nodes.";
            event.getChannel().sendMessage(vectorSize).queue();
            String edgesSize = "The edge vector holds " + String.valueOf(myEdges.size()) + " edges.";
            event.getChannel().sendMessage(edgesSize).queue();
        }


        //Outputs command list to discord
        if (messageText.equals("!help")) {
            StringBuilder messageToSend = new StringBuilder();
            for (Map.Entry<String, String> command : helpMap.entrySet()) {
                messageToSend.append(command.getKey())
                        .append(" : ")
                        .append(command.getValue())
                        .append("\n");
            }
            event.getChannel().sendMessage(messageToSend.toString()).queue();
        }

        /*
         * Prints the stats for a gametype and player name (Case Sensitive)
         */
        if (messageText.contains("!stats")) {
            handleStatsCommand(messageText, event);
        }
    }

    //TODO Fix this to handle bad inputs
    private void handleStatsCommand(String messageText, MessageReceivedEvent event) {
        String gameType = messageText.split(" ")[1];
        String playerName = messageText.split(" ")[2];
        String output = "";

        if (gameType.equals("duos")) {
            try {
                output = apiClient.getDuosStatsForPlayer(playerName).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (gameType.equals("squads")) {
            try {
                output = apiClient.getSquadsForPlayer(playerName).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        event.getChannel().sendMessage(output).queue();
    }

    //Start of DropPosition generation. Generates random position and calls image creater
    private void generateDropPosition(BufferedImage image, int optionCount) {
        int imgH = image.getHeight();
        int imgW = image.getWidth();

        for (int i = 1; i < optionCount + 1; i++) {
            while (true) {
                int x = rand.nextInt(imgW);
                int y = rand.nextInt(imgH);

                int colorRGB = image.getRGB(x, y);
                Color color = new Color(colorRGB);
                //Picking non water position and call generateImage
                if ((color.getBlue() <= color.getRed() && color.getBlue() <= color.getGreen()) || (color.getBlue() <= 50 && color.getGreen() <= 50 && color.getRed() >= 20)) {
                    generateDropPositionImageMULTI(image, x, y, i);
                    currentCoordinatesMap.put(String.valueOf(i), new Point(x, y));
                    break;
                }
            }
        }
    }

    //Multi-Drop Version that generates an image with given coords with a different color incremented number
    private void generateDropPositionImageMULTI(BufferedImage image, int x, int y, int color) {
        color--;
        List<Color> colors = Arrays.asList(
                RED,
                BLUE.brighter().brighter(),
                GREEN,
                YELLOW,
                ORANGE,
                PINK,
                CYAN);
        int xColor = color % colors.size();
        color++;
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setFont(new Font("Ariel", Font.PLAIN, 40));
        graphics2D.setColor(colors.get(xColor));
        graphics2D.drawString(String.valueOf(color), x, y);
    }

    //Marks a given point on image with given Mark
    private void markMapPosition(BufferedImage image, int x, int y, String mark) {

        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setFont(new Font("Ariel", Font.PLAIN, 90));
        graphics2D.setColor(RED);
        graphics2D.drawString(mark, x, y);
    }

    //Version for pathing. Picks buildings, then builds paths between
    private void generatePathPositionImage(BufferedImage image) {
        int imgH = image.getHeight();
        int imgW = image.getWidth();

        //Graphics settings
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setFont(new Font("Ariel", Font.PLAIN, 10));
        graphics2D.setColor(RED);
        //Making sure our building vector is empty
        unvisitedBuildings.clear();

        //Setting min and max coords for plotting around a drop site
        Point currentCoords = currentCoordinatesMap.get("1");
        int distance = 400; //Distance in pixels of the area to search in
        int squareDiameter = 25; //Diameter of the drawn square, shrinking this for more accurate results, but a bigger vector
        int startX = currentCoords.x - distance / 2;
        int startY = currentCoords.y - distance / 2;
        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;
        int maxX = currentCoords.x + distance / 2;
        int maxY = currentCoords.y + distance / 2;
        if (maxX > imgW) maxX = imgW;
        if (maxY > imgH) maxY = imgH;

        //Marks every building on map and adds to set
//Uncommenting this makes path run for the entire map
        for (int y = startY; y < maxY; y++) {
            for (int x = startX; x < maxX; x++) {
                int colorRGB = image.getRGB(x, y);
                Color color = new Color(colorRGB);


                //This loop will not work when the rectangles stop getting added, which is necessary to show a readable path
                //Draws rectangles to mark buildings MOSTLY FOR DEBUGGING
                if ((color.getRed() >= 150 && color.getGreen() >= 170 && color.getBlue() >= 170) && color != RED) {
                    graphics2D.setColor(RED);
//                    graphics2D.fillRect(x - squareDiameter / 2, y - squareDiameter / 2, squareDiameter, squareDiameter);
                    Point myPoint = new Point(x, y);
                    unvisitedBuildings.add(myPoint);
                    x += squareDiameter / 2;
                }
            }
        }

        //Plots lines between every point in set || Calculates edges between all nodes in unvisitedBuildings
        for(int startPos = 0; startPos < unvisitedBuildings.size() - 1; startPos++) {

            int currX = unvisitedBuildings.get(startPos).x;
            int currY = unvisitedBuildings.get(startPos).y;

            for (int destPos = 1; destPos < unvisitedBuildings.size(); destPos++) {
                int nextX = unvisitedBuildings.get(destPos).x;
                int nextY = unvisitedBuildings.get(destPos).y;
                graphics2D.setColor(RED);
                //graphics2D.drawLine(currX, currY, nextX, nextY);
                myEdges.add(new Edge(unvisitedBuildings.get(startPos),unvisitedBuildings.get(destPos)));
            }
        }
//        int startPos = 0;
//        int destPos = 1;
//        while( destPos < myEdges.size()){
//            int beginX = myEdges.get(startPos).pointA.x;
//            int beginY = myEdges.get(startPos).pointA.y;
//
//            if (myEdges.get(destPos).pointA.x == beginX && myEdges.get(destPos).pointA.y == beginY){
//                graphics2D.drawLine(beginX,beginY,myEdges.get(destPos).pointB.x,myEdges.get(destPos).pointB.y);
//            }
//            else if (myEdges.get(destPos).pointB.x == beginX && myEdges.get(destPos).pointB.y == beginY)
//            {
//                graphics2D.drawLine(myEdges.get(destPos).pointA.x,myEdges.get(destPos).pointA.y,beginX,beginY);
//            }
//            destPos++;
//        }

        //Adding drop position to edges to get distances necessary to find closest point

        int i;

        graphics2D.setColor(BLUE);
        graphics2D.setFont(new Font("Ariel", Font.PLAIN, 20));
        graphics2D.drawString("x",currentCoords.x,currentCoords.y);
        Color red = RED;
        graphics2D.setColor(RED);
        while(unvisitedBuildings.size() != 0) {
            Point closestPoint = null;
            double closestDistance=999999999;
            int closestIndex = -1;
            myEdges.clear();
            for (i = 0; i < unvisitedBuildings.size(); i++) {
                myEdges.add(new Edge(currentCoords, unvisitedBuildings.get(i)));
                if (myEdges.lastElement().distance <= closestDistance) {
                    closestDistance = myEdges.lastElement().distance;
                    closestPoint = unvisitedBuildings.get(i);
                    closestIndex = i;
                }
            }
            unvisitedBuildings.remove(closestIndex);
            graphics2D.drawLine(currentCoords.x, currentCoords.y, closestPoint.x, closestPoint.y);
            graphics2D.setColor(red);
            visitedBuildings.add(closestPoint);
            currentCoords = closestPoint;
        }
    }

    //Reads in an image into a BufferedImage object
    private BufferedImage getImageFromResource(String image) {
        try {
            return ImageIO.read(this.getClass().getResourceAsStream(image));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Outputs given image to tempdir
    private File writeOutputFile(BufferedImage imageToOutput) {
        File outputFile = new File(tempDir + "PUBGMAPEDIT.jpg");
        try {
            ImageIO.write(imageToOutput, "jpg", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    //Retrieve the last drop image
    private File getLastOutputFile() {
        return new File(tempDir + "PUBGMAPEDIT.jpg");
    }

    //Outputs current coords to the given map file
    private void exportWinningDropCoordinates() {
        db.insertWin((float)currentCoordinates.getX(), (float)currentCoordinates.getY(), String.valueOf(currentMap));
    }

    //Marks the given map with all coords from database and returns image
    private BufferedImage getAllWinCoordinatesImage(String mapKey) throws IOException {
        String mapImageName;
        BufferedImage image;

        //gets file
        switch (mapKey) {
            case "e":
                mapImageName = "PUBGMAP3.jpg";
                break;
            case "m":
                mapImageName = "PUBGMAP2.jpg";
                break;
            default:
                mapImageName = "PUBGMAP1.jpg";
                break;
        }
        image = getImageFromResource(mapImageName);

        List<Point> winPoints = db.getAllWinCoordinatesByMap(String.valueOf(currentMap));
        for(Point point : winPoints)
        {
            assert image != null;
            markMapPosition(image, (int)point.getX(), (int)point.getY(), "x");
        }
        return image;
    }
}
