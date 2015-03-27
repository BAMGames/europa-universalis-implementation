package com.mkl.eu.client.service.vo.country;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.board.Stack;

/**
 * Discovery of a province of the ROTW.
 *
 * @author MKL
 */
public class Discovery extends EuObject {
    /** Country the discovery belongs to. */
    private Country country;
    /** Province of the discovery. */
    private String province;
    /** Stack where the discovery is if being rappatried. */
    private Stack owner;
    /** Turn it was rappatried in a national province (<code>null</code> if on going). */
    private Integer turn;
}
