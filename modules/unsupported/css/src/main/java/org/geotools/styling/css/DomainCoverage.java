/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.styling.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.geotools.styling.css.selector.Data;
import org.geotools.styling.css.selector.Or;
import org.geotools.styling.css.selector.ScaleRange;
import org.geotools.styling.css.selector.Selector;
import org.geotools.styling.css.util.OgcFilterBuilder;
import org.geotools.styling.css.util.ScaleRangeExtractor;
import org.geotools.util.NumberRange;
import org.geotools.util.Range;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 * Represents the current coverage of the scale/filter domain, and has helper methods to add
 * {@link CssRule} to the covereage, split them into subrules by scale ranges, compare them with the
 * existing coverage, and genenerate rules matching only what's left to be covered.
 * 
 * @author Andrea Aime - GeoSolutions
 * 
 */
public class DomainCoverage {

    static final DoubleRange FULL_SCALE_RANGE = new DoubleRange(0, Double.POSITIVE_INFINITY);

    static final class DoubleRange extends NumberRange<Double> {

        private static final long serialVersionUID = -6299927149733005488L;

        public DoubleRange(double minimum, boolean isMinIncluded, double maximum,
                boolean isMaxIncluded) {
            super(Double.class, minimum, isMinIncluded, maximum, isMaxIncluded);
        }

        public DoubleRange(double minimum, double maximum) {
            super(Double.class, minimum, maximum);
        }

        public DoubleRange(Range<Double> range) {
            super(range);
        }

        public DoubleRange(NumberRange<?> range) {
            super(Double.class, range.getMinimum(), range.isMinIncluded(), range.getMaximum(),
                    range.isMaxIncluded());
        }

    }

    /**
     * A simplified representation of a Selector that takes apart the three main components, scale
     * range, filter and pseudoClass, to make it compatible with the SLD filtering model. A Selector
     * expressed in CSS language can be converted into a list of these.
     * 
     * @author Andrea Aime - GeoSolutions
     * 
     */
    class SLDSelector {

        DoubleRange scaleRange;

        Filter filter;

        public SLDSelector(NumberRange<?> scaleRange, Filter filter) {
            this.scaleRange = new DoubleRange(scaleRange.getMinimum(), scaleRange.isMinIncluded(),
                    scaleRange.getMaximum(), scaleRange.isMaxIncluded());
            this.filter = filter;
        }

        /**
         * Returns a list of scale dependent filters that represent the difference (the uncovered
         * area) between this {@link SLDSelector} and then specified rule
         * 
         * @param rule
         * @return
         */
        public List<SLDSelector> difference(SLDSelector other) {
            List<SLDSelector> result = new ArrayList<>();

            // first case, portions of scale range not overlapping
            NumberRange<?>[] scaleRangeDifferences = this.scaleRange.subtract(other.scaleRange);
            for (NumberRange<?> scaleRangeDifference : scaleRangeDifferences) {
                result.add(new SLDSelector(scaleRangeDifference, this.filter));
            }
            
            // second case, scale ranges overlapping, but filter/pseudoclass not
            NumberRange<?> scaleRangeIntersection = this.scaleRange.intersect(other.scaleRange);
            if (scaleRangeIntersection != null && !scaleRangeIntersection.isEmpty()) {
                Filter filterDifference = simplify(FF.and(this.filter, FF.not(other.filter)));
                if (filterDifference != Filter.EXCLUDE) {
                    result.add(new SLDSelector(scaleRangeIntersection, filterDifference));
                }
            }

            return result;
        }

        @Override
        public String toString() {
            return "ScaleDependentFilter [scaleRange=" + scaleRange + ", filter=" + filter + "]";
        }

        public Selector toSelector() {
            Selector selector = Selector.and(new ScaleRange(scaleRange), new Data(filter));
            
            return selector;
        }

    }

    private class ScaleDependentFilterComparator implements Comparator<SLDSelector> {

