/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2004-2005, Open Geospatial Consortium Inc.
 *
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.geotools.api.metadata.distribution;

import static org.geotools.api.annotation.Obligation.OPTIONAL;
import static org.geotools.api.annotation.Specification.ISO_19115;

import java.util.Collection;
import org.geotools.api.annotation.UML;
import org.geotools.api.metadata.citation.ResponsibleParty;

/**
 * Information about the distributor.
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/as#01-111">ISO 19115</A>
 * @author Martin Desruisseaux (IRD)
 * @since GeoAPI 2.0
 */
public interface Distributor {
    /**
     * Party from whom the resource may be obtained. This list need not be exhaustive.
     *
     * @return Party from whom the resource may be obtained.
     */
    ResponsibleParty getDistributorContact();

    /**
     * Provides information about how the resource may be obtained, and related instructions and fee
     * information.
     *
     * @return Information about how the resource may be obtained.
     */
    Collection<? extends StandardOrderProcess> getDistributionOrderProcesses();

    /**
     * Provides information about the format used by the distributor.
     *
     * @return Information about the format used by the distributor.
     */
    Collection<? extends Format> getDistributorFormats();

    /**
     * Provides information about the technical means and media used by the distributor.
     *
     * @return Information about the technical means and media used by the distributor.
     */
    @UML(
            identifier = "distributorTransferOptions",
            obligation = OPTIONAL,
            specification = ISO_19115)
    Collection<? extends DigitalTransferOptions> getDistributorTransferOptions();
}
