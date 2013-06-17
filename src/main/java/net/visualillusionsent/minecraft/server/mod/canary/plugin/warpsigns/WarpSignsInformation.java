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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.canarymod.Canary;
import net.canarymod.chat.Colors;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.TextFormat;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandListener;
import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.VersionChecker;

/**
 * WarpSigns Information command
 * 
 * @author Jason (darkdiplomat)
 */
public final class WarpSignsInformation implements CommandListener{
    private final List<String> about;
    private final WarpSigns warpsigns;

    public WarpSignsInformation(WarpSigns warpsigns){
        this.warpsigns = warpsigns;
        List<String> pre = new ArrayList<String>();
        pre.add(center(Colors.CYAN + "---" + Colors.LIGHT_GREEN + "WarpSigns " + Colors.PURPLE + "v" + warpsigns.getRawVersion() + Colors.CYAN + " ---"));
        pre.add("$VERSION_CHECK$");
        pre.add(Colors.CYAN + "Build: " + Colors.LIGHT_GREEN + warpsigns.getBuildNumber());
        pre.add(Colors.CYAN + "Built: " + Colors.LIGHT_GREEN + warpsigns.getBuildTime());
        pre.add(Colors.CYAN + "Developer: " + Colors.LIGHT_GREEN + "DarkDiplomat");
        pre.add(Colors.CYAN + "Website: " + Colors.LIGHT_GREEN + "http://wiki.visualillusionsent.net/WarpSigns");
        pre.add(Colors.CYAN + "Issues: " + Colors.LIGHT_GREEN + "https://github.com/Visual-Illusions/WarpSigns/issues");

        // Next 2 lines should always remain at the end of the About
        pre.add(center("§aCopyright © 2012-2013 §2Visual §6I§9l§bl§4u§as§2i§5o§en§7s §2Entertainment"));
        about = Collections.unmodifiableList(pre);
        try {
            Canary.commands().registerCommands(this, warpsigns, false);
        }
        catch (CommandDependencyException ex) {}
    }

    private final String center(String toCenter){
        String strColorless = TextFormat.removeFormatting(toCenter);
        return StringUtils.padCharLeft(toCenter, (int) (Math.floor(63 - strColorless.length()) / 2), ' ');
    }

    @Command(aliases = { "warpsigns" },
            description = "WarpSigns Information Command",
            permissions = { "" },
            toolTip = "/warpsigns")
    public final void infoCommand(MessageReceiver msgrec, String[] args){
        for (String msg : about) {
            if (msg.equals("$VERSION_CHECK$")) {
                VersionChecker vc = warpsigns.getVersionChecker();
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
