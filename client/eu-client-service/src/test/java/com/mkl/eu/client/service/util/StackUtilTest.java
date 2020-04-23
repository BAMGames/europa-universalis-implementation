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

    @Test
    public void testMovePhase() {
        for (MovePhaseEnum movePhase : MovePhaseEnum.values()) {
            switch (movePhase) {
                case NOT_MOVED:
                    Assert.assertEquals(false, StackUtil.isMoving(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.hasMoved(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isFighting(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isBesieging(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isLooting(createStack(movePhase)));
                    break;
                case IS_MOVING:
                    Assert.assertEquals(true, StackUtil.isMoving(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.hasMoved(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isFighting(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isBesieging(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isLooting(createStack(movePhase)));
                    break;
                case IS_MOVING_AGGRESSIVE:
                    Assert.assertEquals(true, StackUtil.isMoving(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.hasMoved(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isFighting(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isBesieging(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isLooting(createStack(movePhase)));
                    break;
                case MOVED:
                    Assert.assertEquals(false, StackUtil.isMoving(createStack(movePhase)));
                    Assert.assertEquals(true, StackUtil.hasMoved(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isFighting(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isBesieging(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isLooting(createStack(movePhase)));
                    break;
                case FIGHTING:
                    Assert.assertEquals(false, StackUtil.isMoving(createStack(movePhase)));
                    Assert.assertEquals(true, StackUtil.hasMoved(createStack(movePhase)));
                    Assert.assertEquals(true, StackUtil.isFighting(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isBesieging(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isLooting(createStack(movePhase)));
                    break;
                case BESIEGING:
                    Assert.assertEquals(false, StackUtil.isMoving(createStack(movePhase)));
                    Assert.assertEquals(true, StackUtil.hasMoved(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isFighting(createStack(movePhase)));
                    Assert.assertEquals(true, StackUtil.isBesieging(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isLooting(createStack(movePhase)));
                    break;
                case STILL_BESIEGING:
                    Assert.assertEquals(false, StackUtil.isMoving(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.hasMoved(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isFighting(createStack(movePhase)));
                    Assert.assertEquals(true, StackUtil.isBesieging(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isLooting(createStack(movePhase)));
                    break;
                case LOOTING:
                    Assert.assertEquals(false, StackUtil.isMoving(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.hasMoved(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isFighting(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isBesieging(createStack(movePhase)));
                    Assert.assertEquals(true, StackUtil.isLooting(createStack(movePhase)));
                    break;
                case LOOTING_BESIEGING:
                    Assert.assertEquals(false, StackUtil.isMoving(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.hasMoved(createStack(movePhase)));
                    Assert.assertEquals(false, StackUtil.isFighting(createStack(movePhase)));
                    Assert.assertEquals(true, StackUtil.isBesieging(createStack(movePhase)));
                    Assert.assertEquals(true, StackUtil.isLooting(createStack(movePhase)));
                    break;
                default:
                    Assert.fail("The move Phase " + movePhase + " is not handled by StackUtilTest. Have you changed StackUtil after adding this new movePhase ?");
            }
        }
    }

    private static IStack createStack(MovePhaseEnum movePhase) {
        IStack stack = new Stack();
        stack.setMovePhase(movePhase);
        return stack;
    }
}
