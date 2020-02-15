package de.summerfeeling.labyemoteplayer.npc;

import de.summerfeeling.labyemoteplayer.I18n;
import de.summerfeeling.labyemoteplayer.LabyEmotePlayer;
import de.summerfeeling.labyemoteplayer.utils.reflect.FieldAccessor;
import de.summerfeeling.labyemoteplayer.utils.reflect.Reflect;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity.EnumEntityUseAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class NPCInteractListener extends MessageToMessageDecoder<Packet> {
	
	private static final FieldAccessor ID_FIELD = Reflect.getField(PacketPlayInUseEntity.class, "a");
	
	private NPCManager npcManager;
	private Player player;
	
	public NPCInteractListener(NPCManager npcManager, Player player) {
		this.npcManager = npcManager;
		this.player = player;
	}
	
	@Override
	protected void decode(ChannelHandlerContext context, Packet income, List<Object> out) {
		if (income instanceof PacketPlayInUseEntity) {
			PacketPlayInUseEntity packet = (PacketPlayInUseEntity) income;

			if (packet.a() == EnumEntityUseAction.INTERACT) {
				int entityId = ID_FIELD.get(packet);

				if (npcManager.isEmotePlayer(entityId)) {
					if (!player.hasPermission("labyemoteplayer.use")) {
						String translation = I18n.t("EMOTE_PLAYER_NO_PERMISSION", LabyEmotePlayer.PREFIX);
						if (!translation.isEmpty()) player.sendMessage(translation);
					} else {
						Inventory inventory = Bukkit.createInventory(null, 9, "§9Emotes");
						player.openInventory(inventory);
//						new EmoteSelectorInventory(player, npcManager.getEmotePlayer(entityId), 0).open(player);
					}
				}
			} else if (packet.a() == EnumEntityUseAction.ATTACK) {
				int entityId = ID_FIELD.get(packet);

				if (npcManager.isEmotePlayer(entityId)) {
					LabyEmotePlayer.playEmote(npcManager.getEmotePlayer(entityId), -1, player);
				}
			}
		}
		
		out.add(income);
	}
}
