package org.jgh.emergencybreakthrough.tests

import junit.framework.Assert._
import _root_.android.test.AndroidTestCase

class UnitTests extends AndroidTestCase {
  def testPackageIsCorrect {
    assertEquals("org.jgh.emergencybreakthrough", getContext.getPackageName)
  }
}