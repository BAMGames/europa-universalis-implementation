package com.mkl.eu.front.map.vo;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.StringUtils;

/**
 * Border between two provinces.
 *
 * @author MKL
 */
@XStreamAlias("border")
public class Border {
    /** First province (alphabetical order) of the border. */
    @XStreamAlias("first")
    private String first;
    /** Second province (alphabetical order) of the border. */
    @XStreamAlias("second")
    private String second;
    /** Type of border. */
    @XStreamAlias("type")
    private String type;

    /** Constructor. */
    public Border() {
    }

    /** @return the first. */
    public String getFirst() {
        return first;
    }

    /** @param first the first to set. */
    public void setFirst(String first) {
        this.first = first;
    }

    /** @return the second. */
    public String getSecond() {
        return second;
    }

    /** @param second the second to set. */
    public void setSecond(String second) {
        this.second = second;
    }

    /** @return the type. */
    public String getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(String type) {
        this.type = type;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 11 + 13 * first.hashCode() + 15 * second.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        boolean equals = false;

        if (obj instanceof Border) {
            Border border = (Border) obj;

            return StringUtils.equals(first, border.getFirst())
                    && StringUtils.equals(second, border.getSecond());
        }

        return equals;
    }
}
