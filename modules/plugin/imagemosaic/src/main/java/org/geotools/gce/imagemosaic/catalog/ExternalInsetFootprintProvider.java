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
import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;

/**
 * A footprint provider that applies a negative buffer on the geometries loaded by a delegate
 * {@link FootprintProvider}
 * 
 * @author Andrea Aime - GeoSolutions
 *
 */
public class ExternalInsetFootprintProvider implements FootprintProvider {
    
    FootprintProvider delegate;
    double inset;
    
    public ExternalInsetFootprintProvider(FootprintProvider delegate, double inset) {
        this.delegate = delegate;
        this.inset = Math.abs(inset);
    }

    public Geometry getFootprint(SimpleFeature feature) throws IOException {
        Geometry footprint = delegate.getFootprint(feature);
        if(footprint != null) {
            Geometry bounds = (Geometry) feature.getDefaultGeometry();
            // we buffer only the portions of the footprint that are not overlapping with
            // the granule bounds, and remove them from the footprint
            List<LinearRing> boundRings = getRings(bounds);
            List<LinearRing> footprintRings = getRings(footprint);
            Geometry bufferedOuterRings = buffer(boundRings, Math.max(inset / 100, 1e-9));
            List<LineString> internalBorders = filterRings(footprintRings, bufferedOuterRings);
            if(!internalBorders.isEmpty()) {
                Geometry bufferedInternalRings = buffer(internalBorders, inset);
                Geometry difference = footprint.difference(bufferedInternalRings);
                footprint = collectPolygons(difference);
            }
        }
        
        return footprint;
    }

    /**
     * Collects all sub-polygons into the specified geometry and returns them either as a single
     * polygon, or as a multipolygon, shaving off any other lower dimension geometry
     * @param geometry
     * @return
     */
    private Geometry collectPolygons(Geometry geometry) {
        if(geometry.isEmpty()) {
            return geometry;
        }
        
        final List<Polygon> polygons = new ArrayList<Polygon>();
        geometry.apply(new GeometryComponentFilter() {
            
            @Override
            public void filter(Geometry geom) {
                if(geom instanceof Polygon && !geom.isEmpty()) {
                    polygons.add((Polygon) geom);
                }
                
            }
        });
        
        if(polygons.isEmpty()) {
            return geometry.getFactory().createMultiPolygon(new Polygon[0]);
        } else if(polygons.size() == 1) {
            return polygons.get(0);
        } else {
            Polygon[] array = (Polygon[]) polygons.toArray(new Polygon[polygons.size()]);
            return array[0].getFactory().createMultiPolygon(array);
        }
    }

    private List<LineString> filterRings(List<LinearRing> footprintRings,
            Geometry bufferedOuterRings) {
        List<LineString> result = new ArrayList<LineString>();
        for (LinearRing ring : footprintRings) {
            Geometry difference = ring.difference(bufferedOuterRings);
            if(difference != null) {
                collectLines(difference, result);
            }
        }
        
        return result;
    }

    private Geometry buffer(List<? extends Geometry> geometries, double distance) {
        List<Geometry> polygons = new ArrayList<Geometry>();
        for (Geometry g : geometries) {
            Geometry buffered = g.buffer(distance);
            polygons.add(buffered);
        }
        
        return CascadedPolygonUnion.union(polygons);
    }

    private List<LinearRing> getRings(Geometry bounds) {
        final ArrayList<LinearRing> rings = new ArrayList<LinearRing>();
        bounds.apply(new GeometryComponentFilter() {
            
            @Override
            public void filter(Geometry geom) {
                if(geom instanceof LinearRing && !geom.isEmpty()) {
                    rings.add((LinearRing) geom);
                }
                
            }
        });
        return rings;
    }
    
    private void collectLines(Geometry geometry, final List<LineString> lines) {
        geometry.apply(new GeometryComponentFilter() {
            
            @Override
            public void filter(Geometry geom) {
                if(geom instanceof LineString && !geom.isEmpty()) {
                    lines.add((LineString) geom);
                }
                
            }
        });
    }

    public void dispose() {
        delegate.dispose();
    }

}
