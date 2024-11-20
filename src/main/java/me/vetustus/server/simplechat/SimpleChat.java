package me.vetustus.server.simplechat;

import static me.vetustus.server.simplechat.ChatColor.translateChatColors;
import me.drex.vanish.api.VanishAPI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import com.google.gson.Gson;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import me.drex.vanish.config.ConfigManager;
import me.vetustus.server.simplechat.integration.FTBTeamsIntegration;
import me.vetustus.server.simplechat.integration.LuckPermsIntegration;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class SimpleChat implements ModInitializer {
    private ChatConfig config;
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        try {
            loadConfig();
            LOGGER.info("The config is saved!");
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }

        boolean isftbteams = FabricLoader.getInstance().isModLoaded("ftbteams");
        boolean isluckperms = FabricLoader.getInstance().isModLoaded("luckperms");
        boolean isvanish = FabricLoader.getInstance().isModLoaded("melius-vanish");

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            if (sender.getServer() != null && sender.getServer().isSingleplayer()) {
                LOGGER.error("Single mode detected, the mod will be disabled.");
                return true;
            }
            String originalMessage = message.getContent().getString();

            if (!config.isChatModEnabled())
                return true;
            boolean isSenderVanished = false;
            boolean isGlobalMessage = false;
            boolean isWorldMessage = false;
            if (isvanish) {
                isSenderVanished = VanishAPI.isVanished(sender);
                if (isSenderVanished && ConfigManager.vanish().disableChat) {
                    sender.sendMessage(TextParserUtils.formatText("You cannot chat while vanished."), false);
                    return false;
                }
            }
            String chatFormat = config.getLocalChatFormat();
            if (config.isGlobalChatEnabled()) {
                if (originalMessage.startsWith("!")) {
                    isGlobalMessage = true;
                    chatFormat = config.getGlobalChatFormat();
                    originalMessage = originalMessage.substring(1);
                }
            }
            if (config.isWorldChatEnabled()) {
                if (originalMessage.startsWith("#")) {
                    isWorldMessage = true;
                    chatFormat = config.getWorldChatFormat();
                    originalMessage = originalMessage.substring(1);
                }
            }
            String prepareStringMessage = chatFormat
                    .replaceAll("%player%", sender.getName().getString())
                    .replaceAll("%ftbteam%", isftbteams ? FTBTeamsIntegration.getTeam(sender) : "")
                    .replaceAll("%lp_group%", isluckperms ? translateChatColors('&', LuckPermsIntegration.getPrimaryGroup(sender)) : "")
                    .replaceAll("%lp_prefix%", isluckperms ? translateChatColors('&', LuckPermsIntegration.getPrefix(sender)) : "")
                    .replaceAll("%lp_suffix%", isluckperms ? translateChatColors('&', LuckPermsIntegration.getSuffix(sender)) : "");
            prepareStringMessage = translateChatColors('&', prepareStringMessage);

            String stringMessage = prepareStringMessage
                    .replaceAll("%message%", originalMessage);

            if (config.isChatColorsEnabled())
                stringMessage = translateChatColors('&', stringMessage);

            Text resultMessage = Placeholders.parseText(TextParserUtils.formatText(stringMessage), PlaceholderContext.of(sender));

            int isPlayerLocalFound = 0;

            List<ServerPlayerEntity> players = Objects.requireNonNull(sender.getServer(), "The server cannot be null.")
                    .getPlayerManager().getPlayerList();




            for (ServerPlayerEntity p : players) {


                if (config.isGlobalChatEnabled()) {
                    if (isGlobalMessage) {
                        p.sendMessage(resultMessage, false);

                    } else if (isWorldMessage && config.isWorldChatEnabled()) {
                        if (p.getEntityWorld().getRegistryKey().getValue() == sender.getEntityWorld().getRegistryKey().getValue()) {
                            p.sendMessage(resultMessage, false);

                        }
                    } else if (p.squaredDistanceTo(sender) <= config.getChatRange() || p.getUuid() == sender.getUuid()) {
                        p.sendMessage(resultMessage, false);

                        // Only increment counter if player can see vanished players or sender isn't vanished
                        if (isvanish) {
                            if (VanishAPI.canSeePlayer(p, sender)) {
                                isPlayerLocalFound++;
                            }
                        }
                    }
                } else if (config.isWorldChatEnabled()) {
                    if (isWorldMessage) {
                        if (p.getEntityWorld().getRegistryKey().getValue() == sender.getEntityWorld().getRegistryKey().getValue()) {
                            p.sendMessage(resultMessage, false);

                        }
                    } else if (p.squaredDistanceTo(sender) <= config.getChatRange() && p.getEntityWorld().getRegistryKey().getValue() == sender.getEntityWorld().getRegistryKey().getValue()) {
                        p.sendMessage(resultMessage, false);
                        // Only increment counter if player can see vanished players or sender isn't vanished
                        if (isvanish) {
                            if (VanishAPI.canSeePlayer(p, sender)) {
                                isPlayerLocalFound++;
                            }
                        }
                    }
                } else {
                    p.sendMessage(resultMessage, false);
                    if (p.squaredDistanceTo(sender) <= config.getChatRange()) {
                        // Only increment counter if player can see vanished players or sender isn't vanished
                        if (isvanish) {
                            if (VanishAPI.canSeePlayer(p, sender)) {

                                isPlayerLocalFound++;
                            }
                        }
                    }
                }
            }

            if (isPlayerLocalFound <= 1 && !isGlobalMessage && !isWorldMessage && !isSenderVanished) {
                String noPlayerNearbyText = config.getNoPlayerNearbyText();
                Text noPlayerNearbyTextResult = Text.literal(translateChatColors('&', noPlayerNearbyText));
                sender.sendMessage(noPlayerNearbyTextResult, config.noPlayerNearbyActionBar());
            }

            LOGGER.info(stringMessage);
            return false;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("simplechat").executes(context -> {
                    if (context.getSource().hasPermissionLevel(1)) {
                        try {
                            loadConfig();
                            context.getSource().sendMessage(Text.literal("Settings are reloaded!"));
                        } catch (IOException e) {
                            context.getSource().sendMessage(Text.literal("An error occurred while reloading the settings (see the console)!"));
                            LOGGER.error(e.toString());
                        }
                    } else {
                        context.getSource().sendError(Text.literal("You don't have the right to do this! If you think this is an error, contact your server administrator."));
                    }
                    return 1;
                })));
    }

    private void loadConfig() throws IOException {
        File configFile = new File(ChatConfig.CONFIG_PATH);
        File configFolder = new File("config/");
        if (!configFolder.exists())
            configFolder.mkdirs();
        if (!configFile.exists()) {
            Files.copy(Objects.requireNonNull(
                    this.getClass().getClassLoader().getResourceAsStream("simplechat.json"),
                    "Couldn't find the configuration file in the JAR"), configFile.toPath());
        }
        try {
            config = new Gson().fromJson(new FileReader(ChatConfig.CONFIG_PATH), ChatConfig.class);
        } catch (FileNotFoundException e) {
            config = new ChatConfig();
            LOGGER.error(e.toString());
        }
    }
}