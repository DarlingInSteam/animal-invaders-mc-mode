package shadowshiftstudio.animalinvaders.entity.utils;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class EntityUtils {
    /**
     * Применяет вращение головы к модели сущности
     */
    public static void applyHeadRotation(ModelPart head, float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 45.0F);

        head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        head.xRot = pHeadPitch * ((float)Math.PI / 180F);
    }
    
    /**
     * Обновляет состояние анимации ходьбы на основе позы и скорости движения
     */
    public static void updateWalkAnimation(LivingEntity entity, float partialTicks) {
        float f;
        if (entity.getPose() == Pose.STANDING) {
            f = Math.min(partialTicks * 6.0F, 1.0F);
        } else {
            f = 0.0F;
        }

        entity.walkAnimation.update(f, 0.2F);
    }
    
    /**
     * Управляет анимацией простоя (idle), начиная ее, если таймаут истек
     */
    public static void setupIdleAnimation(AnimationState idleAnimationState, int tickCount, int[] timeoutAnimationState) {
        if (timeoutAnimationState[0] <= 0) {
            timeoutAnimationState[0] = (int)(Math.random() * 40 + 80); // Случайный таймаут между 80 и 120 тиков
            idleAnimationState.start(tickCount);
        } else {
            timeoutAnimationState[0]--;
        }
    }
    
    /**
     * Останавливает одну анимацию и запускает другую
     */
    public static void switchAnimation(AnimationState stopAnimation, AnimationState startAnimation, int tickCount) {
        stopAnimation.stop();
        startAnimation.start(tickCount);
    }
    
    /**
     * Управляет анимациями движения на основе скорости и состояния сущности
     * @param isRunning состояние бега
     * @param entity сущность
     * @param runAnimState анимация бега
     * @param walkAnimState анимация ходьбы
     * @param idleAnimState анимация простоя
     * @param tickCount текущий тик
     */
    public static void handleMovementAnimations(boolean isRunning, LivingEntity entity, 
                                                AnimationState runAnimState, AnimationState walkAnimState, 
                                                AnimationState idleAnimState, int tickCount) {
        if (isRunning && entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
            runAnimState.startIfStopped(tickCount);
            walkAnimState.stop();
            idleAnimState.stop();
        } else if (entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
            walkAnimState.startIfStopped(tickCount);
            runAnimState.stop();
            idleAnimState.stop();
        } else {
            idleAnimState.startIfStopped(tickCount);
            walkAnimState.stop();
            runAnimState.stop();
        }
    }
    
    /**
     * Обрабатывает таймауты для анимаций с обратным отсчетом
     * @param timeoutValue текущее значение таймаута
     * @param callback функция, вызываемая при истечении таймаута
     * @return обновленное значение таймаута
     */
    public static int handleAnimationTimeout(int timeoutValue, Runnable callback) {
        if (timeoutValue > 0) {
            timeoutValue--;
            if (timeoutValue == 0 && callback != null) {
                callback.run();
            }
        }
        return timeoutValue;
    }
}
