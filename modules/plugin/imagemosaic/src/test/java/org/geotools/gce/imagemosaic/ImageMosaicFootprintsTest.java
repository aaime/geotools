package org.geotools.gce.imagemosaic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.gce.imagemosaic.catalog.FootprintProviderFactory;
import org.geotools.geometry.DirectPosition2D;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTWriter;

public class ImageMosaicFootprintsTest {

    private File testMosaic;

    private URL testMosaicUrl;

    private File footprintsSource;

    @Before
    public void cleanup() throws IOException {
        // clean up
        testMosaic = new File("./target/footprintMosaic");
        if (testMosaic.exists()) {
            FileUtils.deleteDirectory(testMosaic);
        }

        // create the base mosaic we are going to use
        File mosaicSource = new File(
                "./src/test/resources/org/geotools/gce/imagemosaic/test-data/rgb");
        FileUtils.copyDirectory(mosaicSource, testMosaic);
        testMosaicUrl = DataUtilities.fileToURL(testMosaic);

        // make sure only the mosaic with a footprint file are left
        footprintsSource = new File(
                "./src/test/resources/org/geotools/gce/imagemosaic/test-data/rgb-footprints");
        ShapefileDataStore ds = new ShapefileDataStore(DataUtilities.fileToURL(new File(
                footprintsSource, "footprints.shp")));
        UniqueVisitor locations = new UniqueVisitor("location");
        ds.getFeatureSource().getFeatures().accepts(locations, null);
        ds.dispose();
        Set unique = locations.getUnique();

        for (File file : testMosaic.listFiles()) {
            String fname = file.getName();
            if (fname.endsWith("png") && !unique.contains(fname)) {
                assertTrue(file.delete());
            }
        }
    }

    @Test
    public void testSingleShapefileDefaults() throws Exception {
        // copy the footprints mosaic over
        FileUtils.copyDirectory(footprintsSource, testMosaic);

        assertItalyFootprints();
    }

