/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.epsg.wkt;

// J2SE dependencies

import static org.junit.Assert.assertNotNull;

import java.util.Set;
import org.geotools.api.metadata.citation.Citation;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.test.TestData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * These EPSG support.
 *
 * @author Jody Garnett
 * @since 2.1.M3
 * @version 2.1.M3
 */
public class EPSGTest {
    EPSGCRSAuthorityFactory factory;
    /*
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        factory = new EPSGCRSAuthorityFactory();
    }

    @Test
    public void testAuthority() {
        Citation authority = factory.getAuthority();

        assertNotNull(authority);
        Assert.assertEquals(
                "European Petroleum Survey Group", authority.getTitle().toString());
        Assert.assertTrue(Citations.identifierMatches(authority, "EPSG"));
    }

    @Test
    public void testVendor() {
        Citation vendor = factory.getVendor();
        assertNotNull(vendor);
        Assert.assertEquals("Geotools", vendor.getTitle().toString());
    }

    @Test
    public void testCodes() throws Exception {
        Set codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);

        assertNotNull(codes);
        Assert.assertEquals(7840, codes.size());
    }

    /** A random CRS for fun. */
    @Test
    public void test26910() throws Exception {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) factory.createObject("EPSG:26910");
        assertNotNull(crs);
    }

    /** UDIG requires this to work */
    @Test
    public void test4326() throws Exception {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) factory.createObject("EPSG:4326");
        assertNotNull(crs);
    }
    /** UDIG requires this to work */
    @Test
    public void test4269() throws Exception {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) factory.createObject("EPSG:4269");
        assertNotNull(crs);
    }
    /** UDIG requires this to work */
    @Test
    public void test42102() throws Exception {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) factory.createObject("EPSG:42102");
        assertNotNull(crs);
        assertNotNull(crs.getIdentifiers());
        Assert.assertFalse(crs.getIdentifiers().isEmpty());
        NamedIdentifier expected = new NamedIdentifier(Citations.EPSG, "42102");
        Assert.assertTrue(crs.getIdentifiers().contains(expected));
    }

    @Test
    public void testDecodeAll() throws Exception {
        // this test is heavy, only enable with extensive tests
        if (!TestData.isExtensiveTest()) {
            return;
        }

        Set codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);

        for (Object o : codes) {
            String code = (String) o;
            try {
                assertNotNull(factory.createObject(code));
            } catch (Exception e) {
                java.util.logging.Logger.getGlobal().log(java.util.logging.Level.INFO, "", e);
                Assert.fail("Failed to decode " + code + " with error: " + e.getMessage());
            }
        }
    }

    /** A random CRS for fun. */
    @Test
    public void test26910Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26910");
        assertNotNull(crs);
    }

    /** A random CRS for fun. */
    @Test
    public void test26986Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26986");
        assertNotNull(crs);
    }

    /** wfs requires this to work */
    @Test
    public void test4326Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:4326");
        assertNotNull(crs);
    }
    /** wfs requires this to work */
    @Test
    public void test26742Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26742");
        assertNotNull(crs);
    }
    /** wfs requires this to work */
    @Test
    public void test4269Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:4269");
        assertNotNull(crs);
    }
    /** wfs requires this to work */
    @Test
    public void test42304Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:42304");
        assertNotNull(crs);
    }
    /** wfs requires this to work */
    @Test
    public void test42102Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:42102");
        assertNotNull(crs);
        assertNotNull(crs.getIdentifiers());
        Assert.assertFalse(crs.getIdentifiers().isEmpty());
        NamedIdentifier expected = new NamedIdentifier(Citations.EPSG, "42102");
        Assert.assertTrue(crs.getIdentifiers().contains(expected));
    }
}
