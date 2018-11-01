import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Random;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Vector;


public class Main extends ListenerAdapter {

    private String tempDir = System.getProperty("java.io.tmpdir");
    private Point currentCoordinates = new Point();
    private Random rand = new Random();

    public static void main(String[] args) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = "NDc0MzAzNDE2ODkwNTU2NDI2.DkOh9A.f6ssNPyNX2-ygwaP5MHj5yVxvTY";
        builder.setToken(token);
        builder.addEventListener(new Main());
        builder.buildAsync();
    }

    //TODO Remove strings and shit from onMessageReceived so everything doesn't get run everytime a message is received.
    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        System.out.println("We received a message from " +
                event.getAuthor().getName() + ": " +
                event.getMessage().getContentDisplay()
        );

        if (event.getAuthor().isBot()) {
            return;
        }

        //Message Text, already raw and lowercase
        String messageText = event.getMessage().getContentRaw().toLowerCase();

        //Deprecated
        //String loc[] = new String[]{"Cave","Bhan","Ha Tinh","Camp Alpha", "Ruins", "Tambang", "Na Kham", "Sahmee", "Camp Charlie", "Pai Nan", "Ban Tai", "Bootcamp", "Paradise Resort", "Tat Mok", "Khao", "Mongnai", "Camp Bravo", "Quarry", "Lakawi", "Kampong", "Docks"};
        //String rel[] = new String[]{"in", "close to"};
        // int maxLoc = loc.length;
        String strat[] = new String[]{"Fast and Loose", "Hyper-aggressive","Mounted Combat", "Play It Safe", "Slow and Steady", "Run and Gun", "Grenadier's Gamble", "Shorts and Shotties", "Long-Range Overwatch", "Amphibious Assault", "Have Gay Sex", "Breach and Clear", "Chase All Shots", "Hold the High Ground", "Hold the Low Ground", "Hold Down the Fort", "Crates are Key", "Stay on the Roads", "Spread Out", "Keep Friends Close","Make 'em Bleed","Use your Fuckin' Brains, Retards","Mountain Goat"};
        int STRATNUM = strat.length;

        ArrayList<String> help = new ArrayList<String>(10);

        help.add("!ping");
        if (messageText.equals("!ping")) {
            event.getChannel().sendMessage("Pong!").queue();
        }

        help.add("!win");
        if (messageText.equals("!win")) {
            exportWinningDropImage();
            event.getChannel().sendMessage("Winning coordinates have been saved!").queue();
        }

        help.add("!strategy");
        if (messageText.equals("!strategy")) {
            int strategy = rand.nextInt(STRATNUM);
            String message1 = "Optimal Strategy: " + strat[strategy];
            event.getChannel().sendMessage(message1).queue();
        }

        help.add("!drop (e,m,s) OR !");
        if (messageText.contains("!drop")|| messageText.equals("!")) {
            String cmdSplit[] = messageText.split(" ", 2);
            BufferedImage img;
            if (cmdSplit.length != 2) {
                img = getImageFromResource("PUBGMAP1.jpg");
            } else {
                switch (cmdSplit[1]) {
                    case "s":
                        img = getImageFromResource("PUBGMAP1.jpg");
                        break;
                    case "m":
                        img = getImageFromResource("PUBGMAP2.jpg");
                        break;
                    case "e":
                        img = getImageFromResource("PUBGMAP3.jpg");
                        break;
                    default:
                        img = getImageFromResource("PUBGMAP1.jpg");
                        break;
                }
            }
            generateDropPositionImage(img);
            event.getChannel().sendFile(writeOutputFile(img)).queue();
        }

        help.add("!help");
        if (messageText.equals("!help")){
            event.getChannel().sendMessage(help.toString()).queue();
        }

    }

    private BufferedImage getImageFromResource(String image) {
        try {
            return ImageIO.read(this.getClass().getResourceAsStream(image));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void generateDropPositionImage(BufferedImage image) {
        int imgH = image.getHeight();
        int imgW = image.getWidth();

        while (true) {
            int x = rand.nextInt(imgW);
            int y = rand.nextInt(imgH);

            int color = image.getRGB(x, y);
            if (color > -1450000 && color < -1400000) {
                Graphics2D graphics2D = image.createGraphics();
                graphics2D.setFont(new Font("Ariel", Font.PLAIN, 50));
                graphics2D.setColor(Color.RED);
                graphics2D.drawString("x", x, y);
                currentCoordinates.setLocation(x, y);
                break;
            }
        }
    }

    private File writeOutputFile(BufferedImage imageToOutput) {
        File outputFile = new File(tempDir + "PUBGMAPEDIT.jpg");
        try {
            ImageIO.write(imageToOutput, "jpg", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    private void exportWinningDropImage() {
        File winningStart = new File(tempDir + "PUBGMAPEDIT.jpg");
        File export = new File(System.getenv("USERPROFILE") +
                "\\desktop\\PUBGWin-X" + currentCoordinates.getX() + "-Y" + currentCoordinates.getY() + ".jpg");

        winningStart.renameTo(export);
    }
}
