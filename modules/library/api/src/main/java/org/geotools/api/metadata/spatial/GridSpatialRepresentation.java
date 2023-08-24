/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2004-2005, Open Geospatial Consortium Inc.
 *
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.geotools.api.metadata.spatial;

import static org.geotools.api.annotation.Obligation.MANDATORY;
import static org.geotools.api.annotation.Specification.ISO_19115;

import java.util.List;
import org.geotools.api.annotation.UML;

/**
 * Basic information required to uniquely identify a resource or resources.
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/as#01-111">ISO 19115</A>
 * @author Martin Desruisseaux (IRD)
 * @author Cory Horner (Refractions Research)
 * @since GeoAPI 2.0
 */
public interface GridSpatialRepresentation extends SpatialRepresentation {
    /**
     * Number of independent spatial-temporal axes.
     *
     * @return Number of independent spatial-temporal axes.
     */
    Integer getNumberOfDimensions();

    /**
     * Information about spatial-temporal axis properties.
     *
     * @return Information about spatial-temporal axis properties.
     */
    List<? extends Dimension> getAxisDimensionsProperties();

    /**
     * Identification of grid data as point or cell.
     *
     * @return Identification of grid data as point or cell.
     */
    CellGeometry getCellGeometry();

    /**
     * Indication of whether or not parameters for transformation exists.
     *
     * @return Whether or not parameters for transformation exists.
     */
    @UML(
            identifier = "transformationParameterAvailability",
            obligation = MANDATORY,
            specification = ISO_19115)
    boolean isTransformationParameterAvailable();
}
