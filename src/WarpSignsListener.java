/**
 * WarpSigns
 * <p>
 * Copyright (C) 2012 - 2013 Visual Illusions Entertainment.
 * <p>
 * This program is free software: you can redistribute it and/or modify it<br/>
 * under the terms of the GNU General Public License as published by the Free Software Foundation,<br/>
 * either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;<br/>
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.<br/>
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.<br/>
 * If not, see http://www.gnu.org/licenses/gpl.html
 * 
 * @version 2.0.0
 * @author Jason (darkdiplomat)
 */
public final class WarpSignsListener extends PluginListener{
    private final PropertiesFile warpProps;

    public WarpSignsListener(WarpSigns plugin){
        this.warpProps = plugin.getPropertiesFile();
        warpProps.getBoolean("allow.always", true);
        warpProps.getBoolean("allow.all.create", false);
        warpProps.getBoolean("require.warp.group", true);
        PluginLoader loader = etc.getLoader();
        loader.addListener(PluginLoader.Hook.BLOCK_RIGHTCLICKED, this, plugin, Priority.MEDIUM);
        loader.addListener(PluginLoader.Hook.SIGN_CHANGE, this, plugin, Priority.MEDIUM);
    }

    @Override
    public final boolean onBlockRightClick(Player player, Block block, Item item){
        if (isSign(block)) {
            Sign sign = (Sign) player.getWorld().getComplexBlock(block);
            if (isWarpSign(sign.getText(0))) {
                Warp warptarget = etc.getDataSource().getWarp(sign.getText(1));
                if (warptarget != null) {
                    if (canWarp(warptarget, player) || isWarpAll(sign.getText(0))) {
                        player.notify("Warping to ".concat(warptarget.Name));
                        player.teleportTo(warptarget.Location);
                    }
                    else {
                        player.notify("You do not have permission to use this Warp...");
                    }
                }
                else {
                    player.notify("Warning: Warp " + sign.getText(1) + " does not exist.");
                }
                setSign(sign, warptarget == null);
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean onSignChange(Player player, Sign sign){
        if (sign.getText(0).toLowerCase().matches("warp\\:|warp\\-all\\:")) {
            if (sign.getText(1).isEmpty()) {
                player.notify("Warning: You did not specify a Warp name.");
                return true;
            }
            else if (sign.getText(0).toLowerCase().matches("warp\\-all\\:") && !player.canUseCommand("warpsigns.create.all")) {
                player.notify("Warning: You do not have permission to create Warp-All signs.");
                return true;
            }

            Warp warptarget = etc.getDataSource().getWarp(sign.getText(1));
            if (warptarget == null) {
                player.notify("Warning: Warp " + sign.getText(1) + " does not exist.");
                return true;
            }
            else if (!canCreate(warptarget, player)) {
                player.notify("Warning: You do not have permission to create a Warp sign for that Warp.");
                return true;
            }
            setSign(sign, false);
            player.sendMessage(Colors.Green.concat("Warp sign created!"));
        }
        return false;
    }

    private final void setSign(Sign sign, boolean bad){
        sign.setText(0, (bad ? Colors.Red : Colors.LightGreen).concat(sign.getText(0).toLowerCase().matches("warp\\-all\\:") ? "Warp-All:" : "Warp:"));
    }

    private final boolean isSign(Block block){
        return block.blockType == Block.Type.WallSign || block.blockType == Block.Type.SignPost;
    }

    private final boolean isWarpSign(String text){
        return text.length() > 2 && text.substring(2).matches("Warp\\:|Warp\\-All\\:");
    }

    private final boolean isWarpAll(String text){
        return text.substring(2).matches("Warp\\-All\\:");
    }

    private final boolean canWarp(Warp warp, Player player){
        if (warpProps.getBoolean("allow.always") || warp.Group == null || warp.Group.isEmpty()) {
            return true;
        }
        else {
            return player.isInGroup(warp.Group);
        }
    }

    private final boolean canCreate(Warp warp, Player player){
        if (player.canUseCommand("warpsigns.create.all") || warpProps.getBoolean("allow.all.create")) {
            return true;
        }
        else if (player.canUseCommand("warpsigns.create")) {
            if (warpProps.getBoolean("require.warp.group")) {
                return warp.Group == null || warp.Group.isEmpty() || player.isInGroup(warp.Group);
            }
            return true;
        }
        return false;
    }
}