package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.tables.AttritionLandEurope;
import com.mkl.eu.client.service.vo.tables.AttritionOther;
import com.mkl.eu.client.service.vo.tables.Tables;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.function.Supplier;

/**
 * Unit tests for TablesUtil.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class TablesUtilTest {

    @Test
    public void testGetResult() {
        Tables tables = new Tables();

        Assert.assertEquals(null, TablesUtil.getResult(tables.getAttritionsOther(), 7));

        tables.getAttritionsOther().add(createAttrition(7, 20));

        Assert.assertEquals(20, TablesUtil.getResult(tables.getAttritionsOther(), 7).getLossPercentage().intValue());
        Assert.assertEquals(20, TablesUtil.getResult(tables.getAttritionsOther(), 9).getLossPercentage().intValue());
        Assert.assertEquals(20, TablesUtil.getResult(tables.getAttritionsOther(), 5).getLossPercentage().intValue());

        tables.getAttritionsOther().add(createAttrition(9, 40));
        tables.getAttritionsOther().add(createAttrition(11, 60));

        Assert.assertEquals(20, TablesUtil.getResult(tables.getAttritionsOther(), 6).getLossPercentage().intValue());
        Assert.assertEquals(20, TablesUtil.getResult(tables.getAttritionsOther(), 7).getLossPercentage().intValue());
        tables.getAttritionsOther().add(createAttrition(8, 20));
        Assert.assertEquals(40, TablesUtil.getResult(tables.getAttritionsOther(), 9).getLossPercentage().intValue());
        tables.getAttritionsOther().add(createAttrition(10, 20));
        Assert.assertEquals(60, TablesUtil.getResult(tables.getAttritionsOther(), 11).getLossPercentage().intValue());
        Assert.assertEquals(60, TablesUtil.getResult(tables.getAttritionsOther(), 12).getLossPercentage().intValue());

        tables.getAttritionsLandEurope().add(createAttrition(10, 2, 3, 0));
        tables.getAttritionsLandEurope().add(createAttrition(11, 2, 3, 1));
        tables.getAttritionsLandEurope().add(createAttrition(8, 3, 6, 0));
        tables.getAttritionsLandEurope().add(createAttrition(9, 3, 6, 1));
        tables.getAttritionsLandEurope().add(createAttrition(8, 6, 9, 1));
        tables.getAttritionsLandEurope().add(createAttrition(9, 6, 9, 2));

        Assert.assertEquals(0, TablesUtil.getResult(tables.getAttritionsLandEurope(), 10,
                item -> item.getMinSize() <= 2 && item.getMaxSize() > 2).getLoss().intValue());
        Assert.assertEquals(1, TablesUtil.getResult(tables.getAttritionsLandEurope(), 11,
                item -> item.getMinSize() <= 2 && item.getMaxSize() > 2).getLoss().intValue());
        Assert.assertEquals(0, TablesUtil.getResult(tables.getAttritionsLandEurope(), 8,
                item -> item.getMinSize() <= 3 && item.getMaxSize() > 3).getLoss().intValue());
        Assert.assertEquals(1, TablesUtil.getResult(tables.getAttritionsLandEurope(), 9,
                item -> item.getMinSize() <= 3 && item.getMaxSize() > 3).getLoss().intValue());
        Assert.assertEquals(1, TablesUtil.getResult(tables.getAttritionsLandEurope(), 8,
                item -> item.getMinSize() <= 6 && item.getMaxSize() > 6).getLoss().intValue());
        Assert.assertEquals(2, TablesUtil.getResult(tables.getAttritionsLandEurope(), 9,
                item -> item.getMinSize() <= 6 && item.getMaxSize() > 6).getLoss().intValue());
    }

    private static AttritionOther createAttrition(int dice, int percentage) {
        AttritionOther object = new AttritionOther();

        object.setDice(dice);
        object.setLossPercentage(percentage);

        return object;
    }

    private static AttritionLandEurope createAttrition(int dice, int min, int max, int loss) {
        AttritionLandEurope object = new AttritionLandEurope();

        object.setDice(dice);
        object.setMinSize(min);
        object.setMaxSize(max);
        object.setLoss(loss);

        return object;
    }

    @Test
    public void testGetAttritionOtherCasualtiesInThird() {
        Supplier<Boolean> additionalCasualty = () -> true;
        Supplier<Boolean> noAdditionalCasualty = () -> false;

        Assert.assertEquals(2, TablesUtil.getAttritionOtherCasualtiesInThird(20, 10, additionalCasualty));
        Assert.assertEquals(2, TablesUtil.getAttritionOtherCasualtiesInThird(20, 10, noAdditionalCasualty));

        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(1, 10, additionalCasualty));
        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(2, 10, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(3, 10, additionalCasualty));
        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(3, 10, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(4, 10, additionalCasualty));
        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(4, 10, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(5, 10, additionalCasualty));
        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(5, 10, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(6, 10, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(6, 10, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(7, 10, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(7, 10, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(8, 10, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(8, 10, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(9, 10, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(9, 10, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(10, 10, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(10, 10, noAdditionalCasualty));

        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(1, 10, additionalCasualty));
        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(1, 20, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 30, additionalCasualty));
        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(1, 30, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 40, additionalCasualty));
        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(1, 40, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 50, additionalCasualty));
        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(1, 50, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 60, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 60, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 70, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 70, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 80, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 80, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 90, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 90, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 100, additionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(1, 100, noAdditionalCasualty));

        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(2, 10, additionalCasualty));
        Assert.assertEquals(0, TablesUtil.getAttritionOtherCasualtiesInThird(2, 20, noAdditionalCasualty));
        Assert.assertEquals(1, TablesUtil.getAttritionOtherCasualtiesInThird(2, 20, additionalCasualty));
    }
}
