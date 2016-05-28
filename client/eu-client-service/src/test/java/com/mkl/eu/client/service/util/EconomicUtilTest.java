package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.InvestmentEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Unit tests for EconomicUtil.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class EconomicUtilTest {
    @Test
    public void testAdminActionCost() {
        Assert.assertEquals(null, EconomicUtil.getAdminActionCost(null, null));
        Assert.assertEquals(null, EconomicUtil.getAdminActionCost(AdminActionTypeEnum.FTI, null));
        Assert.assertEquals(null, EconomicUtil.getAdminActionCost(null, InvestmentEnum.S));
        Assert.assertEquals(null, EconomicUtil.getAdminActionCost(AdminActionTypeEnum.EXL, InvestmentEnum.S));

        Assert.assertEquals(new Integer(10), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.TFI, InvestmentEnum.S));
        Assert.assertEquals(new Integer(30), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.TFI, InvestmentEnum.M));
        Assert.assertEquals(new Integer(50), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.TFI, InvestmentEnum.L));

        Assert.assertEquals(new Integer(10), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.TP, InvestmentEnum.S));
        Assert.assertEquals(new Integer(30), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.TP, InvestmentEnum.M));
        Assert.assertEquals(new Integer(50), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.TP, InvestmentEnum.L));

        Assert.assertEquals(new Integer(10), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.TFC, InvestmentEnum.S));
        Assert.assertEquals(new Integer(30), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.TFC, InvestmentEnum.M));
        Assert.assertEquals(new Integer(50), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.TFC, InvestmentEnum.L));

        Assert.assertEquals(new Integer(10), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.ERC, InvestmentEnum.S));
        Assert.assertEquals(new Integer(30), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.ERC, InvestmentEnum.M));
        Assert.assertEquals(new Integer(50), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.ERC, InvestmentEnum.L));

        Assert.assertEquals(new Integer(30), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.DTI, InvestmentEnum.S));
        Assert.assertEquals(new Integer(50), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.DTI, InvestmentEnum.M));
        Assert.assertEquals(new Integer(100), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.DTI, InvestmentEnum.L));

        Assert.assertEquals(new Integer(30), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.FTI, InvestmentEnum.S));
        Assert.assertEquals(new Integer(50), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.FTI, InvestmentEnum.M));
        Assert.assertEquals(new Integer(100), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.FTI, InvestmentEnum.L));

        Assert.assertEquals(new Integer(30), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.COL, InvestmentEnum.S));
        Assert.assertEquals(new Integer(50), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.COL, InvestmentEnum.M));
        Assert.assertEquals(new Integer(100), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.COL, InvestmentEnum.L));

        Assert.assertEquals(new Integer(30), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.MNU, InvestmentEnum.S));
        Assert.assertEquals(new Integer(50), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.MNU, InvestmentEnum.M));
        Assert.assertEquals(new Integer(100), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.MNU, InvestmentEnum.L));

        Assert.assertEquals(new Integer(30), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.ELT, InvestmentEnum.S));
        Assert.assertEquals(new Integer(50), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.ELT, InvestmentEnum.M));
        Assert.assertEquals(new Integer(100), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.ELT, InvestmentEnum.L));

        Assert.assertEquals(new Integer(30), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.ENT, InvestmentEnum.S));
        Assert.assertEquals(new Integer(50), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.ENT, InvestmentEnum.M));
        Assert.assertEquals(new Integer(100), EconomicUtil.getAdminActionCost(AdminActionTypeEnum.ENT, InvestmentEnum.L));
    }

    @Test
    public void testAdminActionBonus() {
        Assert.assertEquals(null, EconomicUtil.getAdminActionColumnBonus(null, null));
        Assert.assertEquals(null, EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.FTI, null));
        Assert.assertEquals(null, EconomicUtil.getAdminActionColumnBonus(null, InvestmentEnum.S));
        Assert.assertEquals(null, EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.EXL, InvestmentEnum.S));

        Assert.assertEquals(new Integer(0), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.TFI, InvestmentEnum.S));
        Assert.assertEquals(new Integer(1), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.TFI, InvestmentEnum.M));
        Assert.assertEquals(new Integer(3), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.TFI, InvestmentEnum.L));

        Assert.assertEquals(new Integer(0), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.TP, InvestmentEnum.S));
        Assert.assertEquals(new Integer(1), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.TP, InvestmentEnum.M));
        Assert.assertEquals(new Integer(3), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.TP, InvestmentEnum.L));

        Assert.assertEquals(new Integer(0), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.TFC, InvestmentEnum.S));
        Assert.assertEquals(new Integer(1), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.TFC, InvestmentEnum.M));
        Assert.assertEquals(new Integer(3), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.TFC, InvestmentEnum.L));

        Assert.assertEquals(new Integer(0), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.ERC, InvestmentEnum.S));
        Assert.assertEquals(new Integer(1), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.ERC, InvestmentEnum.M));
        Assert.assertEquals(new Integer(3), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.ERC, InvestmentEnum.L));

        Assert.assertEquals(new Integer(0), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.DTI, InvestmentEnum.S));
        Assert.assertEquals(new Integer(1), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.DTI, InvestmentEnum.M));
        Assert.assertEquals(new Integer(3), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.DTI, InvestmentEnum.L));

        Assert.assertEquals(new Integer(0), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.FTI, InvestmentEnum.S));
        Assert.assertEquals(new Integer(1), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.FTI, InvestmentEnum.M));
        Assert.assertEquals(new Integer(3), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.FTI, InvestmentEnum.L));

        Assert.assertEquals(new Integer(0), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.COL, InvestmentEnum.S));
        Assert.assertEquals(new Integer(1), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.COL, InvestmentEnum.M));
        Assert.assertEquals(new Integer(3), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.COL, InvestmentEnum.L));

        Assert.assertEquals(new Integer(0), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.MNU, InvestmentEnum.S));
        Assert.assertEquals(new Integer(1), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.MNU, InvestmentEnum.M));
        Assert.assertEquals(new Integer(3), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.MNU, InvestmentEnum.L));

        Assert.assertEquals(new Integer(0), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.ELT, InvestmentEnum.S));
        Assert.assertEquals(new Integer(1), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.ELT, InvestmentEnum.M));
        Assert.assertEquals(new Integer(3), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.ELT, InvestmentEnum.L));

        Assert.assertEquals(new Integer(0), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.ENT, InvestmentEnum.S));
        Assert.assertEquals(new Integer(1), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.ENT, InvestmentEnum.M));
        Assert.assertEquals(new Integer(3), EconomicUtil.getAdminActionColumnBonus(AdminActionTypeEnum.ENT, InvestmentEnum.L));
    }
}
