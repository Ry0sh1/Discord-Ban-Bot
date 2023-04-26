import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;

public class BanBot {
    public static BanBot INSTANCE;
    String token = "";                      //Insert your token here

    public static void main (String[] args){

        try {

            new BanBot();

        } catch (LoginException e) {

            throw new RuntimeException(e);

        }

    }

    public  BanBot() throws LoginException {

        INSTANCE = this;

        JDABuilder builder = JDABuilder.createDefault(token);

        builder.setActivity(Activity.playing("In Bearbeitung"));
        builder.setStatus(OnlineStatus.ONLINE);

        builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);

        builder.addEventListeners(new BanMember());

        JDA banbot = builder.build();

    }

}
