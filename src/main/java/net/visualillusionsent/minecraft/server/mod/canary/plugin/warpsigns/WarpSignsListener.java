/*
 * This file is part of WarpSigns.
 *
 * Copyright © 2012-2013 Visual Illusions Entertainment
 *
 * WarpSigns is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * WarpSigns is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with WarpSigns.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.minecraft.server.mod.canary.plugin.warpsigns;

import java.io.File;
import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.chat.Colors;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.SignChangeHook;
import net.canarymod.logger.Logman;
import net.canarymod.plugin.PluginListener;
import net.canarymod.user.Group;
import net.canarymod.warp.Warp;
import net.visualillusionsent.utils.PropertiesFile;

/**
 * WarpSigns Listener
 * 
 * @author Jason (darkdiplomat)
 */
public final class WarpSignsListener implements PluginListener{
    private final PropertiesFile warpProps;
    private final Logman logman;

    public WarpSignsListener(WarpSigns plugin){
        boolean save = !new File("config/WarpSigns/setings.cfg").exists();
        this.warpProps = new PropertiesFile("config/WarpSigns/settings.cfg");
        warpProps.getBoolean("allow.always", true);
        warpProps.getBoolean("allow.all.create", false);
        warpProps.getBoolean("require.warp.group", true);
        warpProps.getBoolean("log.warps", false);
        if (save) {
            warpProps.save();
        }
        logman = plugin.getLogman();
        Canary.hooks().registerListener(this, plugin);
    }

    @HookHandler
    public final void onBlockRightClick(BlockRightClickHook hook){
        Player player = hook.getPlayer();
        Block block = hook.getBlockClicked();
        if (isSign(block)) {
            Sign sign = (Sign) player.getWorld().getComplexBlock(block);
            if (isWarpSign(sign.getTextOnLine(0))) {
                Warp warptarget = Canary.warps().getWarp(sign.getTextOnLine(1));
                if (warptarget != null) {
                    if (canWarp(warptarget, player) || isWarpAll(sign.getTextOnLine(0))) {
                        player.notice("Warping to ".concat(warptarget.getName()));
                        player.teleportTo(warptarget.getLocation());
                        if (warpProps.getBoolean("log.warps")) {
                            logman.info("Warped " + player.getName() + " to " + warptarget.getName() + " (" + warptarget.getLocation().toString() + ")");
                        }
                    }
                    else {
                        player.notice("You do not have permission to use this Warp...");
                    }
                }
                else {
                    player.notice("Warning: Warp " + sign.getTextOnLine(1) + " does not exist.");
                }
                setSign(sign, warptarget == null);
                hook.setCanceled();
            }
        }
    }

    @HookHandler
    public final void onSignChange(SignChangeHook hook){
        Player player = hook.getPlayer();
        Sign sign = hook.getSign();
        if (sign.getTextOnLine(0).toLowerCase().matches("warp\\:|warp\\-all\\:")) {
            if (sign.getTextOnLine(1).isEmpty()) {
                player.notice("Warning: You did not specify a Warp name.");
                hook.setCanceled();
                return;
            }
            else if (sign.getTextOnLine(0).toLowerCase().matches("warp\\-all\\:") && !player.hasPermission("warpsigns.create.all")) {
                player.notice("Warning: You do not have permission to create Warp-All signs.");
                hook.setCanceled();
                return;
            }

            Warp warptarget = Canary.warps().getWarp(sign.getTextOnLine(1));
            if (warptarget == null) {
                player.notice("Warning: Warp " + sign.getTextOnLine(1) + " does not exist.");
                hook.setCanceled();
                return;
            }
            else if (!canCreate(warptarget, player)) {
                player.notice("Warning: You do not have permission to create a Warp sign for that Warp.");
                hook.setCanceled();
                return;
            }
            setSign(sign, false);
            player.message(Colors.GREEN.concat("Warp sign created!"));
        }
    }

    private final void setSign(Sign sign, boolean bad){
        sign.setTextOnLine((bad ? Colors.RED : Colors.LIGHT_GREEN).concat(sign.getTextOnLine(0).toLowerCase().matches("warp\\-all\\:") ? "Warp-All:" : "Warp:"), 0);
    }

    private final boolean isSign(Block block){
        return block.getType() == BlockType.WallSign || block.getType() == BlockType.SignPost;
    }

    private final boolean isWarpSign(String text){
        return text.length() > 2 && text.substring(2).matches("Warp\\:|Warp\\-All\\:");
    }

    private final boolean isWarpAll(String text){
        return text.substring(2).matches("Warp\\-All\\:");
    }

    private final boolean canWarp(Warp warp, Player player){
        if (warpProps.getBoolean("allow.always")) {
            return true;
        }
        else {
            return testGroups(player, warp.getGroups());
        }
    }

    private final boolean canCreate(Warp warp, Player player){
        if (player.hasPermission("warpsigns.create.all") || warpProps.getBoolean("allow.all.create")) {
            return true;
        }
        else if (player.hasPermission("warpsigns.create")) {
            if (warpProps.getBoolean("require.warp.group")) {
                return testGroups(player, warp.getGroups());
            }
            return true;
        }
        return false;
    }

    private final boolean testGroups(Player player, Group[] groups){
        if (groups == null) {
            return true;
        }
        else if (groups.length == 0) {
            return true;
        }
        for (Group group : groups) {
            if (player.isInGroup(group, true)) {
                return true;
            }
        }
        return false;
    }
}