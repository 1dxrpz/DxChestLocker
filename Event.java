package com.dxrpz.dxchestlocker;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Event implements Listener, InventoryHolder {

    private DxChestLocker plugin;
    public Event(DxChestLocker plugin){
        this.plugin = plugin;
    }

    List<Location> LockedChests = new ArrayList<Location>();
    Inventory CreateCodeInv = Bukkit.createInventory(this, 9, plugin.getConfig().getString("LockerUI.create"));
    Inventory EditCodeInv = Bukkit.createInventory(this, 9, "Edit Locker");
    Inventory ProceedCodeInv = Bukkit.createInventory(this, 9, "Enter Code");
    Map<Location, int[]> Codes = new HashMap<>();

    @EventHandler
    public void LockChest(PlayerInteractEvent p){
        Player player = p.getPlayer();

        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("0");
        item.setItemMeta(meta);
        CreateCodeInv.setItem(0, item);
        CreateCodeInv.setItem(1, item);
        CreateCodeInv.setItem(2, item);
        CreateCodeInv.setItem(3, item);
        CreateCodeInv.setItem(4, item);
        CreateCodeInv.setItem(5, item);

        ItemStack confirm = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta_confirm = item.getItemMeta();
        meta_confirm.setDisplayName("Confirm");
        confirm.setItemMeta(meta_confirm);
        CreateCodeInv.setItem(8, confirm);

        EditCodeInv.setItem(0, item);
        EditCodeInv.setItem(1, item);
        EditCodeInv.setItem(2, item);
        EditCodeInv.setItem(3, item);
        EditCodeInv.setItem(4, item);
        EditCodeInv.setItem(5, item);

        EditCodeInv.setItem(7, confirm);

        ItemStack cancel = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta_cancel = item.getItemMeta();
        meta_cancel.setDisplayName("Remove code");
        cancel.setItemMeta(meta_cancel);
        EditCodeInv.setItem(8, cancel);

        ProceedCodeInv.setItem(0, item);
        ProceedCodeInv.setItem(1, item);
        ProceedCodeInv.setItem(2, item);
        ProceedCodeInv.setItem(3, item);
        ProceedCodeInv.setItem(4, item);
        ProceedCodeInv.setItem(5, item);

        ProceedCodeInv.setItem(8, confirm);

        ItemStack MainHand = player.getInventory().getItemInMainHand();
        Location ClickedLocation = p.getClickedBlock().getLocation();
        if (MainHand.getType() == Material.STICK &&
                MainHand.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "KeyLocker") &&
                !LockedChests.contains(p.getClickedBlock().getLocation()) &&
                p.getClickedBlock().getType().equals(Material.CHEST))
        {
            p.setCancelled(true);
            player.openInventory(CreateCodeInv);
        }
        else
        if (MainHand.getType() == Material.STICK &&
                LockedChests.contains(p.getClickedBlock().getLocation()) &&
                p.getClickedBlock().getType().equals(Material.CHEST) &&
                MainHand.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Key") &&
                Integer.parseInt(MainHand.getItemMeta().getLore().get(1)) == ClickedLocation.getBlockX() &&
                Integer.parseInt(MainHand.getItemMeta().getLore().get(2)) == ClickedLocation.getBlockY() &&
                Integer.parseInt(MainHand.getItemMeta().getLore().get(3)) == ClickedLocation.getBlockZ())
        {

            if (player.isSneaking()){
                p.setCancelled(true);
                player.openInventory(EditCodeInv);
            } else {
                p.setCancelled(false);
            }
        } else
        if (LockedChests.contains(p.getClickedBlock().getLocation()) &&
                p.getClickedBlock().getType().equals(Material.CHEST))
        {
            p.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 3);
            player.openInventory(ProceedCodeInv);
            //player.sendMessage(ChatColor.RED + "Chest locked");
        }
    }
    @EventHandler
    public void Chest(InventoryClickEvent e){
        Inventory i = e.getInventory();
        ItemStack CurrentItem = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        if (i.equals(this.CreateCodeInv) || i.equals(this.EditCodeInv) || i.equals(this.ProceedCodeInv)){
            e.setCancelled(true);
            if (CurrentItem.getType() == Material.GRAY_STAINED_GLASS_PANE){
                ItemMeta meta = CurrentItem.getItemMeta();
                int n = Integer.parseInt(CurrentItem.getItemMeta().getDisplayName()) + 1;
                if (n > 9) n = 0;
                meta.setDisplayName(String.valueOf(n));
                e.getCurrentItem().setItemMeta(meta);
            }
        }
        Location CurrentChestLocation = e.getWhoClicked().getTargetBlockExact(5).getLocation();
        if (i.equals(this.CreateCodeInv)) {
            if (CurrentItem.getType() == Material.SLIME_BALL){
                LockedChests.add(CurrentChestLocation);
                player.closeInventory();
                player.getInventory().getItemInMainHand().setAmount(
                        player.getInventory().getItemInMainHand().getAmount() - 1);

                ItemStack item = new ItemStack(Material.STICK);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "Key");
                List<String> lore = new ArrayList<String>();
                lore.add("Unlock the chest");
                lore.add(String.valueOf(CurrentChestLocation.getBlockX()));
                lore.add(String.valueOf(CurrentChestLocation.getBlockY()));
                lore.add(String.valueOf(CurrentChestLocation.getBlockZ()));
                meta.setLore(lore);
                meta.addEnchant(Enchantment.IMPALING, 0, true);
                item.setItemMeta(meta);

                player.getInventory().addItem(item);

                int[] code = new int[6];
                for (int n = 0; n < 6; n++){
                    code[n] = Integer.parseInt(i.getItem(n).getItemMeta().getDisplayName());
                }
                Codes.put(CurrentChestLocation, code);
            }
        }
        if (i.equals(this.EditCodeInv)) {
            if (CurrentItem.getType() == Material.SLIME_BALL){
                int[] code = new int[6];
                for (int n = 0; n < 6; n++){
                    code[n] = Integer.parseInt(i.getItem(n).getItemMeta().getDisplayName());
                }
                Codes.put(CurrentChestLocation, code);
                player.closeInventory();
            }
            if (CurrentItem.getType() == Material.BARRIER){
                LockedChests.remove(CurrentChestLocation);
                Codes.remove(CurrentChestLocation);
                player.closeInventory();
                player.getInventory().getItemInMainHand().setAmount(
                        player.getInventory().getItemInMainHand().getAmount() - 1);
                ItemStack item = new ItemStack(Material.STICK);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "KeyLocker");
                List<String> lore = new ArrayList<String>();
                lore.add("Lock the chest");
                meta.setLore(lore);
                meta.addEnchant(Enchantment.IMPALING, 0, true);
                item.setItemMeta(meta);
                player.getInventory().addItem(item);
            }
        }
        if (i.equals(this.ProceedCodeInv)) {
            if (CurrentItem.getType() == Material.SLIME_BALL){
                player.closeInventory();
                int[] code = new int[6];
                for (int n = 0; n < 6; n++){
                    code[n] = Integer.parseInt(i.getItem(n).getItemMeta().getDisplayName());
                }
                for (int n = 0; n < 6; n++){
                    if (code[n] != Codes.get(CurrentChestLocation)[n]) {
                        player.sendMessage(ChatColor.RED + "ACCESS DENIED");
                        return;
                    }
                }
                player.sendMessage(ChatColor.GREEN + "ACCESS GRANTED");
                Block b = CurrentChestLocation.getBlock();
                Chest cur = (Chest) b.getState();
                player.openInventory(cur.getBlockInventory());
            }
        }
    }
    @Override
    public Inventory getInventory() {
        return null;
    }
}