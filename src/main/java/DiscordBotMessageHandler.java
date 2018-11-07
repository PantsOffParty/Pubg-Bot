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

/*
TODO break if message received into function calls and methods
TODO Add documentation to everything
TODO Fix random color generation in dropPosition overload 3
*/
public class DiscordBotMessageHandler extends ListenerAdapter {

    //Class Variables
    private final String tempDir = System.getProperty("java.io.tmpdir"); //Stores output images
    private Point currentCoordinates = new Point(); //Stores Current Point for win recording
    private char currentMap = ' ';  //Stores current map character
    private Random rand = new Random(); //Random generator for coordinate generation
    private boolean waitingForWinConfirmation = false; //Do we check to see if the next message is confirming a win

    //Stuff for Strategy generation. pulled out so it doesn't rerun every time a message is received
    private final String[] strat = new String[] {"Fast and Loose",
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
            "One Gun Salute"};

    private final int STRATNUM = strat.length;
    private final static Map<String, String> helpMap; //Map to store command list and action
    static
    {
        helpMap = new HashMap<>();
        helpMap.put("!ping", "Check if the bot is online.");
        helpMap.put("!win", "Save winning map position to the server.");
        helpMap.put("!strategy", "Be given a random strategy for how to play out the next round.");
        helpMap.put("!drop (e,m,s) OR !", "Be given a random position to drop in the next round.");
        helpMap.put("!help", "View all possible bot commands.");
        helpMap.put("!allwin (e,m,s)", "Display a map of all starting coordinates that resulted in a win for a given map.");
        helpMap.put("!stop", "Stops all instances of God Bot.");
    }

    DiscordBotMessageHandler()
    {
        //Logs Bot into Discord and gets ready to receive Messages
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        File file = new File("token.txt");
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert sc != null;
        String token = sc.next();
        builder.setToken(token);
        builder.addEventListener(this);
        try
        {
            builder.buildAsync();
        } catch (LoginException e) {
            System.err.println("Unable to login.");
        }
    }

    //Repsonds to discord commands received
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        //Outputs message author and content to terminal
        System.out.println("We received a message from " +
                event.getAuthor().getName() + ": " +
                event.getMessage().getContentDisplay()
        );

        //Ignores message if the author is a bot
        if (event.getAuthor().isBot()) {
            return;
        }

        //Message Text, already raw and lowercase
        String messageText = event.getMessage().getContentRaw().toLowerCase();

        //Stops all instances of God Bot
        if (messageText.equals("!stop")){
            event.getChannel().sendMessage("Shutting down...").queue();
            System.exit(0);
        }

        //For confirming a !win command.
        if (waitingForWinConfirmation)
        {
            waitingForWinConfirmation = false;
            if (messageText.equals("y"))
            {
                exportWinningDropCoordinates();
                event.getChannel().sendMessage("Winning coordinates have been saved!").queue();
                return;
            }
            event.getChannel().sendMessage("No win confirmation was given.").queue();
        }

        //OG Test if Bot is working
        if (messageText.equals("!ping")) {
            event.getChannel().sendMessage("Pong!").queue();
        }

        //Stores winning coordinates in file after confirmation
        if (messageText.equals("!win")) {
            event.getChannel().sendMessage(" (Y,N) - Confirm you want to save a win at coordinates (" +
                    currentCoordinates.getX() + ", " + currentCoordinates.getY() +  ")").queue();
            waitingForWinConfirmation = true;
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
            if (cmdSplit.length ==1 || messageText.equals("!")) {
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
                generateDropPositionImage(img, dropCount);
            }
            else {
                int dropCount = 1;
                generateDropPositionImage(img, dropCount);
            }
                event.getChannel().sendFile(writeOutputFile(img)).queue();
        }

        //Regurgitate all winning coordinates for a given map
        if (messageText.contains("!allwin"))
        {
            String mapKey = "s";
            if (messageText.split(" ").length != 1)
            {
                mapKey = messageText.split(" ")[1];
            }
            try {
                event.getChannel().sendFile(writeOutputFile(getAllWinCoordinatesImage(mapKey))).queue();
            } catch (IOException e) {
                event.getChannel().sendMessage("You have not captured a win for this map because you suck, RYAN.").queue();
            }
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
    }

