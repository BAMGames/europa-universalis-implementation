package com.mkl.eu.client.common.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for CommonUtil.
 *
 * @author MKL
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class CommonUtilTest {
    @Test
    public void testFindFirst() {
        List<String> list = new ArrayList<>();
        list.add("one");
        list.add("two");
        list.add("three");

        String find = CommonUtil.findFirst(list, s -> StringUtils.equals(s, "one"));

        Assert.assertEquals("one", find);

        find = CommonUtil.findFirst(list, s -> StringUtils.equals(s, "four"));

        Assert.assertEquals(null, find);
    }

    @Test
    public void testAdd() {
        Assert.assertEquals(null, CommonUtil.add());
        Assert.assertEquals(null, CommonUtil.add((Integer)null));
        Assert.assertEquals(null, CommonUtil.add(null, null));
        Assert.assertEquals(1, CommonUtil.add(1, null).intValue());
        Assert.assertEquals(2, CommonUtil.add(null, 2).intValue());
        Assert.assertEquals(3, CommonUtil.add(1, 2).intValue());
        Assert.assertEquals(10, CommonUtil.add(1, 2, 3, 4).intValue());
    }

    @Test
    public void testAddSubtractOne() {
        Map<String, Integer> map = new HashMap<>();

        CommonUtil.addOne(null, null);
        CommonUtil.addOne(null, "one");

        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("four", 4);

        CommonUtil.addOne(map, "one");

        Assert.assertEquals(4, map.size());
        Assert.assertEquals(2, map.get("one").intValue());
        Assert.assertEquals(2, map.get("two").intValue());
        Assert.assertEquals(3, map.get("three").intValue());
        Assert.assertEquals(4, map.get("four").intValue());

        CommonUtil.addOne(map, "four");

        Assert.assertEquals(4, map.size());
        Assert.assertEquals(2, map.get("one").intValue());
        Assert.assertEquals(2, map.get("two").intValue());
        Assert.assertEquals(3, map.get("three").intValue());
        Assert.assertEquals(5, map.get("four").intValue());

        CommonUtil.addOne(map, "five");

        Assert.assertEquals(5, map.size());
        Assert.assertEquals(2, map.get("one").intValue());
        Assert.assertEquals(2, map.get("two").intValue());
        Assert.assertEquals(3, map.get("three").intValue());
        Assert.assertEquals(5, map.get("four").intValue());
        Assert.assertEquals(1, map.get("five").intValue());

        CommonUtil.addOne(map, null);

        Assert.assertEquals(6, map.size());
        Assert.assertEquals(2, map.get("one").intValue());
        Assert.assertEquals(2, map.get("two").intValue());
        Assert.assertEquals(3, map.get("three").intValue());
        Assert.assertEquals(5, map.get("four").intValue());
        Assert.assertEquals(1, map.get("five").intValue());
        Assert.assertEquals(1, map.get(null).intValue());

        CommonUtil.subtractOne(null, null);
        CommonUtil.subtractOne(null, "one");
        CommonUtil.subtractOne(map, null);

        Assert.assertEquals(5, map.size());
        Assert.assertEquals(2, map.get("one").intValue());
        Assert.assertEquals(2, map.get("two").intValue());
        Assert.assertEquals(3, map.get("three").intValue());
        Assert.assertEquals(5, map.get("four").intValue());
        Assert.assertEquals(1, map.get("five").intValue());

        CommonUtil.subtractOne(map, "two");

        Assert.assertEquals(5, map.size());
        Assert.assertEquals(2, map.get("one").intValue());
        Assert.assertEquals(1, map.get("two").intValue());
        Assert.assertEquals(3, map.get("three").intValue());
        Assert.assertEquals(5, map.get("four").intValue());
        Assert.assertEquals(1, map.get("five").intValue());

        CommonUtil.subtractOne(map, "two");

        Assert.assertEquals(4, map.size());
        Assert.assertEquals(2, map.get("one").intValue());
        Assert.assertEquals(3, map.get("three").intValue());
        Assert.assertEquals(5, map.get("four").intValue());
        Assert.assertEquals(1, map.get("five").intValue());
    }
}
