import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BanMember extends ListenerAdapter {

    String modRole = "";            //Put your mod role here
    int mods;
    private final Map<String, Integer> yesClicks = new HashMap<>();
    private final Map<String, Integer> noClicks = new HashMap<>();
    Member effectedMember;
    User effectedUser;

    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getMessage().getContentStripped().startsWith("!banvote")) {

            if (!event.getMessage().getMentions().getMembers().get(0).equals(effectedMember)  && effectedMember != null){

                event.getChannel().sendMessage("Es ist bereits ein Vote am Laufen bitte versuch es nach dem der vorherige abgelaufen ist erneut!").queue();

            }else {

                if (event.getMember().getRoles().contains(event.getGuild().getRoleById(modRole))){

                    mods = event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(modRole)).size() + 3;

                    effectedMember = event.getMessage().getMentions().getMembers().get(0);
                    effectedUser = effectedMember.getUser();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Bann Vote!");
                    embed.setDescription("Du kannst hier für ja voten, wenn du möchtest, dass diese Person " + effectedMember.getAsMention() + " von meinem Server gebannt werden soll.\n" +
                            "Du kannst nur voten, wenn du die Rolle Chef oder höher besitzt.\n" +
                            "Dieser Vote schließt nach 2 Minuten!");
                    embed.setThumbnail(effectedMember.getUser().getAvatarUrl());

                    event.getChannel().sendMessageEmbeds(embed.build()).addActionRow(Button.success("yesButton", "Ja"), Button.danger("noButton", "Nein")).queue(message -> {
                        message.delete().queueAfter(2, TimeUnit.MINUTES);
                        scheduleClearTask();});



                } else {

                    event.getChannel().sendMessage("Du hast nicht die Berechtigung für diesen Befehl!").queue();

                }

            }

        }

    }

    private void scheduleClearTask() {

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.schedule(() -> {

            yesClicks.clear();
            noClicks.clear();
            effectedMember = null;
            effectedUser = null;

        }, 2, TimeUnit.MINUTES);

        scheduler.shutdown();

    }

    public void onButtonInteraction(ButtonInteractionEvent event) {

        ButtonInteraction buttonInteraction = event.getInteraction();
        String buttonId = buttonInteraction.getComponentId();
        Member member = buttonInteraction.getMember();

        if (event.getMember().getRoles().contains(event.getGuild().getRoleById(modRole))){

            if (buttonId.equals("yesButton")) {

                if (noClicks.containsKey(member.getId()) || yesClicks.containsKey(member.getId())) {

                    buttonInteraction.reply("Du hast bereits gevotet!").setEphemeral(true).queue();

                } else {

                    yesClicks.put(member.getId(), 1);
                    buttonInteraction.reply("Danke für deinen Vote!").setEphemeral(true).queue();
                    event.getChannel().sendMessageEmbeds(createVoteEmbed().build()).queue();

                }

            } else if (buttonId.equals("noButton")) {

                if (noClicks.containsKey(member.getId()) || yesClicks.containsKey(member.getId())) {

                    buttonInteraction.reply("Du hast bereits gevotet!").setEphemeral(true).queue();

                } else {

                    noClicks.put(member.getId(), 1);
                    buttonInteraction.reply("Danke für deinen Vote!").setEphemeral(true).queue();
                    event.getChannel().sendMessageEmbeds(createVoteEmbed().build()).queue();

                }

            }

            if (yesClicks.size() > mods / 2){

                event.getGuild().ban(effectedUser, 7).queue();
                event.getChannel().sendMessage(effectedMember.getAsMention() + " wurde vom Server gebannt").queue();
                yesClicks.clear();
                noClicks.clear();

            } else if (noClicks.size() > mods / 2){

                event.getChannel().sendMessage(effectedMember.getAsMention() + " darf weiterhin auf dem Server bleiben").queue();
                noClicks.clear();
                yesClicks.clear();

            }

        } else {

            buttonInteraction.reply("Du hast nicht die Berechtigung zu voten!").setEphemeral(true).queue();

        }

    }

    private EmbedBuilder createVoteEmbed() {

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Bann Vote Ergebnisse");
        embed.setDescription("Aktuelle Votes:");
        embed.addField("Ja", String.valueOf(yesClicks.size()), true);
        embed.addField("Nein", String.valueOf(noClicks.size()), true);
        return embed;

    }

}