        @Override
        public int compare(SLDSelector o1, SLDSelector o2) {
            NumberRange<Double> sr1 = o1.scaleRange;
            NumberRange<Double> sr2 = o2.scaleRange;
            if (sr1.getMinimum() == sr2.getMinimum()) {
                if (sr1.isMinIncluded()) {
                    return sr2.isMinIncluded() ? 0 : -1;
                } else {
                    return sr2.isMinIncluded() ? 1 : 0;
                }

            } else {
                return sr1.getMinimum() > sr2.getMinimum() ? 1 : -1;
            }
        }

    }

    static final FilterFactory2 FF = CommonFactoryFinder.getFilterFactory2();

    private List<SLDSelector> elements;

    private FeatureType targetFeatureType;

    public DomainCoverage(FeatureType targetFeatureType) {
        this.elements = new ArrayList<>();
        this.targetFeatureType = targetFeatureType;
    }

    public List<CssRule> addRule(CssRule rule) {
        Selector selector = rule.selector;
        
        // turns the rule in a set of domain coverage expressions (simplified selectors)
        List<SLDSelector> ruleCoverage = toScaleDependentFilters(selector, targetFeatureType);
        
        // for each rule we have in the domain, get the differences, if any, with this rule, 
        // emit them as derived rules, and increase the coverage
        if (elements.isEmpty()) {
            elements.addAll(ruleCoverage);
            return Collections.singletonList(rule);
        } else {
            List<SLDSelector> reducedCoverage = new ArrayList<>(ruleCoverage);
            for (SLDSelector element : elements) {
                List<SLDSelector> difference = new ArrayList<>();
                for (SLDSelector rc : reducedCoverage) {
                      difference.addAll(rc.difference(element));
                }
                reducedCoverage = difference;
                if(reducedCoverage.isEmpty()) {
                    break;
                }
            }
            
            if (!reducedCoverage.isEmpty()) {
                List<CssRule> derivedRules = new ArrayList<>();
                for (SLDSelector rc : reducedCoverage) {
                    derivedRules.add(new CssRule(rc.toSelector(), rule.properties, rule.comment));
                    }

                elements.addAll(reducedCoverage);

                // so far, this sorting done just for the sake of readability during debugging
                Collections.sort(elements, new ScaleDependentFilterComparator());
                return derivedRules;
            } else {
                return Collections.emptyList();
            }
        }
    }

    List<SLDSelector> toScaleDependentFilters(Selector selector,
            FeatureType targetFeatureType) {
        List<SLDSelector> result = new ArrayList<>();
        if (selector instanceof Or) {
            Or or = (Or) selector;
            for (Selector s : or.children) {
                if (s instanceof Or) {
                    throw new IllegalArgumentException(
                            "Unexpected or selector nested inside another one, "
                                    + "at this point they should have been all flattened");
                }
                toIndependentSLDSelectors(s, targetFeatureType, result);
            }
        } else {
            toIndependentSLDSelectors(selector, targetFeatureType, result);
        }

        return result;
    }

    private void toIndependentSLDSelectors(Selector selector, FeatureType targetFeatureType,
            List<SLDSelector> scaleDependentFilters) {
        Range<Double> range = ScaleRangeExtractor.getScaleRange(selector);
        if (range == null) {
            range = FULL_SCALE_RANGE;
        }
        Filter filter = OgcFilterBuilder.buildFilter(selector, targetFeatureType);
        boolean merged = false;
        for (SLDSelector existing : scaleDependentFilters) {
            if (existing.scaleRange.equals(range)) {
                if (existing.filter instanceof org.opengis.filter.Or) {
                    org.opengis.filter.Or or = (org.opengis.filter.Or) existing.filter;
                    List<Filter> children = new ArrayList<>(or.getChildren());
                    children.add(filter);
                    existing.filter = simplify(FF.or(children));
                } else {
                    existing.filter = simplify(FF.or(existing.filter, filter));
                }
                merged = true;
                break;
            }
        }
        if (!merged) {
            scaleDependentFilters
.add(new SLDSelector(new NumberRange<>(range), filter));
        }
    }

    Filter simplify(Filter filter) {
        SimplifyingFilterVisitor visitor = new SimplifyingFilterVisitor();
        visitor.setFeatureType(targetFeatureType);
        return (Filter) filter.accept(visitor, null);
    }

    @Override
    public String toString() {
        return elements.toString();
    }

}
