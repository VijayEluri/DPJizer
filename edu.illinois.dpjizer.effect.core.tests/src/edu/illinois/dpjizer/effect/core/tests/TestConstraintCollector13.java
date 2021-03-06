/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.dpjizer.effect.core.tests;

import org.junit.Test;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class TestConstraintCollector13 extends DPJInferencerTestCase {
	@Test
	public void testOne() throws Throwable {
		compareCollectedConstraints();
	}

	@Test
	public void testTwo() throws Throwable {
		compareCollectedConstraints();
	}

	@Override
	protected String getTestDir() {
		return "13-method-region-params/";
	}

}
