package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.filter;

import m4jdsl.NormallyDistributedThinkTime;
import m4jdsl.ThinkTime;

/**
 * <code>ThinkTime</code> formatter for normally distributed think times.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class NormallyDistributedThinkTimeFormatter
extends AbstractThinkTimeFormatter {

    /** Template <code>String</code> for being filled with actual values. */
    private final static String FORMAT_TEMPLATE = "norm(%s %s)";


    /* **************************  public methods  ************************** */


    /**
     * {@inheritDoc}
     * <p> This method is specialized for <b>normally distributed think
     * times</b>.
     */
    @Override
    public String getThinkTimeString () {

        return this.getThinkTimeString(0.0d, 0.0d);
    }

    /**
     * {@inheritDoc}
     * <p> This method is specialized for <b>normally distributed think
     * times</b>.
     */
    @Override
    public String getThinkTimeString (final ThinkTime thinkTime) {

        final String thinkTimeString;  // to be returned;

        if (thinkTime instanceof NormallyDistributedThinkTime) {

            final NormallyDistributedThinkTime normallyDistributedThinkTime =
                    (NormallyDistributedThinkTime) thinkTime;

            thinkTimeString = this.getThinkTimeString(
                    normallyDistributedThinkTime.getMean(),
                    normallyDistributedThinkTime.getDeviation());

        } else {

            thinkTimeString = null;
        }

        return thinkTimeString;
    }


    /* **************************  private methods  ************************* */


    /**
     * Returns a <code>String</code> representation for a given mean/deviation
     * pair.
     *
     * @param mean       mean value of a normally distributed think time.
     * @param deviation  deviation value of a normally distributed think time.
     *
     * @return  a valid <code>String</code> instance.
     */
    private String getThinkTimeString (
            final double mean,
            final double deviation) {

        return String.format(
                NormallyDistributedThinkTimeFormatter.FORMAT_TEMPLATE,
                this.formatDouble(mean),
                this.formatDouble(deviation));
    }


}