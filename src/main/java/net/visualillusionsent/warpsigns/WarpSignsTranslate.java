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
 * See the ${licence.type} for more details.
 *
 * You should have received a copy of the ${gpl.type} along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.warpsigns;

import net.visualillusionsent.minecraft.plugin.MessageTranslator;
import net.visualillusionsent.minecraft.plugin.VisualIllusionsPlugin;

/**
 * @author Jason (darkdiplomat)
 */
final class WarpSignsTranslate extends MessageTranslator {

    WarpSignsTranslate(VisualIllusionsPlugin plugin, String defaultLocale, boolean updateLang) {
        super(plugin, defaultLocale, updateLang);
    }
}
