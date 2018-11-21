import Pubg.Api.Client.PubgApiClient;

public class Main {

    //Runs discord bot object
    public static void main(String[] args) throws Exception {
        //new DiscordBotMessageHandler();

        PubgApiClient client = new PubgApiClient();
        //client.getPlayerIdFromName("CuteBabyVagina");

        System.out.println(client.getLatestSeasonId());

    }
}
