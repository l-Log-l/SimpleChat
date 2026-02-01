package me.vetustus.server.simplechat;

import static me.vetustus.server.simplechat.ChatColor.translateChatColors;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.context.CommandContext;

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
//? if >=1.21.11 {
//?} else {
import eu.pb4.placeholders.api.TextParserUtils;
//?}
import me.drex.vanish.config.ConfigManager;
import me.vetustus.server.simplechat.integration.LuckPermsIntegration;
import me.vetustus.server.simplechat.permissions.PermissionHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
//? if >=1.21.11 {
/*import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
*///?}
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class SimpleChat implements ModInitializer {
    private static ChatConfig config;
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        try {
            loadConfig();
            LOGGER.info("The config is saved!");
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }

        boolean isluckperms = FabricLoader.getInstance().isModLoaded("luckperms");
        boolean isvanish = FabricLoader.getInstance().isModLoaded("melius-vanish");

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            //? if >=1.21.11
            MinecraftServer server = sender.getEntityWorld().getServer();
            //? if >=1.20.6 && <1.21.11
            /*MinecraftServer server = sender.getServerWorld().getServer();*/
            //? if <1.20.6
            /*MinecraftServer server = sender.getServer();*/
            if (server != null && server.isSingleplayer()) {
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
                    //? if >=1.21.11 {
                    /*sender.sendMessage(Text.literal("You cannot chat while vanished."), false);
                    *///?} else
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
                    .replace("%player%", sender.getName().getString())
                    .replace("%lp_group%", isluckperms ? translateChatColors('&', LuckPermsIntegration.getPrimaryGroup(sender)) : "")
                    .replace("%lp_prefix%", isluckperms ? translateChatColors('&', LuckPermsIntegration.getPrefix(sender)) : "")
                    .replace("%lp_suffix%", isluckperms ? translateChatColors('&', LuckPermsIntegration.getSuffix(sender)) : "");
            prepareStringMessage = translateChatColors('&', prepareStringMessage);

            String stringMessage = prepareStringMessage
                    .replace("%message%", originalMessage);

            if (config.isChatColorsEnabled())
                stringMessage = translateChatColors('&', stringMessage);

            //? if >=1.21.11 {
            /*Text resultMessage = Placeholders.parseText(Text.literal(stringMessage), PlaceholderContext.of(sender));
            *///?} else
            Text resultMessage = Placeholders.parseText(TextParserUtils.formatText(stringMessage), PlaceholderContext.of(sender));

            if (!config.getSuggestsWhenYouClickOnThePlayerName().isBlank()) {
                String stringSuggest = translateChatColors('&', config.getSuggestsWhenYouClickOnThePlayerName()).replace("%player%", sender.getName().getString());
                //? if >=1.21.11 {
                /*Text placeholder_suggest = Placeholders.parseText(Text.literal(stringSuggest), PlaceholderContext.of(sender));
                resultMessage = resultMessage.copy().styled(style -> style.withClickEvent(new ClickEvent.SuggestCommand(placeholder_suggest.getString())));
                *///?} else {
                Text placeholder_suggest = Placeholders.parseText(TextParserUtils.formatText(stringSuggest), PlaceholderContext.of(sender));
                resultMessage = resultMessage.copy().styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, placeholder_suggest.getString())));
                //?}
            }
            int isPlayerLocalFound = 0;

            List<ServerPlayerEntity> players = Objects.requireNonNull(server, "The server cannot be null.")
                    .getPlayerManager().getPlayerList();

            for (ServerPlayerEntity p : players) {

                if (config.isGlobalChatEnabled()) {
                    if (isGlobalMessage) {
                        p.sendMessage(resultMessage, false);

                    } else if (isWorldMessage && config.isWorldChatEnabled()) {
                        if (p.getEntityWorld().getRegistryKey().getValue().equals(sender.getEntityWorld().getRegistryKey().getValue())) {
                            p.sendMessage(resultMessage, false);

                        }
                    } else if (p.squaredDistanceTo(sender) <= config.getChatRange() || p.getUuid().equals(sender.getUuid())) {
                        p.sendMessage(resultMessage, false);

                        // Only increment counter if player can see vanished players or sender isn't vanished
                        if (isvanish) {
                            if (VanishAPI.canSeePlayer(p, sender)) {
                                isPlayerLocalFound++;
                            }
                        } else {
                            isPlayerLocalFound++;
                        }
                    }
                } else if (config.isWorldChatEnabled()) {
                    if (isWorldMessage) {
                        if (p.getEntityWorld().getRegistryKey().getValue().equals(sender.getEntityWorld().getRegistryKey().getValue())) {
                            p.sendMessage(resultMessage, false);

                        }
                    } else if (p.squaredDistanceTo(sender) <= config.getChatRange() && p.getEntityWorld().getRegistryKey().getValue().equals(sender.getEntityWorld().getRegistryKey().getValue())) {
                        p.sendMessage(resultMessage, false);
                        // Only increment counter if player can see vanished players or sender isn't vanished
                        if (isvanish) {
                            if (VanishAPI.canSeePlayer(p, sender)) {
                                isPlayerLocalFound++;
                            }
                        } else {
                            isPlayerLocalFound++;
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
                        } else {
                            isPlayerLocalFound++;
                        }
                    }
                }
            }

            if (isPlayerLocalFound <= 1 && !isGlobalMessage && !isWorldMessage && !isSenderVanished) {
                String noPlayerNearbyText = config.getNoPlayerNearbyText();
                if (!noPlayerNearbyText.isBlank()) {
                    Text noPlayerNearbyTextResult = Text.literal(translateChatColors('&', noPlayerNearbyText));

                    sender.sendMessage(noPlayerNearbyTextResult, config.noPlayerNearbyActionBar());
                }
            }

            LOGGER.info(stringMessage);
            // Call CHAT_MESSAGE event for compatibility with BlockBot and other mods
            ServerMessageEvents.CHAT_MESSAGE.invoker().onChatMessage(message, sender, params);
            return false;
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("simplechat").executes(context -> {

                    //? if >=1.21.11 {
                    /*if (context.getSource().getPermissions().hasPermission(new Permission.Level(PermissionLevel.fromLevel(1)))) {
                    *///?} else
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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("simplechat")
                    .requires(source -> PermissionHandler.hasPermission(source, "simplechat.use", 2))
                    .then(literal("clear")
                            .requires(source -> PermissionHandler.hasPermission(source, "simplechat.clear", 2))
                            .executes(this::spamMessages)));
        });
    }
    private int spamMessages(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        if (server == null) {
            context.getSource().sendError(Text.literal("Failed to get server instance!"));
            return 0;
        }
        Text emptyMessage = Text.literal("");
        List<ServerPlayerEntity> players = Objects.requireNonNull(server, "The server cannot be null.")
                .getPlayerManager().getPlayerList();
        for (ServerPlayerEntity p : players) {
            for (int i = 0; i < 44; i++) {
                p.sendMessage(emptyMessage, false);
            }
        }
        return 1;
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
        try (FileReader reader = new FileReader(ChatConfig.CONFIG_PATH)) {
            config = new Gson().fromJson(reader, ChatConfig.class);
        } catch (FileNotFoundException e) {
            config = new ChatConfig();
            LOGGER.error(e.toString());
        }
    }
}