    //Reads in an image into a BufferedImage object
    private BufferedImage getImageFromResource(String image) {
        try
        {
            return ImageIO.read(this.getClass().getResourceAsStream(image));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }


    //Overloaded Version that generates an image with given coords marked NOT RANDOM
    private void generateDropPositionImage(BufferedImage image, int x, int y){
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setFont(new Font("Ariel", Font.PLAIN, 50));
        graphics2D.setColor(Color.RED);
        graphics2D.drawString("x", x, y);
    }

    //Overloaded version for Multiple Drops
    private void generateDropPositionImage(BufferedImage image, int optionCount) {
        int imgH = image.getHeight();
        int imgW = image.getWidth();

        for (int i = 0; i < optionCount; i++) {
            while (true) {
                int x = rand.nextInt(imgW);
                int y = rand.nextInt(imgH);
                int red = rand.nextInt(256);
                int blue = rand.nextInt(256-red);
                int green = rand.nextInt(256-red-blue);
                Color c = new Color(red,green,blue);

                int colorRGB = image.getRGB(x, y);
                Color color = new Color(colorRGB);
                if ((color.getBlue() <= color.getRed() && color.getBlue() <= color.getGreen() && !color.equals(c)) || (color.getBlue() <= 50 && color.getGreen() <= 50 && color.getRed() >=20 && !color.equals(c))) {
                    generateDropPositionImage(image, x, y);
                    currentCoordinates.setLocation(x, y);
                    break;
                }
            }
        }
    }
    //Outputs given image to tempdir
    private File writeOutputFile(BufferedImage imageToOutput) {
        File outputFile = new File(tempDir + "PUBGMAPEDIT.jpg");
        try
        {
            ImageIO.write(imageToOutput, "jpg", outputFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return outputFile;
    }

    //Outputs current coords to the given map file
    private void exportWinningDropCoordinates()
    {
        String mapFileName = "";
        switch (currentMap) {
            case 's':
                mapFileName = "WinCoordinatesSanhok";
                break;
            case 'e':
                mapFileName = "WinCoordinatesErangel";
                break;
            case 'm':
                mapFileName = "WinCoordinatesMiramar";
                break;
        }
        try (BufferedWriter output = new BufferedWriter(new FileWriter(System.getenv("USERPROFILE") +
                "\\desktop\\" + mapFileName + ".txt", true)))
        {
            output.write(currentCoordinates.getX() + "," + currentCoordinates.getY());
            output.newLine();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //Marks the given map with all coords from file and returns image
    private BufferedImage getAllWinCoordinatesImage(String mapKey) throws IOException {
        String mapFileName;
        String mapImageName;
        BufferedImage image;

        //Sets map and gets file
        switch (mapKey) {
            case "e":
                mapFileName = "WinCoordinatesErangel";
                mapImageName = "PUBGMAP3.jpg";
                break;
            case "m":
                mapFileName = "WinCoordinatesMiramar";
                mapImageName = "PUBGMAP2.jpg";
                break;
            default:
                mapFileName = "WinCoordinatesSanhok";
                mapImageName = "PUBGMAP1.jpg";
                break;
        }
        image = getImageFromResource(mapImageName);
        BufferedReader br = new BufferedReader(new FileReader(System.getenv("USERPROFILE") +
                "\\desktop\\" + mapFileName + ".txt"));

        //Read in coords from file and add to image
        for (String line; (line = br.readLine()) != null; )
        {
            List<String> x_y_coords = Arrays.asList(line.split(","));
            int x = (int) Double.parseDouble(x_y_coords.get(0));
            int y = (int) Double.parseDouble(x_y_coords.get(1));

            assert image != null;
            generateDropPositionImage(image,x,y);
        }
        br.close();
        return image;
    }
}
