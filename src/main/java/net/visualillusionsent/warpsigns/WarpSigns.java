/*
 * This file is part of WarpSigns.
 *
 * Copyright © 2012-2014 Visual Illusions Entertainment
 *
 * WarpSigns is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License v3 for more details.
 *
 * You should have received a copy of the GNU General Public License v3 along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.warpsigns;

import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPlugin;

/**
 * WarpSigns main plugin class
 *
 * @author Jason (darkdiplomat)
 */
public final class WarpSigns extends VisualIllusionsCanaryPlugin {

    @Override
    public final boolean enable() {
        try {
            super.enable();
            new WarpSignsListener(this);
        }
        catch (Exception ex) {
            getLogman().error("WarpSigns failed to enable...", ex);
            return false;
        }
        return true;
    }

    @Override
    public final void disable() {
        // no logic needed
    }
}
