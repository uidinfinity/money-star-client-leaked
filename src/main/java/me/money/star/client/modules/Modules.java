package me.money.star.client.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.client.*;
import me.money.star.client.modules.combat.*;
import me.money.star.client.modules.exploit.*;
import me.money.star.client.modules.miscellaneous.*;
import me.money.star.client.modules.movement.*;
import me.money.star.client.modules.render.*;
import me.money.star.client.modules.world.*;
import me.money.star.event.impl.Render2DEvent;
import me.money.star.event.impl.Render3DEvent;
import me.money.star.client.System;

import me.money.star.util.traits.Jsonable;
import me.money.star.util.traits.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Modules implements Jsonable, Util {
    public List<Module> modules = new ArrayList<>();
    public List<Module> sortedModules = new ArrayList<>();
    public List<String> sortedModulesABC = new ArrayList<>();

    public void init() {
        //COMBAT
        modules.add(new Criticals());
        modules.add(new Trigger());
        modules.add(new BowRelease());
        modules.add(new BowAim());
        modules.add(new AutoFeetPlace());
        modules.add(new Offhand());
        modules.add(new Aura());
        modules.add(new Quiver());
        modules.add(new AutoExp());
        modules.add(new AutoCrystal());
        modules.add(new SelfTrap());
        modules.add(new Burrow());
       // modules.add(new LegacyCrystal());
       // modules.add(new HoleFiller());
        modules.add(new AutoTrap());
        modules.add(new AutoMine());
        modules.add(new AutoWeb());
        //MISC
        modules.add(new MiddleClick());
        modules.add(new NameProtect());
        modules.add(new Notifier());
        modules.add(new AutoAccept());
        modules.add(new AutoRespawn());
        modules.add(new ExtraTab());
        modules.add(new AutoLog());
        //modules.add(new AutoReconnect());
        modules.add(new NoAFK());
        //RENDER
        modules.add(new BlockHighlight());
        modules.add(new CrossHair());
        modules.add(new Aspect());
        modules.add(new LogoutSpots());
        modules.add(new FullBright());
        modules.add(new ViewClip());
        modules.add(new SkyColors());
        modules.add(new Environment());
        modules.add(new NoRender());
        //MOVEMENT
        modules.add(new Step());
        modules.add(new FastFall());
        //modules.add(new NoFall());
        modules.add(new Velocity());
        modules.add(new Sprint());
        modules.add(new Flight());
        modules.add(new AutoWalk());
        modules.add(new Blink());
        modules.add(new ElytraFlight());
        modules.add(new EntityControl());
        modules.add(new IceSpeed());
        modules.add(new LongJump());
        modules.add(new NoSlow());
        modules.add(new SafeWalk());
        modules.add(new Jesus());
        modules.add(new Parkour());
        //modules.add(new Speed());
        //modules.add(new FastSwim());
        //WORLD
        modules.add(new NoCooldown());
        modules.add(new AirPlace());
        modules.add(new RotationLock());
        modules.add(new AutoFish());
        modules.add(new FakePlayer());
        modules.add(new NoGlitchBlocks());
        modules.add(new AutoTool());
        modules.add(new AutoEat());
        modules.add(new AutoMount());
        modules.add(new NoInteract());
        modules.add(new Replenish());
        modules.add(new NoHitBox());
        modules.add(new Scaffold());
        modules.add(new FreeLook());
        modules.add(new SpeedMine());
        modules.add(new StayCamera());
        //modules.add(new Nuker());

        //EXPLOIT
        modules.add(new XCarry());
        modules.add(new RocketExtender());
        modules.add(new PacketCanceler());
        modules.add(new Crasher());
        modules.add(new Multitask());
        modules.add(new Timer());
        modules.add(new NoHunger());
        modules.add(new NoMineAnimation());
        modules.add(new PingSpoof());
        //CLIENT
        modules.add(new HUD());
        modules.add(new ClickGui());
        modules.add(new Debug());
        modules.add(new Rotations());
        modules.add(new Notifications());
        modules.add(new AntiCheat());
        modules.add(new Colors());
       // modules.add(new Discord());
    }

    public Module getModuleByName(String name) {
        for (Module module : this.modules) {
            if (!module.getName().equalsIgnoreCase(name)) continue;
            return module;
        }
        return null;
    }

    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        for (Module module : this.modules) {
            if (!clazz.isInstance(module)) continue;
            return (T) module;
        }
        return null;
    }

    public void enableModule(Class<Module> clazz) {
        Module module = this.getModuleByClass(clazz);
        if (module != null) {
            module.enable();
        }
    }

    public void disableModule(Class<Module> clazz) {
        Module module = this.getModuleByClass(clazz);
        if (module != null) {
            module.disable();
        }
    }

    public void enableModule(String name) {
        Module module = this.getModuleByName(name);
        if (module != null) {
            module.enable();
        }
    }

    public void disableModule(String name) {
        Module module = this.getModuleByName(name);
        if (module != null) {
            module.disable();
        }
    }

    public boolean isModuleEnabled(String name) {
        Module module = this.getModuleByName(name);
        return module != null && module.isOn();
    }

    public boolean isModuleEnabled(Class<Module> clazz) {
        Module module = this.getModuleByClass(clazz);
        return module != null && module.isOn();
    }

    public Module getModuleByDisplayName(String displayName) {
        for (Module module : this.modules) {
            if (!module.getDisplayName().equalsIgnoreCase(displayName)) continue;
            return module;
        }
        return null;
    }

    public ArrayList<Module> getEnabledModules() {
        ArrayList<Module> enabledModules = new ArrayList<>();
        for (Module module : this.modules) {
            if (!module.isEnabled()) continue;
            enabledModules.add(module);
        }
        return enabledModules;
    }

    public ArrayList<String> getEnabledModulesName() {
        ArrayList<String> enabledModules = new ArrayList<>();
        for (Module module : this.modules) {
            if (!module.isEnabled() || !module.isDrawn()) continue;
            enabledModules.add(module.getFullArrayString());
        }
        return enabledModules;
    }

    public ArrayList<Module> getModulesByCategory(Module.Category category) {
        ArrayList<Module> modulesCategory = new ArrayList<Module>();
        this.modules.forEach(module -> {
            if (module.getCategory() == category) {
                modulesCategory.add(module);
            }
        });
        return modulesCategory;
    }

    public List<Module.Category> getCategories() {
        return Arrays.asList(Module.Category.values());
    }

    public void onLoad() {
        this.modules.stream().filter(Module::listening).forEach(EVENT_BUS::register);
        this.modules.forEach(Module::onLoad);
    }

    public void onUpdate() {
        this.modules.stream().filter(System::isEnabled).forEach(Module::onUpdate);
    }

    public void onTick() {
        this.modules.stream().filter(System::isEnabled).forEach(Module::onTick);
    }

    public void onRender2D(Render2DEvent event) {
        this.modules.stream().filter(System::isEnabled).forEach(module -> module.onRender2D(event));
    }

    public void onRender3D(Render3DEvent event) {
        this.modules.stream().filter(System::isEnabled).forEach(module -> module.onRender3D(event));
    }

    public void sortModules(boolean reverse) {
        this.sortedModules = this.getEnabledModules().stream().filter(Module::isDrawn)
                .sorted(Comparator.comparing(module -> mc.textRenderer.getWidth(module.getFullArrayString()) * (reverse ? -1 : 1)))
                .collect(Collectors.toList());
    }

    public void sortModulesABC() {
        this.sortedModulesABC = new ArrayList<>(this.getEnabledModulesName());
        this.sortedModulesABC.sort(String.CASE_INSENSITIVE_ORDER);
    }

    public void onUnload() {
        this.modules.forEach(EVENT_BUS::unregister);
        this.modules.forEach(Module::onUnload);
    }

    public void onUnloadPost() {
        for (Module module : this.modules) {
            module.enabled.setValue(false);
        }
    }

    public void onKeyPressed(int eventKey) {
        if (eventKey <= 0) return;
        this.modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey) {
                module.toggle();
            }
        });
    }

    @Override public JsonElement toJson() {
        JsonObject object = new JsonObject();
        for (Module module : modules) {
            object.add(module.getName(), module.toJson());
        }
        return object;
    }

    @Override public void fromJson(JsonElement element) {
        for (Module module : modules) {
            module.fromJson(element.getAsJsonObject().get(module.getName()));
        }
    }

    @Override public String getFileName() {
        return "modules.json";
    }
}
