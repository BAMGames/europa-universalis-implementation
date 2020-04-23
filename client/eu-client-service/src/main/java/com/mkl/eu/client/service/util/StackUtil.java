package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.board.IStack;
import com.mkl.eu.client.service.vo.enumeration.MovePhaseEnum;

/**
 * Utility around stacks.
 *
 * @author MKL.
 */
public class StackUtil {

    /**
     * Private constructor.
     */
    private StackUtil() {

    }

    /**
     * Reset a stack at the beginning of a new military round.
     *
     * @param stack the stack to reset.
     */
    public static void resetStack(IStack stack) {
        stack.setMove(0);
        if (isBesieging(stack)) {
            stack.setMovePhase(MovePhaseEnum.STILL_BESIEGING);
        } else {
            stack.setMovePhase(MovePhaseEnum.NOT_MOVED);
        }
    }

    /**
     * @param stack the stack.
     * @return if the stack is moving.
     */
    public static boolean isMoving(IStack stack) {
        return stack.getMovePhase() == MovePhaseEnum.IS_MOVING || stack.getMovePhase() == MovePhaseEnum.IS_MOVING_AGGRESSIVE;
    }

    /**
     * @param stack the stack.
     * @return if the stack has moved.
     */
    public static boolean hasMoved(IStack stack) {
        return stack.getMovePhase() == MovePhaseEnum.MOVED || stack.getMovePhase() == MovePhaseEnum.FIGHTING || stack.getMovePhase() == MovePhaseEnum.BESIEGING;
    }

    /**
     * @param stack the stack.
     * @return if the stack is fighting.
     */
    public static boolean isFighting(IStack stack) {
        return stack.getMovePhase() == MovePhaseEnum.FIGHTING;
    }

    /**
     * @param stack the stack.
     * @return if the stack is besieging.
     */
    public static boolean isBesieging(IStack stack) {
        return stack.getMovePhase() == MovePhaseEnum.BESIEGING || stack.getMovePhase() == MovePhaseEnum.STILL_BESIEGING || stack.getMovePhase() == MovePhaseEnum.LOOTING_BESIEGING;
    }

    /**
     * @param stack the stack.
     * @return if the stack is looting.
     */
    public static boolean isLooting(IStack stack) {
        return stack.getMovePhase() == MovePhaseEnum.LOOTING || stack.getMovePhase() == MovePhaseEnum.LOOTING_BESIEGING;
    }
}
