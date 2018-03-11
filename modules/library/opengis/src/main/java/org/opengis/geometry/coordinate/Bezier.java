/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2005, Open Geospatial Consortium Inc.
 *
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.geometry.coordinate;

import org.opengis.annotation.UML;

import static org.opengis.annotation.Specification.*;


/**
 * Polynomial splines that use Bezier or Bernstein polynomials for interpolation
 * purposes. An <var>n</var>-long control point array shall create a polynomial
 * curve of degree <var>n</var> that defines the entire curve segment. These curves
 * are defined in terms of a set of basis functions called the Bézier or Bernstein
 * polynomials.
 *
 * @author Martin Desruisseaux (IRD)
 * @version <A HREF="http://www.opengeospatial.org/standards/as">ISO 19107</A>
 * @source $URL$
 * @todo Add equations from ISO 19107 to the javadoc.
 * @since GeoAPI 2.0
 */
@UML(identifier = "GM_Bezier", specification = ISO_19107)
public interface Bezier extends BSplineCurve {
}
