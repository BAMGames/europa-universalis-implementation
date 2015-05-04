package com.mkl.eu.service.service.persistence.oe.ref.province;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * European province. A tile on the european board that can contains counters.
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_PROVINCE_ROTW")
@PrimaryKeyJoinColumn(name = "ID")
public class RotwProvinceEntity extends AbstractProvinceEntity {
}
