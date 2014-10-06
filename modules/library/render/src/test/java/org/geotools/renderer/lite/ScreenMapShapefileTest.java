package org.geotools.renderer.lite;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.TextSymbolizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Sebastian Graca, ISPiK S.A.
 */
public class ScreenMapShapefileTest {

    private DataStore shapeFileDataStore;

    private SimpleFeatureType featureType;

    private SimpleFeature feature;

    @Before
    public void setUp() throws Exception {
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName("render-test");
        ftb.add("the_geom", Point.class, DefaultGeographicCRS.WGS84);
        ftb.setDefaultGeometry("the_geom");
        ftb.add("name", String.class);
        featureType = ftb.buildFeatureType();

        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory();

        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(featureType);
        fb.set("the_geom", gf.createPoint(new Coordinate(10, 10)));
        fb.set("name", "The name");
        feature = fb.buildFeature(null);
        
        File shpFile = new File("./target/screenMapTest/"
                + feature.getFeatureType().getName().getLocalPart() + ".shp");
        shpFile.getParentFile().mkdirs();

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(ShapefileDataStoreFactory.URLP.key, shpFile.toURI().toURL());
        shapeFileDataStore = dataStoreFactory.createNewDataStore(params);
        shapeFileDataStore.createSchema(feature.getFeatureType());
        SimpleFeatureStore featureStore = (SimpleFeatureStore) shapeFileDataStore
                .getFeatureSource(shapeFileDataStore.getTypeNames()[0]);
        featureStore.addFeatures(DataUtilities.collection(feature));
    }

    @After 
    public void dispose() {
        if(shapeFileDataStore != null) {
            shapeFileDataStore.dispose();
        }
    }

    private static BufferedImage renderImage(SimpleFeatureSource featureSource, int width,
            int height, Rectangle2D mapArea, Style style, Map renderingHints) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        MapContent mapContent = new MapContent();
        MapViewport viewport = mapContent.getViewport();
        viewport.setBounds(new ReferencedEnvelope(mapArea, DefaultGeographicCRS.WGS84));
        viewport.setScreenArea(new Rectangle(width, height));
        mapContent.addLayer(new FeatureLayer(featureSource, style));

        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setRendererHints(renderingHints);
        renderer.setMapContent(mapContent);
        renderer.paint(g, viewport.getScreenArea(), viewport.getBounds());
        return image;
    }

    @Test
    public void testOffsetLabel() throws IOException {
        SimpleFeatureSource fs = shapeFileDataStore.getFeatureSource(featureType.getTypeName());
        Style style = createLabelOffsetStyle();
        Map renderingHints = new HashMap();
        BufferedImage image = renderImage(fs, 200, 200, new Rectangle2D.Double(15, 0, 25, 10),
                style, renderingHints);
        assertEquals(0, countNonBlankPixels(image));
        renderingHints.put(StreamingRenderer.RENDERING_BUFFER, 100);
        image = renderImage(fs, 200, 200, new Rectangle2D.Double(15, 0, 25, 10), style,
                renderingHints);
        assertTrue(countNonBlankPixels(image) > 0);
    }


    private static Style createLabelOffsetStyle() {
        StyleBuilder sb = new StyleBuilder();
        PointPlacement pp = sb.createPointPlacement(0.5, 0.5, 50, 0, 0);
        TextSymbolizer ts = sb.createTextSymbolizer();
        ts.setFont(sb.createFont("Serif", 20));
        ts.setLabel(sb.getFilterFactory().literal("name"));
        ts.setLabelPlacement(pp);
        ts.getOptions().put("partials", "true");
        return sb.createStyle(ts);
    }

    protected int countNonBlankPixels(BufferedImage image) {
        int pixelsDiffer = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != Color.WHITE.getRGB()) {
                    ++pixelsDiffer;
                }
            }
        }

        return pixelsDiffer;
    }

}