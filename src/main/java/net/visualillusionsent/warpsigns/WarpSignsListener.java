/*
 * This file is part of WarpSigns.
 *
 * Copyright © 2012-2014 Visual Illusions Entertainment
 *
 * WarpSigns is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.warpsigns;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.SignChangeHook;
import net.canarymod.logger.Logman;
import net.canarymod.plugin.PluginListener;
import net.canarymod.user.Group;
import net.canarymod.warp.Warp;
import net.visualillusionsent.minecraft.plugin.ChatFormat;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;
import net.visualillusionsent.utils.PropertiesFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WarpSigns Listener
 *
 * @author Jason (darkdiplomat)
 */
public final class WarpSignsListener extends VisualIllusionsCanaryPluginInformationCommand implements PluginListener {

    private final PropertiesFile warpProps;
    private final WarpSignsTranslate trans;
    private final Logman logman;
    private final Matcher warpOrWarpAllIgnoreCase = Pattern.compile("(?i)warp(\\-all)?:").matcher(""),
            warpAllIgnoreCase = Pattern.compile("(?i)warp\\-all:").matcher(""),
            warpOrWarpAll = Pattern.compile("Warp(\\-All)?:").matcher("");


    public WarpSignsListener(WarpSigns plugin) throws CommandDependencyException {
        super(plugin);
        this.warpProps = new PropertiesFile("config/WarpSigns/settings.cfg");
        this.warpProps.getBoolean("allow.always", true);
        this.warpProps.getBoolean("allow.all.create", false);
        this.warpProps.getBoolean("require.warp.group", true);
        this.warpProps.getBoolean("log.warps", false);
        this.warpProps.getBoolean("allow.world.load", true);
        this.warpProps.getString("server.locale", "en_US");
        this.warpProps.getBoolean("update.lang", true);
        this.warpProps.save();
        this.logman = plugin.getLogman();
        this.trans = new WarpSignsTranslate(plugin, warpProps.getString("server.locale"), warpProps.getBoolean("update.lang"));
        plugin.registerCommands(this, false);
        plugin.registerListener(this);
    }

    @Command(
            aliases = { "warpsigns" },
            description = "Displays plugin information or reloads configuration",
            permissions = { "" },
            toolTip = "/warpsigns [reload]"
    )
    public final void warpSignsCommand(MessageReceiver msgrec, String[] args) {
        if (args.length > 1 && args[1].equals("reload")) {
            try {
                warpProps.reload();
                msgrec.notice(trans.localeTranslate("cfg.reload.success", localeFor(msgrec)));
            }
            catch (Exception ex) {
                msgrec.notice(trans.localeTranslate("cfg.reload.fail", localeFor(msgrec)));
            }
        }
        else {
            super.sendInformation(msgrec);
        }
    }

    @HookHandler
    public final void onBlockRightClick(BlockRightClickHook hook) {
        Player player = hook.getPlayer();
        Block block = hook.getBlockClicked();
        if (isSign(block)) {
            Sign sign = (Sign) hook.getBlockClicked().getTileEntity();
            if (isWarpSign(sign.getTextOnLine(0))) {
                Warp warptarget = Canary.warps().getWarp(getWarpName(sign));
                if (warptarget != null) {
                    if (isWarpAll(sign.getTextOnLine(0)) || canWarp(warptarget, player)) {
                        if (warpProps.getBoolean("allow.world.load") && !Canary.getServer().getWorldManager().worldIsLoaded(warptarget.getLocation().getWorld().getFqName())) {
                            Canary.getServer().getWorldManager().getWorld(warptarget.getLocation().getWorldName(), true);
                        }
                        if (Canary.getServer().getWorldManager().worldIsLoaded(warptarget.getLocation().getWorld().getFqName())) {
                            player.notice(trans.localeTranslate("warp.to", player.getLocale(), warptarget.getName()));
                            player.teleportTo(warptarget.getLocation());
                            if (warpProps.getBoolean("log.warps")) {
                                logman.info("Warped " + player.getName() + " to " + warptarget.getName() + " (" + warptarget.getLocation().toString() + ")");
                            }
                        }
                        else {
                            player.notice(trans.localeTranslate("world.not.loaded", player.getLocale(), warptarget.getName()));
                        }
                    }
                    else {
                        player.notice(trans.localeTranslate("use.denied", player.getLocale(), warptarget.getName()));
                    }
                }
                else {
                    player.notice(trans.localeTranslate("bad.warp", player.getLocale(), getWarpName(sign)));
                }
                updateSign(sign, warptarget == null);
                hook.setCanceled();
            }
        }
    }

