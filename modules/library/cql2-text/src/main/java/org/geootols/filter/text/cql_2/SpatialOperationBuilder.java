/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geootols.filter.text.cql_2;

import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.Expression;
import org.geotools.api.filter.spatial.BBOX;
import org.geotools.api.filter.spatial.BinarySpatialOperator;
import org.geotools.api.filter.spatial.Contains;
import org.geotools.api.filter.spatial.Crosses;
import org.geotools.api.filter.spatial.Disjoint;
import org.geotools.api.filter.spatial.Equals;
import org.geotools.api.filter.spatial.Intersects;
import org.geotools.api.filter.spatial.Overlaps;
import org.geotools.api.filter.spatial.Touches;
import org.geotools.api.filter.spatial.Within;
import org.geotools.filter.text.commons.BuildResultStack;
import org.geotools.filter.text.cql2.CQLException;

/**
 * Builds an instance of one {@link BinarySpatialOperator} subclass. Copied from gt-cql, should be removed once the
 * modules * are merged.
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
class SpatialOperationBuilder {

    private final BuildResultStack resultStack;
    private final FilterFactory filterFactory;

    public SpatialOperationBuilder(BuildResultStack resultStack, FilterFactory filterFactory) {
        assert resultStack != null;
        assert filterFactory != null;

        this.resultStack = resultStack;
        this.filterFactory = filterFactory;
    }

    protected final BuildResultStack getResultStack() {
        return resultStack;
    }

    protected final FilterFactory getFilterFactory() {
        return filterFactory;
    }

    /**
     * Retrieve the parameters of spatial operation from stack result
     *
     * @return Expression array with the parameters in the natural order
     */
    private Expression[] buildParameters() throws CQLException {

        Expression[] params = new Expression[2];

        params[1] = resultStack.popExpression();

        params[0] = resultStack.popExpression();

        return params;
    }

    protected BinarySpatialOperator buildFilter(Expression expr1, Expression expr2) {
        throw new UnsupportedOperationException("must be implemented");
    }

    /** @return new instance of {@link Contains} operation */
    protected Contains buildContains() throws CQLException {

        Expression[] params = buildParameters();

        return getFilterFactory().contains(params[0], params[1]);
    }

    /** @return new instance of {@link Equals} operation */
    public Equals buildEquals() throws CQLException {

        Expression[] params = buildParameters();

        return getFilterFactory().equal(params[0], params[1]);
    }

    /** @return new instance of {@link Disjoint} operation */
    public Disjoint buildDisjoint() throws CQLException {

        Expression[] params = buildParameters();

        return getFilterFactory().disjoint(params[0], params[1]);
    }

    /** @return new instance of {@link Intersects} operation */
    public Intersects buildIntersects() throws CQLException {
        Expression[] params = buildParameters();

        return getFilterFactory().intersects(params[0], params[1]);
    }

    /** @return new instance of {@link Touches} operation */
    public Touches buildTouches() throws CQLException {

        Expression[] params = buildParameters();

        return getFilterFactory().touches(params[0], params[1]);
    }

    /** @return new instance of {@link Crosses} operation */
    public Crosses buildCrosses() throws CQLException {

        Expression[] params = buildParameters();

        return getFilterFactory().crosses(params[0], params[1]);
    }

    /** @return new instance of {@link Within} operation */
    public Within buildWithin() throws CQLException {

        Expression[] params = buildParameters();

        return getFilterFactory().within(params[0], params[1]);
    }

    /** @return new instance of {@link Within} operation */
    public Overlaps buildOverlaps() throws CQLException {

        Expression[] params = buildParameters();

        return getFilterFactory().overlaps(params[0], params[1]);
    }

    /**
     * Builds a bbox using the stack subproducts
     *
     * @return {@link BBOX}}
     */
    public Filter buildBBoxWithCRS() throws CQLException {
        String crs = getResultStack().popStringValue();
        assert crs != null;
        return buildBBox(crs);
    }

    /**
     * Builds a bbox using the stack subproducts
     *
     * @return {@link BBOX}}
     */
    public Filter buildBBox() throws CQLException {
        return buildBBox(null);
    }

    /**
     * build a bbox using the stack subproducts and the crs parameter
     *
     * @return {@link BBOX}}
     */
    private Filter buildBBox(final String crs) throws CQLException {

        double maxY = getResultStack().popDoubleValue();
        double maxX = getResultStack().popDoubleValue();
        double minY = getResultStack().popDoubleValue();
        double minX = getResultStack().popDoubleValue();

        Expression expr = getResultStack().popExpression();

        FilterFactory ff = getFilterFactory();

        if (isWrappingDateline(minX, maxX)) {
            BBOX bbox1 = ff.bbox(expr, minX, minY, 180, maxY, crs);
            BBOX bbox2 = ff.bbox(expr, -180, minY, maxX, maxY, crs);
            return ff.and(bbox1, bbox2);
        }

        BBOX bbox = ff.bbox(expr, minX, minY, maxX, maxY, crs);
        return bbox;
    }

    /**
     * From the spec: In cases where the bounding box spans the antimeridian of a geographic coordinate reference
     * system, the lower-left value (west-most box edge) is larger than the // upper-right value (east-most box edge).
     * Since with CQL2 we won't get the CRS (it's provided in a separate request parameter, not part of the CQL2 text),
     * we'll just assume it's a geographic CRS
     */
    static boolean isWrappingDateline(double minLon, double maxLon) {
        return minLon > maxLon && validLongitude(minLon) && validLongitude(maxLon);
    }

    /**
     * Check if the given value is a valid longitude value
     *
     * @param x
     * @return
     */
    static boolean validLongitude(double x) {
        return x >= -180 && x <= 180;
    }
}
