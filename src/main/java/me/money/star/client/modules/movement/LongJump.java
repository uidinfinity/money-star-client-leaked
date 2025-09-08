package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.entity.player.PlayerMoveEvent;
import me.money.star.event.impl.network.PlayerUpdateEvent;
import me.money.star.util.player.MovementUtil;
import me.money.star.util.traits.Util;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec2f;


public class LongJump extends Module {
    //
    public Setting<JumpMode> mode = mode("Mode", JumpMode.NORMAL);
    public Setting<Float> boost = num("Boost ", 4.5f, 0.1f, 10.0f);
    public Setting<Boolean> autoDisable = bool("AutoDisable", true);

    //
    private int stage;
    private double distance;
    private double speed;
    //
    private int airTicks;
    private int groundTicks;

    /**
     *
     */
    public LongJump() {
        super("LongJump", "Allows the player to jump farther",
                Category.MOVEMENT,true,false,false);
    }



    @Override
    public void onEnable() {
        groundTicks = 0;
    }

    @Override
    public void onDisable() {
        stage = 0;
        distance = 0.0;
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() != Stage.PRE) {
            return;
        }
        double dx = Util.mc.player.getX() - Util.mc.player.prevX;
        double dz = Util.mc.player.getZ() - Util.mc.player.prevZ;
        distance = Math.sqrt(dx * dx + dz * dz);
    }

    @Subscribe
    public void onPlayerMove(PlayerMoveEvent event) {
        if (mode.getValue() == JumpMode.NORMAL) {
            if (Util.mc.player == null || Util.mc.world == null
                    || MoneyStar.moduleManager.getModuleByClass(Flight.class).isEnabled()

                    || !MovementUtil.isInputtingMovement()) {
                return;
            }
            //
            double speedEffect = 1.0;
            double slowEffect = 1.0;
            if (Util.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                double amplifier = Util.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                speedEffect = 1 + (0.2 * (amplifier + 1));
            }
            if (Util.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                double amplifier = Util.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                slowEffect = 1 + (0.2 * (amplifier + 1));
            }
            final double base = 0.2873f * speedEffect / slowEffect;
            if (stage == 0) {
                stage = 1;
                speed = boost.getValue() * base - 0.01;
            } else if (stage == 1) {
                stage = 2;
                MoneyStar.movementManager.setMotionY(0.42);
                event.setY(0.42);
                speed *= 2.149;
            } else if (stage == 2) {
                stage = 3;
                double moveSpeed = 0.66 * (distance - base);
                speed = distance - moveSpeed;
            } else {
                if (!Util.mc.world.isSpaceEmpty(Util.mc.player, Util.mc.player.getBoundingBox().offset(0,
                        Util.mc.player.getVelocity().getY(), 0)) || Util.mc.player.verticalCollision) {
                    stage = 0;
                }
                speed = distance - distance / 159.0;
            }
            speed = Math.max(speed, base);
            event.cancel();
            Vec2f motion = handleStrafeMotion((float) speed);
            event.setX(motion.x);
            event.setZ(motion.y);
        }
    }

    @Subscribe
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        // Direkt LongJump
        if (event.getStage() == Stage.PRE
                && mode.getValue() == JumpMode.GLIDE) {
            if (MoneyStar.moduleManager.getModuleByClass(Flight.class).isEnabled() || Util.mc.player.isGliding()
                    || Util.mc.player.isHoldingOntoLadder()
                    || Util.mc.player.isTouchingWater()) {
                return;
            }
            if (Util.mc.player.isOnGround()) {
                distance = 0.0;
            }
            final float direction = Util.mc.player.getYaw() +
                    ((Util.mc.player.forwardSpeed < 0.0f) ? 180 : 0) +
                    ((Util.mc.player.sidewaysSpeed > 0.0f) ? (-90.0f *
                            ((Util.mc.player.forwardSpeed < 0.0f) ? -0.5f :
                                    ((Util.mc.player.forwardSpeed > 0f) ? 0.5f : 1.0f)))
                            : 0.0f) - ((Util.mc.player.sidewaysSpeed < 0.0f) ? (-90.0f *
                    ((Util.mc.player.forwardSpeed < 0.0f) ? -0.5f :
                            ((Util.mc.player.forwardSpeed > 0.0f) ? 0.5f : 1.0f))) : 0.0f);
            final float dx = (float) Math.cos((direction + 90.0f) * Math.PI / 180.0);
            final float dz = (float) Math.sin((direction + 90.0f) * Math.PI / 180.0);
            if (!Util.mc.player.verticalCollision) {
                airTicks++;
                if (Util.mc.player.input.playerInput.sneak()) {
                    Util.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            0.0, 2.147483647e9, 0.0, false, Util.mc.player.horizontalCollision));
                }
                groundTicks = 0;
                if (!Util.mc.player.verticalCollision) {
                    if (Util.mc.player.getVelocity().y == -0.07190068807140403) {
                        MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.35f);
                    }
                    if (Util.mc.player.getVelocity().y == -0.10306193759436909) {
                        MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.55f);
                    }
                    if (Util.mc.player.getVelocity().y == -0.13395038817442878) {
                        MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.67f);
                    }
                    if (Util.mc.player.getVelocity().y == -0.16635183030382) {
                        MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.69f);
                    }
                    if (Util.mc.player.getVelocity().y == -0.19088711097794803) {
                        MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.71f);
                    }
                    if (Util.mc.player.getVelocity().y == -0.21121925191528862) {
                        MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.2f);
                    }
                    if (Util.mc.player.getVelocity().y == -0.11979897632390576) {
                        MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.93f);
                    }
                    if (Util.mc.player.getVelocity().y == -0.18758479151225355) {
                        MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.72f);
                    }
                    if (Util.mc.player.getVelocity().y == -0.21075983825251726) {
                        MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.76f);
                    }
                    if (getJumpCollisions(Util.mc.player, 70.0) < 0.5) {
                        if (Util.mc.player.getVelocity().y == -0.23537393014173347) {
                            MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.03f);
                        }
                        if (Util.mc.player.getVelocity().y == -0.08531999505205401) {
                            MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * -0.5);
                        }
                        if (Util.mc.player.getVelocity().y == -0.03659320313669756) {
                            MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * -0.1f);
                        }
                        if (Util.mc.player.getVelocity().y == -0.07481386749524899) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * -0.07f);
                        }
                        if (Util.mc.player.getVelocity().y == -0.0732677700939672) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * -0.05f);
                        }
                        if (Util.mc.player.getVelocity().y == -0.07480988066790395) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * -0.04f);
                        }
                        if (Util.mc.player.getVelocity().y == -0.0784000015258789) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.1f);
                        }
                        if (Util.mc.player.getVelocity().y == -0.08608320193943977) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.1f);
                        }
                        if (Util.mc.player.getVelocity().y == -0.08683615560584318) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.05f);
                        }
                        if (Util.mc.player.getVelocity().y == -0.08265497329678266) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.05f);
                        }
                        if (Util.mc.player.getVelocity().y == -0.08245009535659828) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.05f);
                        }
                        if (Util.mc.player.getVelocity().y == -0.08244005633718426) {
                             MoneyStar.movementManager.setMotionY(-0.08243956442521608);
                        }
                        if (Util.mc.player.getVelocity().y == -0.08243956442521608) {
                             MoneyStar.movementManager.setMotionY(-0.08244005590677261);
                        }
                        if (Util.mc.player.getVelocity().y > -0.1
                                && Util.mc.player.getVelocity().y < -0.08
                                && !Util.mc.player.isOnGround()
                                && Util.mc.player.input.playerInput.forward()) {
                             MoneyStar.movementManager.setMotionY(-1.0e-4f);
                        }
                    } else {
                        if (Util.mc.player.getVelocity().y < -0.2
                                && Util.mc.player.getVelocity().y > -0.24) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.7);
                        }
                        if (Util.mc.player.getVelocity().y < -0.25
                                && Util.mc.player.getVelocity().y > -0.32) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.8);
                        }
                        if (Util.mc.player.getVelocity().y < -0.35
                                && Util.mc.player.getVelocity().y > -0.8) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.98);
                        }
                        if (Util.mc.player.getVelocity().y < -0.8
                                && Util.mc.player.getVelocity().y > -1.6) {
                             MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y * 0.99);
                        }
                    }
                }
                MoneyStar.tickManager.setClientTick(0.85f);
                double[] jumpFactor = new double[]
                        {
                                0.420606, 0.417924, 0.415258, 0.412609,
                                0.409977, 0.407361, 0.404761, 0.402178,
                                0.399611, 0.39706, 0.394525, 0.392, 0.3894,
                                0.38644, 0.383655, 0.381105, 0.37867, 0.37625,
                                0.37384, 0.37145, 0.369, 0.3666, 0.3642, 0.3618,
                                0.35945, 0.357, 0.354, 0.351, 0.348, 0.345,
                                0.342, 0.339, 0.336, 0.333, 0.33, 0.327, 0.324,
                                0.321, 0.318, 0.315, 0.312, 0.309, 0.307,
                                0.305, 0.303, 0.3, 0.297, 0.295, 0.293, 0.291,
                                0.289, 0.287, 0.285, 0.283, 0.281, 0.279, 0.277,
                                0.275, 0.273, 0.271, 0.269, 0.267, 0.265, 0.263,
                                0.261, 0.259, 0.257, 0.255, 0.253, 0.251, 0.249,
                                0.247, 0.245, 0.243, 0.241, 0.239, 0.237
                        };
                if (Util.mc.player.input.playerInput.forward()) {
                    try {
                         MoneyStar.movementManager.setMotionXZ((double) dx * jumpFactor[airTicks - 1] * 3.0,
                                (double) dz * jumpFactor[airTicks - 1] * 3.0);
                    } catch (ArrayIndexOutOfBoundsException ignored) {

                    }
                    return;
                }
                 MoneyStar.movementManager.setMotionXZ(0.0, 0.0);
                return;
            }
            MoneyStar.tickManager.setClientTick(1.0f);
            airTicks = 0;
            groundTicks++;
             MoneyStar.movementManager.setMotionXZ(Util.mc.player.getVelocity().x / 13.0,
                    Util.mc.player.getVelocity().z / 13.0);
            if (groundTicks == 1) {
                Util.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        Util.mc.player.getX(), Util.mc.player.getY(),
                        Util.mc.player.getZ(), Util.mc.player.isOnGround(), Util.mc.player.horizontalCollision));
                Util.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        Util.mc.player.getX() + 0.0624, Util.mc.player.getY(),
                        Util.mc.player.getZ(), Util.mc.player.isOnGround(), Util.mc.player.horizontalCollision));
                Util.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        Util.mc.player.getX(), Util.mc.player.getY() + 0.419,
                        Util.mc.player.getZ(), Util.mc.player.isOnGround(), Util.mc.player.horizontalCollision));
                Util.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        Util.mc.player.getX() + 0.0624, Util.mc.player.getY(),
                        Util.mc.player.getZ(), Util.mc.player.isOnGround(), Util.mc.player.horizontalCollision));
                Util.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        Util.mc.player.getX(), Util.mc.player.getY() + 0.419,
                        Util.mc.player.getZ(), Util.mc.player.isOnGround(), Util.mc.player.horizontalCollision));
            }
            if (groundTicks > 2) {
                groundTicks = 0;
                 MoneyStar.movementManager.setMotionXZ(dx * 0.3, dz * 0.3);
                 MoneyStar.movementManager.setMotionY(0.42399999499320984);
            }
        }
    }

    @Subscribe
    public void onPacketInbound(PacketEvent event) {
        if (Util.mc.player == null || Util.mc.world == null
                || Util.mc.currentScreen instanceof DownloadingTerrainScreen) {
            return;
        }
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket
                && autoDisable.getValue()) {
            disable();
        }
    }

    /**
     * @param player
     * @param d
     * @return
     */
    private double getJumpCollisions(PlayerEntity player, double d) {
        /*
        List<VoxelShape> collisions = Lists.newArrayList(mc.world.getCollisions(
                player, player.getBoundingBox().expand(0.0, -d, 0.0)));
        if (collisions.isEmpty())
        {
            return 0.0;
        }
        d = 0.0;
        for (VoxelShape coll : collisions)
        {
            Box bb = coll.getBoundingBox();
            if (bb.maxY <= d)
            {
                continue;
            }
            d = bb.maxY;
        }
        return player.getY() - d;
        */
        return 1.0;
    }

    public enum JumpMode {
        NORMAL,
        GLIDE
    }
    public Vec2f handleStrafeMotion(final float speed) {
        float forward = Util.mc.player.input.movementForward;
        float strafe = Util.mc.player.input.movementSideways;
        float yaw = Util.mc.player.prevYaw + (Util.mc.player.getYaw() - Util.mc.player.prevYaw) * Util.mc.getRenderTickCounter().getTickDelta(true);
        if (forward == 0.0f && strafe == 0.0f) {
            return Vec2f.ZERO;
        } else if (forward != 0.0f) {
            if (strafe >= 1.0f) {
                yaw += forward > 0.0f ? -45 : 45;
                strafe = 0.0f;
            } else if (strafe <= -1.0f) {
                yaw += forward > 0.0f ? 45 : -45;
                strafe = 0.0f;
            }
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        float rx = (float) Math.cos(Math.toRadians(yaw));
        float rz = (float) -Math.sin(Math.toRadians(yaw));
        return new Vec2f((forward * speed * rz) + (strafe * speed * rx),
                (forward * speed * rx) - (strafe * speed * rz));
    }
}
