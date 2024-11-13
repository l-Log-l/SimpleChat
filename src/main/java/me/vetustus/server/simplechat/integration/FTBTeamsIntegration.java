package me.vetustus.server.simplechat.integration;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class FTBTeamsIntegration {

    public static String getTeam(ServerPlayerEntity player) {
        //Team team = FTBTeamsAPI.api().getManager().getPlayerTeam(player.getUuid());
        //Team team = FTBTeamsAPI.api().getClientManager().getKnownPlayer(player.getUuid()).;
        Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUuid());
        //getType().isPlayer()
        if (team == null || team.get().isPlayerTeam()) {
            return "";
        }

        return team.get().getShortName();
    }

}
