package com.github.hannahscript.rainbot;

import com.github.hannahscript.rainbot.untracking.TrackingRemover;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RainBot extends ListenerAdapter {
    public static void main(String[] args) throws InterruptedException {
        var token = System.getenv("DISCORD_TOKEN");

        if (token == null) {
            System.err.println("No discord token specified as environment variable: DISCORD_TOKEN");
            System.exit(1);
        }

        JDA jda = JDABuilder.createDefault(token)
                .addEventListeners(new RainBot())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
        
        setUpKillhook(jda);

        jda.awaitReady();
    }
    
    private static void setUpKillhook(JDA jda) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received shutdown hook. Attempting to shut down gracefully ...");
            
            try {
                jda.awaitShutdown();
            } catch (InterruptedException e) {
                System.err.println("Error while shutting down JDA gracefully.");
            }

            System.out.println("Okay, done. Bye!");
            System.exit(0);
        }));
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("API is ready");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Optional<String> untrackedLinkMaybe = TrackingRemover.removeTracking(event.getMessage().getContentRaw());
        if (untrackedLinkMaybe.isPresent() && event.getMessage().isFromGuild()) {
            event.getChannel().deleteMessageById(event.getMessageId())
                    .flatMap(e -> event.getChannel().sendMessage(event.getMessage().getMember().getAsMention() + " posted a tracked link. I have removed the tracking: " + untrackedLinkMaybe.get()))
                    .onErrorFlatMap(e -> event.getChannel().sendMessage(event.getMessage().getMember().getAsMention() + " posted a tracked link. I have removed the tracking: <" + untrackedLinkMaybe.get() + ">"))
                    .queue();
        }
    }
}
