package me.matl114.slimefunhopper.impl;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import me.matl114.matlib.core.AddonInitialization;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class HopperMain extends JavaPlugin implements SlimefunAddon {
    public static String username;
    public static String repo;
    public static String branch;
    static{
        username="m1919810";
        repo="SlimefunHopper";
        branch="master";
    }
    AddonInitialization matlibInstance;
    InstructionGroup group ;
    public void onEnable(){
        if (!PaperLib.isPaper()) {
            getLogger().log(Level.WARNING, "#######################################################");
            getLogger().log(Level.WARNING, "");
            getLogger().log(Level.WARNING, "自 25/2/1 起 LogiTech");
            getLogger().log(Level.WARNING, "转为 Paper 插件, 你必须要使用 Paper");
            getLogger().log(Level.WARNING, "或其分支才可使用 LogiTech.");
            getLogger().log(Level.WARNING, "立即下载 Paper: https://papermc.io/downloads/paper");
            getLogger().log(Level.WARNING, "");
            getLogger().log(Level.WARNING, "#######################################################");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        matlibInstance = new AddonInitialization(this, "SlimefunHopper")
            .displayName("粘液漏斗")
            .onEnable()
            .cast()
        ;
        Config addonConfig = new Config(this);
        Bukkit.getServer().getPluginManager().registerEvents(new HopperListener(addonConfig), this);
        group = new InstructionGroup()
            .init(this);
        tryUpdate();
        getLogger().log(Level.INFO, "#######################################################");
        getLogger().log(Level.INFO, "");
        getLogger().log(Level.INFO, "SlimefunHopper 已经成功加载");
        getLogger().log(Level.WARNING, "- 请注意配置文件和说明书(粘液书菜单中)");
        getLogger().log(Level.WARNING,"- 出现bug或报错后请尽快反馈作者");
        getLogger().log(Level.WARNING,"- 可以从guizhan构建站找到作者的github链接反馈,或者在鬼斩QQ群反馈");
        getLogger().log(Level.INFO, "");
        getLogger().log(Level.INFO, "#######################################################");
    }
    public void onDisable() {

        matlibInstance.onDisable();
        matlibInstance = null;
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        group.deconstruct();
        group = null;
    }
    public void tryUpdate() {
        if ( getDescription().getVersion().startsWith("Build")) {
            GuizhanUpdater.start(this, getFile(), username, repo, branch);
        }
    }

    @NotNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return null;
    }
}