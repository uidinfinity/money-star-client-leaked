package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.exploit.Timer;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.ClientEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.entity.player.PlayerMoveEvent;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.util.math.MathUtil;
import me.money.star.util.player.MovementUtil;
import me.money.star.util.world.FakePlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;



public class Speed extends Module {
    private static Speed INSTANCE;

    //
    public Setting<SpeedMode> speedMode = mode("Mode", SpeedMode.STRAFE);
    public Setting<Float> speedPlayer = num("Speed", 4.0f, 0.1f, 10.0f);
    public Setting<Float> collisionDistance = num("collision-Distance", 1.5f, 0.5f, 2.0f);
    public Setting<Boolean> vanillaStrafe = bool("Strafe-Vanilla", false);
    public Setting<Boolean> fast = bool("Fast", false);
    public Setting<Boolean> strafeBoost = bool("Strafe-Boost", false);
    public Setting<Boolean> timer = bool("Use-Timer", false);
    public Setting<Integer> boostTicksPlayer = num("Boost-Ticks ", 10, 20, 40);
    public Setting<Boolean> speedWater = bool("Speed-In-Water", false);
    //
    private int strafe = 4;
    private boolean accel;
    private int strictTicks;
    private int strictFastTicks;
    private int boostTicks;
    //
    private double speed;
    private double boostSpeed;
    private double distance;
    //
    private boolean prevTimer;

    private static final float FRICTION = 159.077f;



    public Speed() {
        super("Speed", "Move faster", Category.MOVEMENT,true,false,false);
        INSTANCE = this;

    }

    public static Speed getInstance()
    {
        return INSTANCE;
    }







    @Override
    public void onEnable()
    {
        prevTimer = Timer.getInstance().isEnabled();
        if (timer.getValue() && !prevTimer && isStrafe())
        {
            Timer.getInstance().enable();
        }
    }

    @Override
    public void onDisable()
    {
        resetStrafe();
        if (Timer.getInstance().isEnabled())
        {
            Timer.getInstance().resetTimer();
            if (!prevTimer)
            {
                Timer.getInstance().disable();
            }
        }
    }

    @Subscribe
    public void onTick(TickEvent event)
    {
        if (event.getStage() == Stage.PRE)
        {
            boostTicks++;
            if (boostTicks > boostTicksPlayer.getValue())
            {
                boostSpeed = 0.0;
            }
            double dx = mc.player.getX() - mc.player.prevX;
            double dz = mc.player.getZ() - mc.player.prevZ;
            distance = Math.sqrt(dx * dx + dz * dz);
            if (speedMode.getValue() == SpeedMode.GRIM_COLLIDE && MovementUtil.isInputtingMovement())
            {
                int collisions = 0;
                for (Entity entity : mc.world.getEntities())
                {
                    if (checkIsCollidingEntity(entity) && MathHelper.sqrt((float) mc.player.squaredDistanceTo(entity)) <= collisionDistance.getValue())
                    {
                        collisions++;
                    }
                }
                if (collisions > 0)
                {
                    Vec3d velocity = mc.player.getVelocity();
                    // double COLLISION_DISTANCE = 1.5;
                    double factor = 0.08 * collisions;
                    Vec2f strafe = handleStrafeMotion((float) factor);
                    mc.player.setVelocity(velocity.x + strafe.x, velocity.y, velocity.z + strafe.y);
                }
            }
        }
    }