    @HookHandler
    public final void onSignChange(SignChangeHook hook) {
        Player player = hook.getPlayer();
        Sign sign = hook.getSign();
        if (warpOrWarpAllIgnoreCase.reset(sign.getTextOnLine(0)).matches()) {
            if (getWarpName(sign).isEmpty()) {
                player.notice(trans.localeTranslate("bad.warp.name", player.getLocale()));
                sign.getBlock().dropBlockAsItem(true);
                hook.setCanceled();
                return;
            }
            else if (warpAllIgnoreCase.reset(sign.getTextOnLine(0)).matches() && !player.hasPermission("warpsigns.create.all")) {
                player.notice(trans.localeTranslate("create.denied.warpall", player.getLocale()));
                sign.getBlock().dropBlockAsItem(true);
                hook.setCanceled();
                return;
            }

            Warp warptarget = Canary.warps().getWarp(getWarpName(sign));
            if (warptarget == null) {
                player.notice(trans.localeTranslate("bad.warp", player.getLocale(), getWarpName(sign)));
                sign.getBlock().dropBlockAsItem(true);
                hook.setCanceled();
                return;
            }
            else if (!canCreate(warptarget, player)) {
                player.notice(trans.localeTranslate("create.denied", player.getLocale()));
                sign.getBlock().dropBlockAsItem(true);
                hook.setCanceled();
                return;
            }
            updateSign(sign, false);
            player.message(ChatFormat.GREEN.concat(trans.localeTranslate("create.success", player.getLocale())));
        }
    }

    private void updateSign(Sign sign, boolean bad) {
        sign.setTextOnLine((bad ? ChatFormat.RED : ChatFormat.LIGHT_GREEN).concat(sign.getTextOnLine(0).toLowerCase().contains("warp-all:") ? "Warp-All:" : "Warp:"), 0);
        sign.update();
    }

    private boolean isSign(Block block) {
        return block.getType() == BlockType.WallSign || block.getType() == BlockType.SignPost;
    }

    private boolean isWarpSign(String text) {
        return text.length() > 2 && warpOrWarpAll.reset(text.substring(2)).matches();
    }

    private boolean isWarpAll(String text) {
        return text.length() > 2 && text.substring(2).equals("Warp-All:");
    }

    private boolean canWarp(Warp warp, Player player) {
        return warpProps.getBoolean("allow.always") || testGroups(player, warp.getGroups());
    }

    private boolean canCreate(Warp warp, Player player) {
        if (player.hasPermission("warpsigns.create.all") || warpProps.getBoolean("allow.all.create")) {
            return true;
        }
        else if (player.hasPermission("warpsigns.create")) {
            return !warpProps.getBoolean("require.warp.group") || testGroups(player, warp.getGroups());
        }
        return false;
    }

    private String getWarpName(Sign sign) {
        return sign.getTextOnLine(1) + sign.getTextOnLine(2) + sign.getTextOnLine(3);
    }

    private boolean testGroups(Player player, Group[] groups) {
        if (groups == null || groups.length == 0) {
            return true;
        }
        for (Group group : groups) {
            if (player.isInGroup(group, true)) {
                return true;
            }
        }
        return false;
    }

    private String localeFor(MessageReceiver msgrec) {
        if (msgrec instanceof Player) {
            return ((Player) msgrec).getLocale();
        }
        return warpProps.getString("server.locale");
    }
}
