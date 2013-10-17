/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2013, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagemosaic.catalog;

import java.io.IOException;

import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A footprint provider that applies a negative buffer on the geometries loaded by a delegate
 * {@link FootprintProvider}
 * 
 * @author Andrea Aime - GeoSolutions
 *
 */
public class FullInsetFootprintProvider implements FootprintProvider {
    
    FootprintProvider delegate;
    double inset;
    
    public FullInsetFootprintProvider(FootprintProvider delegate, double inset) {
        this.delegate = delegate;
        this.inset = Math.abs(inset);
    }

    public Geometry getFootprint(SimpleFeature feature) throws IOException {
        Geometry g = delegate.getFootprint(feature);
        if(g != null) {
            g = g.buffer(-inset);
        }
        
        return g;
    }

    public void dispose() {
        delegate.dispose();
    }

}