    @Subscribe
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (mc.player != null && mc.world != null && event.getType() == MovementType.SELF)
        {
            if (!MovementUtil.isInputtingMovement()
                    || MoneyStar.moduleManager.getModuleByClass(Flight.class).isEnabled()
                    || MoneyStar.moduleManager.getModuleByClass(LongJump.class).isEnabled()
                   // || (FreecamModule.getInstance().isEnabled() && !FreecamModule.getInstance().control)
                    || mc.player.getAbilities().flying
                    //      || Modules.ELYTRA_FLY.isEnabled()
                    || mc.player.isRiding()
                    || mc.player.isGliding()
                    || mc.player.isHoldingOntoLadder()
                    || mc.player.fallDistance > 2.0f
                    || (mc.player.isInLava() || mc.player.isTouchingWater())
                    && !speedWater.getValue())
            {
                resetStrafe();
                Timer.getInstance().setTimer(1.0f);
                return;
            }
            event.cancel();
            //
            double speedEffect = 1.0;
            double slowEffect = 1.0;
            if (mc.player.hasStatusEffect(StatusEffects.SPEED))
            {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                speedEffect = 1 + (0.2 * (amplifier + 1));
            }
            if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS))
            {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                slowEffect = 1 + (0.2 * (amplifier + 1));
            }
            final double base = 0.2873f * speedEffect / slowEffect;
            float jumpEffect = 0.0f;
            if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST))
            {
                jumpEffect += (mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;
            }
            // ~29 kmh
            if (speedMode.getValue() == SpeedMode.STRAFE || speedMode.getValue() == SpeedMode.STRAFE_B_HOP)
            {
                if (!MoneyStar.antiCheatManager.hasPassed(100))
                {
                    return;
                }
                if (timer.getValue())
                {
                    Timer.getInstance().setTimer(1.0888f);
                }
                if (strafe == 1)
                {
                    speed = 1.35f * base - 0.01f;
                }
                else if (strafe == 2)
                {
                    if (mc.player.input.playerInput.jump() || !mc.player.isOnGround())
                    {
                        return;
                    }
                    float jump = (speedMode.getValue() == SpeedMode.STRAFE_B_HOP ? 0.4000000059604645f : 0.3999999463558197f) + jumpEffect;
                    event.setY(jump);
                    MoneyStar.movementManager.setMotionY(jump);
                    speed *= speedMode.getValue() == SpeedMode.STRAFE_B_HOP ? 1.535 : (accel ? 1.6835 : 1.395);
                }
                else if (strafe == 3)
                {
                    double moveSpeed = 0.66 * (distance - base);
                    speed = distance - moveSpeed;
                    accel = !accel;
                }
                else
                {
                    if ((!mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(0,
                            mc.player.getVelocity().getY(), 0)) || mc.player.verticalCollision) && strafe > 0)
                    {
                        strafe = MovementUtil.isInputtingMovement() ? 1 : 0;
                    }
                    speed = distance - distance / FRICTION;
                }
                speed = Math.max(speed, base);
                if (strafeBoost.getValue())
                {
                    speed += boostSpeed;
                }
                final Vec2f motion = handleStrafeMotion((float) speed);
                event.setX(motion.x);
                event.setZ(motion.y);
                strafe++;
            }
            // ~26-27 kmh
            else if (speedMode.getValue() == SpeedMode.STRAFE_STRICT)
            {
                if (!MoneyStar.antiCheatManager.hasPassed(100))
                {
                    return;
                }
                if (timer.getValue())
                {
                    if (fast.getValue())
                    {
                        ++strictFastTicks;
                        if (strictFastTicks > 10)
                        {
                            strictFastTicks = 0;
                        }
                        float res = 1.0f + strictFastTicks / 100.0f;
                        Timer.getInstance().setTimer(Math.max(1.0f, res));
                    }
                    else
                    {
                        Timer.getInstance().setTimer(1.0888f);
                    }
                }
                if (strafe == 1)
                {
                    speed = 1.35f * base - 0.01f;
                }
                else if (strafe == 2)
                {
                    if (mc.player.input.playerInput.jump() || !mc.player.isOnGround())
                    {
                        return;
                    }
                    float jump = 0.3999999463558197f + jumpEffect;
                    event.setY(jump);
                    MoneyStar.movementManager.setMotionY(jump);
                    speed *= 2.149;
                }
                else if (strafe == 3)
                {
                    double moveSpeed = 0.66 * (distance - base);
                    speed = distance - moveSpeed;
                }
                else
                {
                    if ((!mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(0,
                            mc.player.getVelocity().getY(), 0)) || mc.player.verticalCollision) && strafe > 0)
                    {
                        strafe = MovementUtil.isInputtingMovement() ? 1 : 0;
                    }
                    speed = distance - distance / FRICTION;
                }
                strictTicks++;
                speed = Math.max(speed, base);
                //
                if (timer.getValue())
                {
                    Timer.getInstance().setTimer(1.0888f);
                }
                double baseMax = 0.465 * speedEffect / slowEffect;
                double baseMin = 0.44 * speedEffect / slowEffect;
                speed = Math.min(speed, strictTicks > 25 ? baseMax : baseMin);
                if (strafeBoost.getValue())
                {
                    speed += boostSpeed;
                }
                if (strictTicks > 50)
                {
                    strictTicks = 0;
                }
                final Vec2f motion = handleStrafeMotion((float) speed);
                event.setX(motion.x);
                event.setZ(motion.y);
                strafe++;
            }
            else if (speedMode.getValue() == SpeedMode.LOW_HOP)
            {
                if (!MoneyStar.antiCheatManager.hasPassed(100))
                {
                    return;
                }
                if (timer.getValue())
                {
                    Timer.getInstance().setTimer(1.0888f);
                }
                if (MathUtil.round(mc.player.getY() - (double) (int) mc.player.getY(), 3) == MathUtil.round(0.4, 3))
                {
                    MoneyStar.movementManager.setMotionY(0.31 + jumpEffect);
                    event.setY(0.31 + jumpEffect);
                }
                else if (MathUtil.round(mc.player.getY() - (double) (int) mc.player.getY(), 3) == MathUtil.round(0.71, 3))
                {
                    MoneyStar.movementManager.setMotionY(0.04 + jumpEffect);
                    event.setY(0.04 + jumpEffect);
                }
                else if (MathUtil.round(mc.player.getY() - (double) (int) mc.player.getY(), 3) == MathUtil.round(0.75, 3))
                {
                    MoneyStar.movementManager.setMotionY(-0.2 - jumpEffect);
                    event.setY(-0.2 - jumpEffect);
                }
                else if (MathUtil.round(mc.player.getY() - (double) (int) mc.player.getY(), 3) == MathUtil.round(0.55, 3))
                {
                    MoneyStar.movementManager.setMotionY(-0.14 + jumpEffect);
                    event.setY(-0.14 + jumpEffect);
                }
                else
                {
                    if (MathUtil.round(mc.player.getY() - (double) (int) mc.player.getY(), 3) == MathUtil.round(0.41, 3))
                    {
                        MoneyStar.movementManager.setMotionY(-0.2 + jumpEffect);
                        event.setY(-0.2 + jumpEffect);
                    }
                }
                if (strafe == 1)
                {
                    speed = 1.35f * base - 0.01f;
                }
                else if (strafe == 2)
                {
                    double jump = (isBoxColliding() ? 0.2 : 0.3999) + jumpEffect;
                    MoneyStar.movementManager.setMotionY(jump);
                    event.setY(jump);
                    speed *= accel ? 1.5685 : 1.3445;
                }
                else if (strafe == 3)
                {
                    double moveSpeed = 0.66 * (distance - base);
                    speed = distance - moveSpeed;
                    accel = !accel;
                }
                else
                {
                    if (mc.player.isOnGround() && strafe > 0)
                    {
                        strafe = MovementUtil.isInputtingMovement() ? 1 : 0;
                    }
                    speed = distance - distance / FRICTION;
                }
                speed = Math.max(speed, base);
                Vec2f motion = handleVanillaMotion((float) speed);
                event.setX(motion.x);
                event.setZ(motion.y);
                strafe++;
            }
            else if (speedMode.getValue() == SpeedMode.GAY_HOP)
            {
                if (!MoneyStar.antiCheatManager.hasPassed(100))
                {
                    strafe = 1;
                    return;
                }
                if (strafe == 1 && mc.player.verticalCollision
                        && MovementUtil.isInputtingMovement())
                {
                    speed = 1.25f * base - 0.01f;
                }
                else if (strafe == 2 && mc.player.verticalCollision
                        && MovementUtil.isInputtingMovement())
                {
                    float jump = (isBoxColliding() ? 0.2f : 0.4f) + jumpEffect;
                    event.setY(jump);
                    MoneyStar.movementManager.setMotionY(jump);
                    speed *= 2.149;
                }
                else if (strafe == 3)
                {
                    double moveSpeed = 0.66 * (distance - base);
                    speed = distance - moveSpeed;
                }
                else
                {
                    if (mc.player.isOnGround() && strafe > 0)
                    {
                        if (1.35 * base - 0.01 > speed)
                        {
                            strafe = 0;
                        }
                        else
                        {
                            strafe = MovementUtil.isInputtingMovement() ? 1 : 0;
                        }
                    }
                    speed = distance - distance / FRICTION;
                }
                speed = Math.max(speed, base);
                if (strafe > 0)
                {
                    Vec2f motion = handleStrafeMotion((float) speed);
                    event.setX(motion.x);
                    event.setZ(motion.y);
                }
                strafe++;
            }
            else if (speedMode.getValue() == SpeedMode.V_HOP)
            {
                if (!MoneyStar.antiCheatManager.hasPassed(100))
                {
                    strafe = 1;
                    return;
                }
                if (MathUtil.round(mc.player.getY() - (double) (int) mc.player.getY(), 3) == MathUtil.round(0.4, 3))
                {
                    MoneyStar.movementManager.setMotionY(0.31 + jumpEffect);
                    event.setY(0.31 + jumpEffect);
                }
                else if (MathUtil.round(mc.player.getY() - (double) (int) mc.player.getY(), 3) == MathUtil.round(0.71, 3))
                {
                    MoneyStar.movementManager.setMotionY(0.04 + jumpEffect);
                    event.setY(0.04 + jumpEffect);
                }
                else if (MathUtil.round(mc.player.getY() - (double) (int) mc.player.getY(), 3) == MathUtil.round(0.75, 3))
                {
                    MoneyStar.movementManager.setMotionY(-0.2 - jumpEffect);
                    event.setY(-0.2 - jumpEffect);
                }
                if (!mc.world.isSpaceEmpty(null, mc.player.getBoundingBox().offset(0.0, -0.56, 0.0))
                        && MathUtil.round(mc.player.getY() - (double) (int) mc.player.getY(), 3) == MathUtil.round(0.55, 3))
                {
                    MoneyStar.movementManager.setMotionY(-0.14 + jumpEffect);
                    event.setY(-0.14 + jumpEffect);
                }
                if (strafe != 1 || !mc.player.verticalCollision
                        || mc.player.forwardSpeed == 0.0f && mc.player.sidewaysSpeed == 0.0f)
                {
                    if (strafe != 2 || !mc.player.verticalCollision
                            || mc.player.forwardSpeed == 0.0f && mc.player.sidewaysSpeed == 0.0f)
                    {
                        if (strafe == 3)
                        {
                            double moveSpeed = 0.66 * (distance - base);
                            speed = distance - moveSpeed;
                        }
                        else
                        {
                            if (mc.player.isOnGround() && strafe > 0)
                            {
                                if (1.35 * base - 0.01 > speed)
                                {
                                    strafe = 0;
                                }
                                else
                                {
                                    strafe = MovementUtil.isInputtingMovement() ? 1 : 0;
                                }
                            }
                            speed = distance - distance / FRICTION;
                        }
                    }
                    else
                    {
                        double jump = (isBoxColliding() ? 0.2 : 0.4) + jumpEffect;
                        MoneyStar.movementManager.setMotionY(jump);
                        event.setY(jump);
                        speed *= 2.149;
                    }
                }
                else
                {
                    speed = 2.0 * base - 0.01;
                }
                if (strafe > 8)
                {
                    speed = base;
                }
                speed = Math.max(speed, base);
                Vec2f motion = handleStrafeMotion((float) speed);
                event.setX(motion.x);
                event.setZ(motion.y);
                strafe++;
            }
            else if (speedMode.getValue() == SpeedMode.B_HOP)
            {
                if (!MoneyStar.antiCheatManager.hasPassed(100))
                {
                    strafe = 4;
                    return;
                }
                if (MathUtil.round(mc.player.getY() - ((int) mc.player.getY()), 3) == MathUtil.round(0.138, 3))
                {
                    MoneyStar.movementManager.setMotionY(mc.player.getVelocity().y - (0.08 + jumpEffect));
                    event.setY(event.getY() - (0.0931 + jumpEffect));
                    MoneyStar.newPositionManager.setPositionY(mc.player.getY() - (0.0931 + jumpEffect));
                }
                if (strafe != 2 || mc.player.forwardSpeed == 0.0f && mc.player.sidewaysSpeed == 0.0f)
                {
                    if (strafe == 3)
                    {
                        double moveSpeed = 0.66 * (distance - base);
                        speed = distance - moveSpeed;
                    }
                    else
                    {
                        if (mc.player.isOnGround())
                        {
                            strafe = 1;
                        }
                        speed = distance - distance / FRICTION;
                    }
                }
                else
                {
                    double jump = (isBoxColliding() ? 0.2 : 0.4) + jumpEffect;
                    MoneyStar.movementManager.setMotionY(jump);
                    event.setY(jump);
                    speed *= 2.149;
                }
                speed = Math.max(speed, base);
                Vec2f motion = handleStrafeMotion((float) speed);
                event.setX(motion.x);
                event.setZ(motion.y);
                strafe++;
            }
            else if (speedMode.getValue() == SpeedMode.VANILLA)
            {
                Vec2f motion = handleStrafeMotion(speedPlayer.getValue() / 10.0f);
                event.setX(motion.x);
                event.setZ(motion.y);
            }
        }
    }

    /**
     * @param speed
     * @return
     */
    public Vec2f handleStrafeMotion(final float speed)
    {
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getRenderTickCounter().getTickDelta(true);
        if (forward == 0.0f && strafe == 0.0f)
        {
            return Vec2f.ZERO;
        }
        else if (forward != 0.0f)
        {
            if (strafe >= 1.0f)
            {
                yaw += forward > 0.0f ? -45 : 45;
                strafe = 0.0f;
            }
            else if (strafe <= -1.0f)
            {
                yaw += forward > 0.0f ? 45 : -45;
                strafe = 0.0f;
            }
            if (forward > 0.0f)
            {
                forward = 1.0f;
            }
            else if (forward < 0.0f)
            {
                forward = -1.0f;
            }
        }
        float rx = (float) Math.cos(Math.toRadians(yaw));
        float rz = (float) -Math.sin(Math.toRadians(yaw));
        return new Vec2f((forward * speed * rz) + (strafe * speed * rx),
                (forward * speed * rx) - (strafe * speed * rz));
    }

    public Vec2f handleVanillaMotion(final float speed)
    {
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        if (forward == 0.0f && strafe == 0.0f)
        {
            return Vec2f.ZERO;
        }
        else if (forward != 0.0f && strafe != 0.0f)
        {
            forward *= (float) Math.sin(0.7853981633974483);
            strafe *= (float) Math.cos(0.7853981633974483);
        }
        return new Vec2f((float) (forward * speed * -Math.sin(Math.toRadians(mc.player.getYaw())) + strafe * speed * Math.cos(Math.toRadians(mc.player.getYaw()))),
                (float) (forward * speed * Math.cos(Math.toRadians(mc.player.getYaw())) - strafe * speed * -Math.sin(Math.toRadians(mc.player.getYaw()))));
    }

    @Subscribe
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof ExplosionS2CPacket packet)
        {
            double x = packet.center().getX();
            double z = packet.center().getZ();
            // boostSpeed = Math.sqrt(x * x + z * z);
            // boostTicks = 0;
        }
        else if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet
                && packet.getEntityId() == mc.player.getId())
        {
            double x = packet.getVelocityX();
            double z = packet.getVelocityZ();
            // boostSpeed = Math.sqrt(x * x + z * z);
            // boostTicks = 0;
        }
        else if (event.getPacket() instanceof PlayerPositionLookS2CPacket)
        {
            resetStrafe();
        }
    }

    @Subscribe
    public void onConfigUpdate(ClientEvent event)
    {
        {
            if (timer.getValue())
            {
                prevTimer = Timer.getInstance().isEnabled();
                if (!prevTimer)
                {
                    Timer.getInstance().enable();
                    // Modules.TIMER.setTimer(1.0888f);
                }
            }
            else if (Timer.getInstance().isEnabled())
            {
                Timer.getInstance().resetTimer();
                if (!prevTimer)
                {
                    Timer.getInstance().disable();
                }
            }
        }
    }

    public boolean isBoxColliding()
    {
        return !mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(0.0, 0.21, 0.0));
    }

    public boolean checkIsCollidingEntity(Entity entity)
    {
        return entity != null && entity != mc.player && entity instanceof LivingEntity
                && !(entity instanceof FakePlayerEntity) && !(entity instanceof ArmorStandEntity);
    }

    public void setPrevTimer()
    {
        prevTimer = !prevTimer;
    }

    public boolean isUsingTimer()
    {
        return isEnabled() && timer.getValue();
    }

    public void resetStrafe()
    {
        strafe = 4;
        strictTicks = 0;
        strictFastTicks = 0;
        speed = 0.0f;
        distance = 0.0;
        accel = false;
    }

    public boolean isStrafe()
    {
        return speedMode.getValue() != SpeedMode.GRIM_COLLIDE && speedMode.getValue() != SpeedMode.VANILLA;
    }

    private enum SpeedMode
    {
        STRAFE,
        STRAFE_STRICT,
        STRAFE_B_HOP,
        LOW_HOP,
        GAY_HOP,
        V_HOP,
        B_HOP,
        VANILLA,
        GRIM_COLLIDE
    }
}
