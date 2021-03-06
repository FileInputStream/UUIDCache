package de.fileinputstream.uuidcache;

import de.fileinputstream.uuidcache.cache.UUIDCache;
import de.fileinputstream.uuidcache.commands.CommandEvictUUID;
import de.fileinputstream.uuidcache.commands.CommandUUID;
import de.fileinputstream.uuidcache.listeners.ListenerLogin;
import de.fileinputstream.uuidcache.redis.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class has been generated by Alexander on 29.04.18 21:57
 * You are not allowed to edit this resource or other components of it
 * © 2018 Alexander Fiedler
 */
public class UUIDCacheBootstrap extends JavaPlugin {

    public static UUIDCacheBootstrap instance;
    public final ExecutorService mainService = Executors.newCachedThreadPool();
    public final ExecutorService redisService = Executors.newFixedThreadPool(4);
    public final RedisManager redisManager = new RedisManager();
    public final UUIDCache uuidCache = new UUIDCache();

    public String redisHost;
    public int redisPort;
    public String redisPassword;
    public int cacheEntryExpire;
    public boolean metricsEnabled;

    @Override
    public void onEnable() {
        instance = this;
        writeConfig();
        loadConfig();
        getCommand("uuid").setExecutor(new CommandUUID());
        getCommand("evictuuid").setExecutor(new CommandEvictUUID());
        Bukkit.getPluginManager().registerEvents(new ListenerLogin(),this);
        startCacheClearerThread();
        Metrics metrics = new Metrics(this);

    }

    public void writeConfig() {
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("RedisHost","127.0.0.1");
        getConfig().addDefault("RedisPort",6379);
        getConfig().addDefault("AuthUsingPassword",false);
        getConfig().addDefault("RedisPassword","MyPassword");
        getConfig().addDefault("CacheEntryExpire",7200); //not necessary when 'AuthUsingPassword' is set to false
        getConfig().addDefault("MetricsEnabled", true);
        saveConfig();
    }

    public void startCacheClearerThread() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            long millisBefore = System.currentTimeMillis();
            System.out.println("Clearing uuid cache...");
            getUuidCache().uuidCache.clear();
            long difference = System.currentTimeMillis() - millisBefore;
            System.out.println("UUID cache clear done in " + difference + " seconds");
        }, getCacheEntryExpire(), getCacheEntryExpire());
    }

    public void loadConfig() {
        this.redisHost = getConfig().getString("RedisHost");
        this.redisPort = getConfig().getInt("RedisPort");
        this.cacheEntryExpire = getConfig().getInt("CacheEntryExpire");
        redisService.execute(() -> redisManager.connectToRedis(redisHost,redisPort));
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public ExecutorService getMainService() {
        return mainService;
    }

    public ExecutorService getRedisService() {
        return redisService;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public UUIDCache getUuidCache() {
        return uuidCache;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getCacheEntryExpire() {
        return cacheEntryExpire;
    }

    public static UUIDCacheBootstrap getInstance() {
        return instance;
    }
}