    @Test
    public void testWkbSidecars() throws Exception {
        // create wkb sidecar files
        ShapefileDataStore ds = new ShapefileDataStore(DataUtilities.fileToURL(new File(
                footprintsSource, "footprints.shp")));
        ds.getFeatureSource().getFeatures().accepts(new FeatureVisitor() {

            @Override
            public void visit(Feature feature) {
                try {
                    SimpleFeature sf = (SimpleFeature) feature;
                    String fileName = (String) sf.getAttribute("location");
                    int idx = fileName.lastIndexOf(".");
                    Geometry g = (Geometry) sf.getDefaultGeometry();
                    File wkbFile = new File(testMosaic, fileName.substring(0, idx) + ".wkb");
                    byte[] bytes = new WKBWriter().write(g);
                    FileUtils.writeByteArrayToFile(wkbFile, bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, null);

        assertItalyFootprints();
    }

    @Test
    public void testWktSidecars() throws Exception {
        // create wkb sidecar files
        ShapefileDataStore ds = new ShapefileDataStore(DataUtilities.fileToURL(new File(
                footprintsSource, "footprints.shp")));
        ds.getFeatureSource().getFeatures().accepts(new FeatureVisitor() {

            @Override
            public void visit(Feature feature) {
                try {
                    SimpleFeature sf = (SimpleFeature) feature;
                    String fileName = (String) sf.getAttribute("location");
                    int idx = fileName.lastIndexOf(".");
                    Geometry g = (Geometry) sf.getDefaultGeometry();
                    File wkbFile = new File(testMosaic, fileName.substring(0, idx) + ".wkt");
                    String wkt = new WKTWriter().write(g);
                    FileUtils.writeStringToFile(wkbFile, wkt);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, null);

        assertItalyFootprints();
    }

    @Test
    public void testShapefileSidecars() throws Exception {
        // create wkb sidecar files
        ShapefileDataStore ds = new ShapefileDataStore(DataUtilities.fileToURL(new File(
                footprintsSource, "footprints.shp")));
        ds.getFeatureSource().getFeatures().accepts(new FeatureVisitor() {

            @Override
            public void visit(Feature feature) {
                try {
                    SimpleFeature sf = (SimpleFeature) feature;
                    String fileName = (String) sf.getAttribute("location");
                    int idx = fileName.lastIndexOf(".");
                    Geometry g = (Geometry) sf.getDefaultGeometry();
                    String filename = fileName.substring(0, idx);
                    File shpFile = new File(testMosaic, filename + ".shp");
                    ShapefileDataStore sds = new ShapefileDataStore(
                            DataUtilities.fileToURL(shpFile));
                    SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
                    tb.setName(filename);
                    GeometryDescriptor gd = sf.getFeatureType().getGeometryDescriptor();
                    tb.add("the_geom", gd.getType().getBinding(), gd.getCoordinateReferenceSystem());
                    SimpleFeatureType sft = tb.buildFeatureType();
                    sds.createSchema(sft);

                    SimpleFeatureBuilder fb = new SimpleFeatureBuilder(sft);
                    fb.add(g);
                    SimpleFeature footprintFeature = fb.buildFeature(null);
                    SimpleFeatureStore fs = (SimpleFeatureStore) sds.getFeatureSource();
                    fs.addFeatures(DataUtilities.collection(footprintFeature));
                    sds.dispose();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, null);

        assertItalyFootprints();
    }

    private void assertItalyFootprints() throws NoSuchAuthorityCodeException, FactoryException,
            IOException {
        GridCoverage2D coverage = readCoverage();

        // RenderedImageBrowser.showChain(coverage.getRenderedImage());
        // System.in.read();

        // check the footprints have been applied by pocking the output image
        byte[] pixel = new byte[3];
        // Mar Ionio, should be black
        coverage.evaluate(new DirectPosition2D(16.87, 40.19), pixel);
        assertEquals(0, pixel[0]);
        assertEquals(0, pixel[1]);
        assertEquals(0, pixel[2]);
        // Golfo di La Spezia, should be black
        coverage.evaluate(new DirectPosition2D(9.12, 44.25), pixel);
        assertEquals(0, pixel[0]);
        assertEquals(0, pixel[1]);
        assertEquals(0, pixel[2]);
        // Sardinia, not black
        coverage.evaluate(new DirectPosition2D(9, 40), pixel);
        assertTrue(pixel[0] + pixel[1] + pixel[2] > 0);
        // Piedmont, not black
        coverage.evaluate(new DirectPosition2D(8, 45), pixel);
        assertTrue(pixel[0] + pixel[1] + pixel[2] > 0);
    }

    private GridCoverage2D readCoverage() throws NoSuchAuthorityCodeException, FactoryException,
            IOException {
        final AbstractGridFormat format = TestUtils.getFormat(testMosaicUrl);
        final ImageMosaicReader reader = TestUtils.getReader(testMosaicUrl, format);
        // activate footprint management
        GeneralParameterValue[] params = new GeneralParameterValue[1];
        ParameterValue<String> footprintManagement = ImageMosaicFormat.FOOTPRINT_BEHAVIOR
                .createValue();
        footprintManagement.setValue(FootprintBehavior.Cut.name());
        params[0] = footprintManagement;
        GridCoverage2D coverage = reader.read(params);
        assertNotNull(coverage);
        return coverage;
    }

    @Test
    public void testInsetsBorder() throws Exception {
        // copy the footprints mosaic over
        FileUtils.copyDirectory(footprintsSource, testMosaic);
        Properties p = new Properties();
        p.put(FootprintProviderFactory.INSET_PROPERTY, "0.1"); 
        saveFootprintProperties(p);

         GridCoverage2D coverage = readCoverage();
//         RenderedImageBrowser.showChain(coverage.getRenderedImage());
//         System.in.read();
        
        // check the footprints have been applied by pocking the output image
        byte[] pixel = new byte[3];
        // Close to San Marino, black if we have the insets
        coverage.evaluate(new DirectPosition2D(12.54, 44.03), pixel);
        assertEquals(0, pixel[0]);
        assertEquals(0, pixel[1]);
        assertEquals(0, pixel[2]);
        // Golfo di La Spezia, should be black
        coverage.evaluate(new DirectPosition2D(9.12, 44.25), pixel);
        assertEquals(0, pixel[0]);
        assertEquals(0, pixel[1]);
        assertEquals(0, pixel[2]);
        // Sardinia, not black
        coverage.evaluate(new DirectPosition2D(9, 40), pixel);
        assertTrue(pixel[0] + pixel[1] + pixel[2] > 0);
        // Piedmont, not black
        coverage.evaluate(new DirectPosition2D(8, 45), pixel);
        assertTrue(pixel[0] + pixel[1] + pixel[2] > 0);
    }
    
    @Test
    public void testInsetsFull() throws Exception {
        // copy the footprints mosaic over
        FileUtils.copyDirectory(footprintsSource, testMosaic);
        Properties p = new Properties();
        p.put(FootprintProviderFactory.INSET_PROPERTY, "0.1"); 
        p.put(FootprintProviderFactory.INSET_TYPE_PROPERTY, "full");
        saveFootprintProperties(p);

        GridCoverage2D coverage = readCoverage();
//         RenderedImageBrowser.showChain(coverage.getRenderedImage());
//         System.in.read();
        
        // check the footprints have been applied by pocking the output image
        byte[] pixel = new byte[3];
        // Close to San Marino, black if we have the insets
        coverage.evaluate(new DirectPosition2D(12.54, 44.03), pixel);
        assertEquals(0, pixel[0]);
        assertEquals(0, pixel[1]);
        assertEquals(0, pixel[2]);
        // Inner border gets black with full insets
        coverage.evaluate(new DirectPosition2D(11.52, 44.57), pixel);
        assertEquals(0, pixel[0]);
        assertEquals(0, pixel[1]);
        assertEquals(0, pixel[2]);
        // Golfo di La Spezia, should be black
        coverage.evaluate(new DirectPosition2D(9.12, 44.25), pixel);
        assertEquals(0, pixel[0]);
        assertEquals(0, pixel[1]);
        assertEquals(0, pixel[2]);
        // Sardinia, not black
        coverage.evaluate(new DirectPosition2D(9, 40), pixel);
        assertTrue(pixel[0] + pixel[1] + pixel[2] > 0);
        // Piedmont, not black
        coverage.evaluate(new DirectPosition2D(8, 45), pixel);
        assertTrue(pixel[0] + pixel[1] + pixel[2] > 0);
    }

    private void saveFootprintProperties(Properties p) throws FileNotFoundException, IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(testMosaic, "footprints.properties"));
            p.store(fos, null);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }
}
