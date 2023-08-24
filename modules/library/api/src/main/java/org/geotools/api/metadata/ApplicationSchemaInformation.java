/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2004-2005, Open Geospatial Consortium Inc.
 *
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.geotools.api.metadata;

import static org.geotools.api.annotation.Obligation.OPTIONAL;
import static org.geotools.api.annotation.Specification.ISO_19115;

import java.net.URI;
import org.geotools.api.annotation.UML;
import org.geotools.api.metadata.citation.Citation;

/**
 * Information about the application schema used to build the dataset.
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/as#01-111">ISO 19115</A>
 * @author Martin Desruisseaux (IRD)
 * @since GeoAPI 2.0
 */
public interface ApplicationSchemaInformation {
    /**
     * Name of the application schema used.
     *
     * @return Name of the application schema.
     */
    Citation getName();

    /**
     * Identification of the schema language used.
     *
     * @return The schema language used.
     */
    String getSchemaLanguage();

    /**
     * Formal language used in Application Schema.
     *
     * @return Formal language used in Application Schema.
     */
    String getConstraintLanguage();

    /**
     * Full application schema given as an ASCII file.
     *
     * @return Application schema as an ASCII file.
     * @todo In UML, the type was {@code CharacterString}. It is not clear if it should be the file
     *     name or the file content.
     */
    URI getSchemaAscii();

    /**
     * Full application schema given as a graphics file.
     *
     * @return Application schema as a graphics file.
     */
    URI getGraphicsFile();

    /**
     * Full application schema given as a software development file.
     *
     * @return Application schema as a software development file.
     * @todo In UML, the type was {@code binary}. It is not clear if it was intented to be the file
     *     content.
     */
    URI getSoftwareDevelopmentFile();

    /**
     * Software dependent format used for the application schema software dependent file.
     *
     * @return Format used for the application schema software file.
     */
    @UML(
            identifier = "softwareDevelopmentFileFormat",
            obligation = OPTIONAL,
            specification = ISO_19115)
    String getSoftwareDevelopmentFileFormat();
}
