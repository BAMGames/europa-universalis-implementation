package com.mkl.eu.service.service.persistence.oe.ref.province;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Box province (military rounds, diplomacy, etc...). Can be europe or rotw.
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_PROVINCE_BOX")
@PrimaryKeyJoinColumn(name = "ID")
public class BoxProvinceEntity extends AbstractProvinceEntity {
}
