package me.vetustus.server.simplechat.permissions;

import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermissionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("backinv-permissions");
    private static final boolean LUCKPERMS_LOADED;
    private static LuckPerms luckPerms;
    static {
        LUCKPERMS_LOADED = FabricLoader.getInstance().isModLoaded("luckperms");
        if (LUCKPERMS_LOADED) {
            try {
                luckPerms = LuckPermsProvider.get();
                LOGGER.info("LuckPerms найден и подключен");
            } catch (Exception e) {
                LOGGER.error("Не удалось инициализировать LuckPerms", e);
            }
        } else {
            LOGGER.info("LuckPerms не найден, используются vanilla permissions");
        }
    }

    public static boolean hasPermission(ServerCommandSource source, String permission, int defaultLevel) {
        // Для консоли всегда используем vanilla permissions
        if (!source.isExecutedByPlayer()) {
            return source.hasPermissionLevel(defaultLevel);
        }

        // Если LuckPerms доступен и работает, используем его
        if (LUCKPERMS_LOADED && luckPerms != null) {
            try {
                ServerPlayerEntity player = source.getPlayer();
                var user = luckPerms.getUserManager().getUser(player.getUuid());
                if (user != null) {
                    return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
                }
            } catch (Exception e) {
                LOGGER.error("Ошибка при проверке прав через LuckPerms", e);
            }
        }

        // Fallback на vanilla permissions
        return source.hasPermissionLevel(defaultLevel);
    }
}