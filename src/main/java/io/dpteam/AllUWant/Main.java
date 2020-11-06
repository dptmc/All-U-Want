package io.dpteam.AllUWant;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkSection;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.Packet53BlockChange; // Needs a Wrapper
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
	private ProtocolManager protocolManager;

	public Main() {
		super();
	}

	public void onEnable() {
		System.out.println("All-U-Want Enabled");
	}

	@Override
	public void onDisable() {
		System.out.println("All-U-Want Disabled");
	}

	public void onLoad() {
		this.getLogger().info("loaded");
		this.protocolManager = ProtocolLibrary.getProtocolManager();
		this.protocolManager.addPacketListener(new PacketAdapter(this, ConnectionSide.CLIENT_SIDE, ListenerPriority.NORMAL, new Integer[]{107}) {
			public void onPacketReceiving(PacketEvent event) {
				if (!event.getPlayer().hasPermission("alluwant.item.creative")) {
					StructureModifier mod = event.getPacket().getItemModifier();
					ItemStack stack = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack)mod.read(0));
					if (stack != null && stack.hasTag()) {
						event.setCancelled(true);
						Main.this.getLogger().info("item id: " + stack.id);
						Main.this.getLogger().info("slot: " + event.getPacket().getIntegers().read(0));
					}
				}

			}
		});
		this.protocolManager.addPacketListener(new PacketAdapter(this, ConnectionSide.CLIENT_SIDE, ListenerPriority.MONITOR, new Integer[]{250}) {
			public void onPacketReceiving(PacketEvent event) {
				if (event.getPacket().getModifier().read(0).equals("AUW|auwping")) {
					ByteArrayOutputStream var3 = new ByteArrayOutputStream();
					DataOutputStream output = new DataOutputStream(var3);

					try {
						output.writeChars("0.2");
						output.writeChars((event.getPlayer().hasPermission("alluwant.item.survival") ? "s" : "") + (event.getPlayer().hasPermission("alluwant.item.creative") ? "c" : "") + (event.getPlayer().hasPermission("alluwant.*") ? "*" : ""));
						PacketContainer packet = Main.this.protocolManager.createPacket(250);

						try {
							Main.this.protocolManager.sendServerPacket(event.getPlayer(), packet);
						} catch (InvocationTargetException var19) {
							var19.printStackTrace();
						}
					} catch (IOException var20) {
						var20.printStackTrace();
					}
				}

				try {
					if (event.getPacketID() == 250) {
						int x;
						int y;
						int z;
						List values;
						DataInputStream input;
						int id;
						if (!event.getPlayer().hasPermission("alluwant.item.survival") && (!event.getPlayer().hasPermission("alluwant.item.creative") || event.getPlayer().getGameMode() != GameMode.CREATIVE) && !event.getPlayer().hasPermission("alluwant.*")) {
							if (event.getPlayer().hasPermission("alluwant.entity.survival") || event.getPlayer().hasPermission("alluwant.entity.creative") && event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().hasPermission("alluwant.*")) {
								if (event.getPacket().getModifier().read(0).equals("AUW|Entity")) {
									values = event.getPacket().getModifier().getValues();
									input = new DataInputStream(new ByteArrayInputStream((byte[])values.get(2)));

									try {
										NBTTagCompound tag = Packet.d(input);
										Entity mentity = EntityTypes.createEntityByName(tag.getString("id"), ((CraftWorld)event.getPlayer().getWorld()).getHandle());
										mentity.e(tag);
										CraftEntity centity = CraftEntity.getEntity((CraftServer)event.getPlayer().getServer(), mentity);
										centity = (CraftEntity)event.getPlayer().getWorld().spawnEntity(new Location(event.getPlayer().getWorld(), (double)tag.getInt("x"), (double)tag.getInt("y"), (double)tag.getInt("z")), centity.getType());
										centity.getHandle().e(tag);
									} catch (Exception var17) {
										Main.this.getLogger().severe(var17.getMessage());
									}
								} else if (event.getPacket().getModifier().read(0).equals("AUW|TileE")) {
									values = event.getPacket().getModifier().getValues();
									input = new DataInputStream(new ByteArrayInputStream((byte[])values.get(2)));

									try {
										id = input.readInt();
										NBTTagCompound tagx = Packet.d(input);
										x = tagx.getInt("x");
										y = tagx.getInt("y");
										z = tagx.getInt("z");
										Block bBlock = event.getPlayer().getWorld().getBlockAt(x, y, z);
										bBlock.setTypeId(id);
										TileEntity entity = ((CraftWorld)event.getPlayer().getWorld()).getTileEntityAt(x, y, z);
										entity.a(tagx);
									} catch (Exception var16) {
										Main.this.getLogger().severe(var16.getMessage());
									}
								}
							}
						} else if (event.getPacket().getModifier().read(0).equals("AUW|mdslot")) {
							values = event.getPacket().getModifier().getValues();

							try {
								DataInputStream inputx = new DataInputStream(new ByteArrayInputStream((byte[])values.get(2)));
								ItemStack handle = Packet.c(inputx);
								CraftItemStack stack = Main.this.stackToStack(handle);
								x = inputx.readInt();
								Main.this.getLogger().info("item id: " + stack.getTypeId());
								Main.this.getLogger().info("slot: " + x);
								event.getPlayer().getOpenInventory().setItem(x, stack);
							} catch (Exception var18) {
								Main.this.getLogger().severe(var18.getMessage());
							}
						}

						if ((event.getPlayer().hasPermission("alluwant.block.survival") || event.getPlayer().hasPermission("alluwant.block.creative") && event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().hasPermission("alluwant.*")) && event.getPacket().getModifier().read(0).equals("AUW|Block")) {
							values = event.getPacket().getModifier().getValues();
							input = new DataInputStream(new ByteArrayInputStream((byte[])values.get(2)));

							try {
								id = input.readInt();
								int meta = input.readInt();
								x = input.readInt();
								y = input.readInt();
								z = input.readInt();
								boolean update = input.readBoolean();
								Block block = event.getPlayer().getWorld().getBlockAt(new Location(event.getPlayer().getWorld(), (double)x, (double)y, (double)z));
								if (!update) {
									block.setTypeId(id);
									block.setData((byte)meta);
								} else {
									World world = ((CraftWorld)event.getPlayer().getWorld()).getHandle();
									if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000 && y >= 0 && y < 256) {
										Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
										ChunkSection var10 = chunk.i()[y >> 4];
										var10.setTypeId(x & 15, y & 15, z & 15, id);
										var10.setData(x & 15, y & 15, z & 15, meta);
										Iterator var15 = world.players.iterator();

										while(var15.hasNext()) {
											Object player = var15.next();
											if (player instanceof EntityPlayer) {
												((EntityPlayer)player).playerConnection.sendPacket(new Packet53BlockChange(x, y, z, world));
											}
										}
									}
								}
							} catch (Exception var21) {
								Main.this.getLogger().severe(var21.getMessage());
							}
						}
					}
				} catch (FieldAccessException var22) {
					Main.this.getLogger().severe(var22.getMessage());
				}

			}
		});
	}

	public CraftItemStack stackToStack(ItemStack stack) {
		CraftItemStack cis = CraftItemStack.asCraftMirror(stack);
		return cis;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return false;
	}
}
