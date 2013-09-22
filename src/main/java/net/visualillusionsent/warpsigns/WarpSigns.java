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

/**
 * WarpSigns main plugin class
 *
 * @author Jason (darkdiplomat)
 */
public final class WarpSigns extends VIPlugin {

    @Override
    public final boolean enable() {
        checkStatus();
        checkVersion();
        new WarpSignsListener(this);
        new VIPluginInformation(this);
        return true;
    }

    @Override
    public final void disable() {
        // no logic needed
    }
}
