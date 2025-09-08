package me.money.star.client.modules.client;

import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.util.discord.DiscordEventHandlers;
import me.money.star.util.discord.DiscordRPC;
import me.money.star.util.discord.DiscordRichPresence;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

import java.io.*;

public class Discord extends Module {
    public static DiscordRPC rpc = DiscordRPC.INSTANCE;
    public Setting<Boolean> ip = bool("Server", false);

    public static DiscordRichPresence presence = new DiscordRichPresence();
    public static boolean started;
    static String String1 = "none";
    public static Thread thread;
    private static Discord instance = new Discord();

    public Discord() {
        super("Discord", "RPC Custom", Module.Category.CLIENT, true, false, false);
        this.setInstance();
    }

    public static Discord getInstance() {
        if (instance == null) {
            instance = new Discord();
        }
        return instance;
    }

    private void setInstance() {
        instance = this;
    }


    public static void readFile() {
        try {
            File file = new File("Cracked/discord/RPC.txt");
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready()) {
                        String1 = reader.readLine();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void WriteFile(String url1, String url2) {
        File file = new File("Cracked/discord/RPC.txt");
        try {
            file.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(url1 + "SEPARATOR" + url2 + '\n');
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDisable() {
        started = false;
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        rpc.Discord_Shutdown();
    }

    @Override
    public void onUpdate() {
        startRpc();
    }

    public void startRpc() {
        if (isDisabled()) return;
        if (!started) {
            started = true;
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            rpc.Discord_Initialize("1313502538263171092", handlers, true, "");
            presence.startTimestamp = (System.currentTimeMillis() / 1000L);

            presence.largeImageText = "$";

            rpc.Discord_UpdatePresence(presence);

            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    rpc.Discord_RunCallbacks();

                    presence.details = getDetails();
                    presence.button_label_1 = "Download";
                    presence.button_url_1 = "";
                    presence.largeImageKey = "https://i.imgur.com/L1ZAfdM.gif";
                    rpc.Discord_UpdatePresence(presence);
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }, "RPC-Handler");
            thread.start();
        }
    }

    private String getDetails() {
        String result = "";

        if (mc.currentScreen instanceof TitleScreen) {
            result = "In Main menu";
        } else if (mc.currentScreen instanceof MultiplayerScreen || mc.currentScreen instanceof AddServerScreen) {
            result = "Picks a server";
        } else if (mc.getCurrentServerEntry() != null) {
            result = (ip.getValue() ? "Playing on " + mc.getCurrentServerEntry().address : "Playing on server");
            if (mc.getCurrentServerEntry().address.equals("ciper.me"))
                result = mc.getCurrentServerEntry().address + " ";
        } else if (mc.isInSingleplayer()) {
            result = "Offline gamer";
        }
        return result;
    }

    private boolean isOn(int x, int z, int x1, int z1) {
        return mc.player.getX() > x && mc.player.getX() < x1 && mc.player.getZ() > z && mc.player.getZ() < z1;
    }


}