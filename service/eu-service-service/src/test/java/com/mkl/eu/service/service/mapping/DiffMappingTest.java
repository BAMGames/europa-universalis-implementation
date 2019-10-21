package com.mkl.eu.service.service.mapping;

import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.ArrayList;
import java.util.List;

/**
 * Test of GameMapping.
 *
 * @author MKL.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/com/mkl/eu/service/service/mapping/test-eu-mapping-applicationContext..xml"})
public class DiffMappingTest {
    @Autowired
    private DiffMapping diffMapping;


    @Test
    public void testVoidGameMapping() {
        List<Diff> vos = diffMapping.oesToVos(null);

        Assert.assertNull(vos);

        List<DiffEntity> entities = new ArrayList<>();

        vos = diffMapping.oesToVos(entities);

        Assert.assertTrue(vos.isEmpty());
    }

    @Test
    public void testFullGameMapping() {
        List<DiffEntity> entities = createDiffEntities();

        List<Diff> vos = diffMapping.oesToVos(entities);

        ReflectionAssert.assertReflectionEquals(createDiffVos(), vos);
    }

    private List<DiffEntity> createDiffEntities() {
        List<DiffEntity> objects = new ArrayList<>();

        DiffEntity object = new DiffEntity();
        object.setId(1L);
        object.setIdGame(2L);
        object.setVersionGame(1L);
        object.setIdCountry(666l);
        object.setIdObject(12L);
        object.setTypeObject(DiffTypeObjectEnum.STACK);
        object.setType(DiffTypeEnum.MOVE);
        List<DiffAttributesEntity> subObjects = new ArrayList<>();
        DiffAttributesEntity subObject = new DiffAttributesEntity();
        subObject.setId(1L);
        subObject.setDiff(object);
        subObject.setType(DiffAttributeTypeEnum.PROVINCE_FROM);
        subObject.setValue("Pecs");
        subObjects.add(subObject);
        subObject = new DiffAttributesEntity();
        subObject.setId(2L);
        subObject.setDiff(object);
        subObject.setType(DiffAttributeTypeEnum.PROVINCE_TO);
        subObject.setValue("Idf");
        subObjects.add(subObject);
        object.setAttributes(subObjects);
        objects.add(object);

        object = new DiffEntity();
        object.setId(2L);
        object.setIdGame(2L);
        object.setVersionGame(2L);
        object.setIdObject(15L);
        object.setTypeObject(DiffTypeObjectEnum.COUNTER);
        object.setType(DiffTypeEnum.REMOVE);
        objects.add(object);

        object = new DiffEntity();
        object.setId(3L);
        object.setIdGame(2L);
        object.setVersionGame(3L);
        object.setIdObject(13L);
        object.setTypeObject(DiffTypeObjectEnum.COUNTER);
        object.setType(DiffTypeEnum.ADD);
        subObjects = new ArrayList<>();
        subObject = new DiffAttributesEntity();
        subObject.setId(3L);
        subObject.setDiff(object);
        subObject.setType(DiffAttributeTypeEnum.PROVINCE);
        subObject.setValue("Pecs");
        subObjects.add(subObject);
        object.setAttributes(subObjects);
        objects.add(object);

        return objects;
    }

    private List<Diff> createDiffVos() {
        List<Diff> objects = new ArrayList<>();

        Diff object = new Diff();
        object.setVersionGame(1L);
        object.setIdCountry(666l);
        object.setIdObject(12L);
        object.setTypeObject(DiffTypeObjectEnum.STACK);
        object.setType(DiffTypeEnum.MOVE);
        List<DiffAttributes> subObjects = new ArrayList<>();
        DiffAttributes subObject = new DiffAttributes();
        subObject.setType(DiffAttributeTypeEnum.PROVINCE_FROM);
        subObject.setValue("Pecs");
        subObjects.add(subObject);
        subObject = new DiffAttributes();
        subObject.setType(DiffAttributeTypeEnum.PROVINCE_TO);
        subObject.setValue("Idf");
        subObjects.add(subObject);
        object.setAttributes(subObjects);
        objects.add(object);

        object = new Diff();
        object.setVersionGame(2L);
        object.setIdObject(15L);
        object.setTypeObject(DiffTypeObjectEnum.COUNTER);
        object.setType(DiffTypeEnum.REMOVE);
        objects.add(object);

        object = new Diff();
        object.setVersionGame(3L);
        object.setIdObject(13L);
        object.setTypeObject(DiffTypeObjectEnum.COUNTER);
        object.setType(DiffTypeEnum.ADD);
        subObjects = new ArrayList<>();
        subObject = new DiffAttributes();
        subObject.setType(DiffAttributeTypeEnum.PROVINCE);
        subObject.setValue("Pecs");
        subObjects.add(subObject);
        object.setAttributes(subObjects);
        objects.add(object);

        return objects;
    }
}
