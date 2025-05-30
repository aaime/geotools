/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2016, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gml3.bindings;

import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.FeatureCollection;
import org.geotools.gml3.GML;
import org.geotools.xsd.AbstractComplexBinding;
import org.geotools.xsd.ElementInstance;
import org.geotools.xsd.Node;

/**
 * Binding object for the type http://www.opengis.net/gml:FeatureArrayPropertyType.
 *
 * <p>
 *
 * <pre>
 *         <code>
 *  &lt;complexType name="FeatureArrayPropertyType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Container for features - follow gml:ArrayAssociationType pattern.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_Feature"/&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 *
 * @generated
 */
public class FeatureArrayPropertyTypeBinding extends AbstractComplexBinding {
    /** @generated */
    @Override
    public QName getTarget() {
        return GML.FeatureArrayPropertyType;
    }

    /**
     *
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    @Override
    public Class getType() {
        return FeatureCollection.class;
    }

    /**
     *
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {
        FeatureCollection fc = node.getChildValue(FeatureCollection.class);
        if (fc != null) {
            return fc;
        }

        List<SimpleFeature> features = node.getChildValues(SimpleFeature.class);
        fc = new DelayedSchemaFeatureCollection();

        ((Collection) fc).addAll(features);
        return fc;
    }

    @Override
    public Object getProperty(Object object, QName name) {
        // passed in should be FeatureCollection, just pass it back
        return object;
    }
}
