package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.board.IStack;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.enumeration.MovePhaseEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Unit tests for StackUtil.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class StackUtilTest {


    @Test
    public void testResetStack() {
        StackBuilder.create()
                .whenResetStack()
                .thenExpect(MovePhaseEnum.NOT_MOVED);
        StackBuilder.create().move(2)
                .whenResetStack()
                .thenExpect(MovePhaseEnum.NOT_MOVED);
        StackBuilder.create().move(2).movePhase(MovePhaseEnum.MOVED)
                .whenResetStack()
                .thenExpect(MovePhaseEnum.NOT_MOVED);
        StackBuilder.create().move(2).movePhase(MovePhaseEnum.BESIEGING)
                .whenResetStack()
                .thenExpect(MovePhaseEnum.STILL_BESIEGING);
        StackBuilder.create().move(2).movePhase(MovePhaseEnum.STILL_BESIEGING)
                .whenResetStack()
                .thenExpect(MovePhaseEnum.STILL_BESIEGING);
        StackBuilder.create().move(2).movePhase(MovePhaseEnum.FIGHTING)
                .whenResetStack()
                .thenExpect(MovePhaseEnum.NOT_MOVED);
        StackBuilder.create().move(2).movePhase(MovePhaseEnum.IS_MOVING)
                .whenResetStack()
                .thenExpect(MovePhaseEnum.NOT_MOVED);
        StackBuilder.create().move(2).movePhase(MovePhaseEnum.LOOTING)
                .whenResetStack()
                .thenExpect(MovePhaseEnum.NOT_MOVED);
        StackBuilder.create().move(2).movePhase(MovePhaseEnum.LOOTING_BESIEGING)
                .whenResetStack()
                .thenExpect(MovePhaseEnum.STILL_BESIEGING);
    }

    static class StackBuilder {
        Integer move;
        MovePhaseEnum movePhase;
        IStack stack;

        static StackBuilder create() {
            return new StackBuilder();
        }

        StackBuilder move(Integer move) {
            this.move = move;
            return this;
        }

        StackBuilder movePhase(MovePhaseEnum movePhase) {
            this.movePhase = movePhase;
            return this;
        }

        StackBuilder whenResetStack() {
            stack = new Stack();
            if (move != null) {
                stack.setMove(move);
            }
            stack.setMovePhase(movePhase);

            StackUtil.resetStack(stack);

            return this;
        }

        StackBuilder thenExpect(MovePhaseEnum result) {
            Assert.assertEquals("The stack did not have its move points reset.", 0, stack.getMove());
            Assert.assertEquals("The stack had the wrong move phase after reset.", result, stack.getMovePhase());

            return this;
        }
    }
}
