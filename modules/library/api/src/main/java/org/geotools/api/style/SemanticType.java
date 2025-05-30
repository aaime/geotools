/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008, Open Geospatial Consortium Inc.
 *
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.geotools.api.style;

import java.util.ArrayList;
import java.util.List;
import org.geotools.api.util.CodeList;

/**
 * Identifies the more general "type" of geometry that this style is meant to act upon. In the current OGC SE
 * specifications, this is an experimental element and can take only one of the following values:
 *
 * <p>
 *
 * <ul>
 *   <li>{@code generic:point}
 *   <li>{@code generic:line}
 *   <li>{@code generic:polygon}
 *   <li>{@code generic:text}
 *   <li>{@code generic:raster}
 *   <li>{@code generic:any}
 * </ul>
 *
 * <p>
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/symbol">Symbology Encoding Implementation Specification
 *     1.1.0</A>
 * @author Open Geospatial Consortium
 * @author Johann Sorel (Geomatys)
 * @since GeoAPI 2.2
 */
public final class SemanticType extends CodeList<SemanticType> {
    /** Serial number for compatibility with different versions. */
    private static final long serialVersionUID = -7328502367911363577L;

    /** List of all enumerations of this type. Must be declared before any enum declaration. */
    private static final List<SemanticType> VALUES = new ArrayList<>(6);

    /** Semantic identifies a point geometry. */
    public static final SemanticType POINT = new SemanticType("POINT");

    /** Semantic identifies a line geometry. */
    public static final SemanticType LINE = new SemanticType("LINE");

    /** Semantic identifies a polygon geometry. */
    public static final SemanticType POLYGON = new SemanticType("POLYGON");

    /** Semantic identifies a text geometry. */
    public static final SemanticType TEXT = new SemanticType("TEXT");

    /** Semantic identifies a raster geometry. */
    public static final SemanticType RASTER = new SemanticType("RASTER");

    /** Semantic identifies any geometry. */
    public static final SemanticType ANY = new SemanticType("ANY");

    /**
     * Constructs an enum with the given name. The new enum is automatically added to the list returned by
     * {@link #values}.
     *
     * @param name The enum name. This name must not be in use by an other enum of this type.
     */
    public SemanticType(final String name) {
        super(name, VALUES);
    }

    /**
     * Returns the list of {@code SemanticType}s.
     *
     * @return The list of codes declared in the current JVM.
     */
    public static SemanticType[] values() {
        synchronized (VALUES) {
            return VALUES.toArray(new SemanticType[VALUES.size()]);
        }
    }

    /** Returns the list of enumerations of the same kind than this enum. */
    @Override
    public SemanticType[] family() {
        return values();
    }

    /**
     * Returns the semantic type that matches the given string, or returns a new one if none match it.
     *
     * @param code The name of the code to fetch or to create.
     * @return A code matching the given name.
     */
    public static SemanticType valueOf(String code) {
        return valueOf(SemanticType.class, code);
    }
}
