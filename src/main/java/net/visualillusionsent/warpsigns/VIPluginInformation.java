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
package net.visualillusionsent.warpsigns;

import net.canarymod.Canary;
import net.canarymod.chat.Colors;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.TextFormat;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandListener;
import net.canarymod.plugin.Plugin;
import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.VersionChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Visual Illusions Plugin Information command
 *
 * @author Jason (darkdiplomat)
 */
public final class VIPluginInformation implements CommandListener {
    private final List<String> about;
    private final VIPlugin plugin;

    public VIPluginInformation(VIPlugin plugin) {
        this.plugin = plugin;
        List<String> pre = new ArrayList<String>();
        pre.add(center(Colors.CYAN + "---" + Colors.LIGHT_GREEN + plugin.getName() + " " + Colors.ORANGE + "v" + plugin.getVersion() + Colors.CYAN + " ---"));
        pre.add("$VERSION_CHECK$");
        pre.add(Colors.CYAN + "Jenkins Build: " + Colors.LIGHT_GREEN + plugin.getBuild());
        pre.add(Colors.CYAN + "Built On: " + Colors.LIGHT_GREEN + plugin.getBuildTime());
        pre.add(Colors.CYAN + "Developer(s): " + Colors.LIGHT_GREEN + plugin.getDevelopers());
        pre.add(Colors.CYAN + "Website: " + Colors.LIGHT_GREEN + plugin.getWikiURL());
        pre.add(Colors.CYAN + "Issues: " + Colors.LIGHT_GREEN + plugin.getIssuesURL());

        // Next line should always remain at the end of the About
        pre.add(center("§BCopyright © 2012-2013 §AVisual §6I§9l§Bl§4u§As§2i§5o§En§7s §6Entertainment"));
        about = Collections.unmodifiableList(pre);
        try {
            Canary.commands().registerCommands(this, plugin, false);
        }
        catch (CommandDependencyException ex) {
        }
    }

    private final String center(String toCenter) {
        String strColorless = TextFormat.removeFormatting(toCenter);
        return StringUtils.padCharLeft(toCenter, (int)(Math.floor(63 - strColorless.length()) / 2), ' ');
    }

    @Command(aliases = {"warpsigns"},
            description = "Displays plugin information",
            permissions = {""},
            toolTip = "WarpSigns Information Command")
    public final void infoCommand(MessageReceiver msgrec, String[] args) {
        for (String msg : about) {
            if (msg.equals("$VERSION_CHECK$")) {
                VersionChecker vc = plugin.getVersionChecker();
                Boolean islatest = vc.isLatest();
                if (islatest == null) {
                    msgrec.message(center(Colors.GRAY + "VersionCheckerError: " + vc.getErrorMessage()));
                }
                else if (!vc.isLatest()) {
                    msgrec.message(center(Colors.GRAY + vc.getUpdateAvailibleMessage()));
                }
                else {
                    msgrec.message(center(Colors.LIGHT_GREEN + "Latest Version Installed"));
                }
            }
            else {
                msgrec.message(msg);
            }
        }
    }
}
