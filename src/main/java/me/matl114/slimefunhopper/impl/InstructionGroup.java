package me.matl114.slimefunhopper.impl;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemState;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.matl114.matlib.core.Manager;
import me.matl114.matlib.implement.slimefun.menu.guideMenu.CustomItemGroup;
import me.matl114.matlib.implement.slimefun.menu.menuGroup.CustomMenuGroup;
import me.matl114.matlib.slimefunUtils.SlimefunRegistryUtils;
import me.matl114.matlib.utils.AddUtils;
import me.matl114.matlib.utils.inventory.itemStacks.CleanItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InstructionGroup  implements Manager {
    Plugin pl;
    private ItemGroup group;
    @Override
    public InstructionGroup init(Plugin pl, String... path) {
        this.pl  = pl;
        this.addToRegistry();
        ItemStack icon = new CleanItemStack(Material.HOPPER,
            AddUtils.colorRandomString("粘液 漏斗"),
            "&7by "+AddUtils.colorPseudorandomString("matl114"),
            "&7⇨ &a点击查看说明书~"
        );
        ItemStack info1 = new CleanItemStack(
            Material.KNOWLEDGE_BOOK,
            AddUtils.colorRandomString("说明书 1"),
            "&7本附属增加了若干与漏斗相关的特性",
            "&7具体特性如下:"
        );
        ItemStack info2 = new CleanItemStack(
            Material.HOPPER,
            AddUtils.colorRandomString("特性1: 漏斗将可直接和粘液机器交互"),
            "&7本附属使漏斗可以直接抓取和推送粘液机器中的物品",
            "&7将漏斗像与原版箱子交互一样放置",
            "&7即可交互粘液机器的槽位",
            "&7其中,当漏斗位于机器下方,抓取机器的时候",
            "&7将只会抓取机器的可输出槽位",
            "&7其他情况,只会向机器的可输入槽位输入",
            "&7漏斗的速度&a默认和原版漏斗一致",
            "&7可以在spigot.yml中修改原版速度的货运速度",
            "&7也可以在本附属的配置文件中专门修改对粘液机器的漏斗货运速度"
        );
        this.group = new CustomItemGroup(
            new NamespacedKey("slimefunhopper","instruction"),
            icon,
            false
        )
            .setLoader(
                CustomMenuGroup.defaultGroupTemplate(icon.getItemMeta().getDisplayName())
                    .addItem(0,info1 , CustomMenuGroup.CustomMenuClickHandler.ofEmpty())
                    .addItem(1, info2, CustomMenuGroup.CustomMenuClickHandler.ofEmpty())
                ,
                Map.of(),
                Map.of()
            )
            .setBackButton(1)
            .setSearchButton(7)
        ;
        this.group.setTier(-999);
        this.group.register((SlimefunAddon) pl);
        return this;
    }

    @Override
    public InstructionGroup reload() {
        deconstruct();
        return init(pl);
    }

    @Override
    public void deconstruct() {
        this.removeFromRegistry();
        if(this.group != null){
            Slimefun.getRegistry().getAllItemGroups().remove(this.group);
            List<ItemGroup> categories = Slimefun.getRegistry().getAllItemGroups();
            Collections.sort(categories, Comparator.comparingInt(ItemGroup::getTier));
        }
        SlimefunRegistryUtils.disableItemGroup(this.group);
    }
}
