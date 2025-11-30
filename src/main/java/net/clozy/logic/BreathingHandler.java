package net.clozy.logic;

import net.clozy.registry.BreathingStyle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.List;

public class BreathingHandler {

    public static void executeMove(ServerPlayerEntity player, BreathingStyle style, int formIndex) {
        if (style == BreathingStyle.WATER) {
            handleWaterBreathing(player, formIndex);
        }
    }

    private static void handleWaterBreathing(ServerPlayerEntity player, int formIndex) {
        // Form 1: Water Surface Slash
        if (formIndex == 0) {
            ServerWorld world = (ServerWorld) player.getWorld();

            // 1. Action Bar Text
            player.sendMessage(Text.literal("Water Breathing, First Form: Water Surface Slash").formatted(Formatting.AQUA), true);

            // 2. Sound Effect
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.0f);

            // 3. Movement (Dash Forward)
            Vec3d lookDir = player.getRotationVector();
            Vec3d dashVec = lookDir.multiply(1.5); // Dash strength
            player.setVelocity(dashVec.x, player.getVelocity().y + 0.2, dashVec.z); // Slight hop + dash
            player.velocityModified = true;

            // 4. Animation (Swing Left Hand)
            player.swingHand(Hand.OFF_HAND, true);

            // 5. Visuals: Expanding Ring at feet
            double px = player.getX();
            double py = player.getY();
            double pz = player.getZ();

            for (int i = 0; i < 360; i += 10) {
                double rad = Math.toRadians(i);
                double x = px + Math.cos(rad) * 1.5;
                double z = pz + Math.sin(rad) * 1.5;
                // Expanding "Poof" ring
                world.spawnParticles(ParticleTypes.POOF, x, py, z, 1, 0, 0, 0, 0.05);
            }

            // 6. Visuals: Horizontal Arc Slash (Water Particles)
            float yaw = player.getYaw();
            // Create an arc in front of the player
            for (int i = -60; i <= 60; i += 5) {
                double rad = Math.toRadians(yaw + i + 90); // +90 to align with MC coordinate system
                double x = px + Math.cos(rad) * 2.0;
                double z = pz + Math.sin(rad) * 2.0;

                // Varied blue colors
                float blueVariant = 0.8f + (world.random.nextFloat() * 0.2f);

                world.spawnParticles(new DustParticleEffect(new Vector3f(0.2f, 0.5f, blueVariant), 1.5f),
                        x, py + 1.2, z, 1, 0, 0, 0, 0); // Chest height

                world.spawnParticles(ParticleTypes.SPLASH, x, py + 1.0, z, 2, 0.1, 0.1, 0.1, 0.1);
            }

            // 7. Damage Logic (Hitbox in front)
            Box hitBox = player.getBoundingBox().expand(2.0, 0.5, 2.0).offset(lookDir.multiply(1.0));
            List<Entity> targets = world.getOtherEntities(player, hitBox);

            for (Entity target : targets) {
                if (target instanceof LivingEntity livingTarget) {
                    livingTarget.damage(player.getDamageSources().playerAttack(player), 8.0f); // Base damage 8
                    // Add knockback
                    livingTarget.takeKnockback(0.5, player.getX() - livingTarget.getX(), player.getZ() - livingTarget.getZ());
                }
            }
        }
    }
}