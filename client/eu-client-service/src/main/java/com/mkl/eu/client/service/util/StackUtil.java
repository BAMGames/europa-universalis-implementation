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
        if (stack.getMovePhase() != null && stack.getMovePhase().isBesieging()) {
            stack.setMovePhase(MovePhaseEnum.STILL_BESIEGING);
        } else {
            stack.setMovePhase(MovePhaseEnum.NOT_MOVED);
        }
    }
}
