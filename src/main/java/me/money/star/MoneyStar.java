package me.money.star;
 // leaked by 4asik with love <3
import me.money.star.client.manager.client.*;
import me.money.star.client.manager.combat.PearlManager;
import me.money.star.client.manager.player.*;
import me.money.star.client.modules.Modules;
import me.money.star.client.manager.BlockManager;
import me.money.star.client.manager.anticheat.AntiCheatManager;
import me.money.star.client.manager.combat.TotemManager;
import me.money.star.client.manager.combat.hole.HoleManager;
import me.money.star.client.manager.network.NetworkManager;
import me.money.star.client.manager.network.ServerManager;
import me.money.star.client.manager.player.interaction.InteractionManager;
import me.money.star.client.manager.player.rotation.RotationManager;
import me.money.star.client.manager.tick.TickManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.InputStream;
                    /////////////////////////   Money-Star-0.5-beta+leaked
public class MoneyStar implements ModInitializer, ClientModInitializer {
    public static final String NAME = "Money-Star";
    public static final String VERSION = "0.5-beta+leaked";

    public static float TIMER = 1f;

    public static final Logger LOGGER = LogManager.getLogger("Money-star");
    public static ServerManager serverManager;
    public static ColorManager colorManager;
    public static RotationManager rotationManager;
    public static PositionManager positionManager;
    public static NewPositionManager newPositionManager;
    public static HoleManager holeManager;
    public static EventManager eventManager;
    public static SpeedManager speedManager;
    public static CommandManager commandManager;
    public static FriendManager friendManager;
    public static Modules moduleManager;
    public static ConfigManager configManager;
    public static NetworkManager networkManager;
    public static TickManager tickManager;
    public static InventoryManager inventoryManager;
    public static AntiCheatManager antiCheatManager;
    public static MovementManager movementManager;
    public static TotemManager totemManager;
    public static InteractionManager interactionManager;
    public static BlockManager blockManager;
    public static PearlManager pearlManager;


    @Override public void onInitialize() {
        eventManager = new EventManager();
        serverManager = new ServerManager();
        rotationManager = new RotationManager();
        positionManager = new PositionManager();
        newPositionManager = new NewPositionManager();
        friendManager = new FriendManager();
        colorManager = new ColorManager();
        commandManager = new CommandManager();
        moduleManager = new Modules();
        speedManager = new SpeedManager();
        holeManager = new HoleManager();
        networkManager = new NetworkManager();
        tickManager = new TickManager();
        inventoryManager = new InventoryManager();
        antiCheatManager = new AntiCheatManager();
        movementManager = new MovementManager();
        totemManager = new TotemManager();
        interactionManager = new InteractionManager();
        blockManager = new BlockManager();
        pearlManager = new PearlManager();
    }



    @Override public void onInitializeClient() {
        eventManager.init();
        moduleManager.init();
        configManager = new ConfigManager();
        configManager.load();
        colorManager.init();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> configManager.save()));
    }
    public static void error(String message) {
        LOGGER.error(message);
    }

    /**
     * @param message
     */
    public static void error(String message, Object... params) {
        LOGGER.error(message, params);
    }
    public static void info(String message) {
        LOGGER.info(String.format("[Money-Star] %s", message));
    }


    public static InputStream getResource(String name)
    {
        InputStream is;
        if ((is = (InputStream) getResourceInternal(name)) != null)
        {
            return is;
        }

        return MoneyStar.class.getClassLoader().getResourceAsStream(name);
    }

    private static native Object getResourceInternal(Object name);

}
