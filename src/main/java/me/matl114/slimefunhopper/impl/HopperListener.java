package me.matl114.slimefunhopper.impl;

import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import me.matl114.matlib.nmsMirror.core.PosEnum;
import me.matl114.matlib.nmsMirror.impl.CraftBukkit;
import me.matl114.matlib.nmsMirror.impl.NMSItem;
import me.matl114.matlib.nmsUtils.LevelUtils;
import me.matl114.matlib.utils.WorldUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.HopperInventorySearchEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import static me.matl114.matlib.nmsMirror.impl.NMSItem.CONTAINER;
import static me.matl114.matlib.nmsMirror.impl.NMSItem.ITEMSTACK;
import static me.matl114.matlib.nmsMirror.impl.NMSLevel.LEVEL;
import static me.matl114.matlib.nmsMirror.impl.NMSLevel.TILE_ENTITIES;

public class HopperListener implements Listener {
    int override;
    public HopperListener(Config server){
        override = server.getOrSetDefault("options.override-slimefun-hopper-speed", -1);
    }
    final Reference2ReferenceArrayMap<World,Object> cachedHandledWorld = new Reference2ReferenceArrayMap<>();

    public static final Inventory EMPTY_INVENTORY = CraftBukkit.INVENTORYS.createCustomInventory(new InventoryHolder() {
         @Override
         public @NotNull Inventory getInventory() {
             return EMPTY_INVENTORY;
         }
     },1,"custom"
    );
    public static final Inventory FULL_INVENTORY = CraftBukkit.INVENTORYS.createCustomInventory(new InventoryHolder() {
        @Override
        public @NotNull Inventory getInventory() {
            return FULL_INVENTORY;
        }
    },1,"custom"
    );
    static{
        FULL_INVENTORY.setItem(0, ChestMenuUtils.getBackground().clone());
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void testSlimefunInventoryHopper(HopperInventorySearchEvent hopper){
        Location loc = hopper.getSearchBlock().getLocation();
        BlockMenu menu = BlockStorage.getInventory(loc);
        if(menu != null){
            if(hopper.getContainerType() == HopperInventorySearchEvent.ContainerType.SOURCE){
                limitGrabbingSlots(menu, hopper.getBlock());
                hopper.setInventory(EMPTY_INVENTORY);
            }else {
                limitPushingSlots(hopper.getBlock(), menu);
                hopper.setInventory(FULL_INVENTORY);
            }
        }

    }

    public void limitGrabbingSlots(BlockMenu sf, Block hopperBlock){
        Object slimefunContainer = CraftBukkit.INVENTORYS.getInventory(sf.getInventory());
        Object hopperContainer = LevelUtils.getBlockEntityAsync(hopperBlock, false);
        int originalValue = 1;
        Object sipigotConfig = null;
        if(override > 0){
            sipigotConfig = getConfig(sf.getLocation().getWorld());
            originalValue = CraftBukkit.SPIGOT_CONFIG.hopperAmountGetter(sipigotConfig);
            CraftBukkit.SPIGOT_CONFIG.hopperAmountSetter(sipigotConfig, override);
        }
        try{
            if(TILE_ENTITIES.isHopper(hopperContainer)){
                var world = WorldUtils.getHandledWorld(sf.getLocation().getWorld());
                var direction = PosEnum.DIR_DOWN;
                TILE_ENTITIES.hopper$setSkipPullModeEventFire(true);
                int[] access = sf.getPreset().getSlotsAccessedByItemTransport(sf, ItemTransportFlow.WITHDRAW, null);
                int i = access.length;
                for (int j = 0; j< i; ++j){
                    int k = access[j];
                    if(tryTakeInItemFromSlot(hopperContainer, slimefunContainer, k, direction, world)){
                        return;
                    }
                }
            }
        }finally {
            if(override > 0){
                CraftBukkit.SPIGOT_CONFIG.hopperAmountSetter(sipigotConfig, originalValue);
            }
        }

    }
    public boolean tryTakeInItemFromSlot(Object hopper, Object slimefunContainer, int index, Object direction, Object world){
        var itemStack = NMSItem.CONTAINER.getItem(slimefunContainer, index);
        if(!NMSItem.ITEMSTACK.isEmpty(itemStack)){
            return TILE_ENTITIES.hopper$hopperPull(world, hopper, slimefunContainer, itemStack, index);
        }
        return false;
    }
    public Object getConfig(World world){
        return cachedHandledWorld.computeIfAbsent(world, (w)->{
            Object handled = WorldUtils.getHandledWorld((World) w);
            return LEVEL.spigotConfigGetter(handled);
        });
    }

    public boolean limitPushingSlots(Block hopperBlock, BlockMenu sf){
        Object hopperContainer = LevelUtils.getBlockEntityAsync(hopperBlock, false);
        Object slimefunContainer = CraftBukkit.INVENTORYS.getInventory(sf.getInventory());
        boolean foundItem = false;
        World world = sf.getLocation().getWorld();
        int hopperValue ;
        Object spigotConfig = getConfig(world);
        if(override <= 0){
            hopperValue = CraftBukkit.SPIGOT_CONFIG.hopperAmountGetter(spigotConfig);
        }else {
            hopperValue = override;
        }
        if(TILE_ENTITIES.isHopper(hopperContainer)){
            int size = CONTAINER.getContainerSize(hopperContainer);
            for (int i=0; i< size ;++ i){
                final var itemStack0 = CONTAINER.getItem(hopperContainer, i);
                if(!ITEMSTACK.isEmpty(itemStack0)){
                    var itemStack = itemStack0;
                    foundItem = true;
                    int originalItemCount = ITEMSTACK.getCount(itemStack);
                    int movedItemCount =  Math.min(hopperValue, originalItemCount);
                    ITEMSTACK.setCount(itemStack, movedItemCount);
                    var remainingItem =addItem(sf, hopperContainer, slimefunContainer, itemStack);
                    int remainingItemCount = ITEMSTACK.getCount(remainingItem);
                    if(remainingItemCount != movedItemCount){
                        //moved
                        itemStack = ITEMSTACK.copy(itemStack, true);
                        ITEMSTACK.setCount(itemStack, originalItemCount);
                        if(!ITEMSTACK.isEmpty(itemStack)){
                            ITEMSTACK.setCount(itemStack, originalItemCount - movedItemCount + remainingItemCount);
                        }
                        CONTAINER.setItem(hopperContainer, i, itemStack);
                        return true;
                    }
                    ITEMSTACK.setCount(itemStack, originalItemCount);
                }
            }
        }
        if(foundItem){
            int cooldown = CraftBukkit.SPIGOT_CONFIG.hopperTransferGetter(spigotConfig);
            TILE_ENTITIES.hopper$setCooldown(hopperContainer, cooldown);
        }
        return false;
    }
    public Object addItem(BlockMenu sf, Object hopper, Object target, Object movedItem ){
        int[] access = sf.getPreset().getSlotsAccessedByItemTransport(sf, ItemTransportFlow.INSERT, null);
        int size = access.length;
        for (int i=0; i<size && !ITEMSTACK.isEmpty(movedItem); ++i){
            movedItem = TILE_ENTITIES.hopper$tryMoveInItem(hopper, target, movedItem, access[i], null);
        }
        return movedItem;
    }
}
