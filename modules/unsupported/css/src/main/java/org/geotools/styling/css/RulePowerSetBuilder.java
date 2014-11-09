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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geotools.styling.css.selector.PseudoClass;
import org.geotools.styling.css.selector.Selector;
import org.geotools.styling.css.util.FilteredPowerSetBuilder;

/**
 * Gives a list of Rules, it builds their power set, making it so that any set of rules extracted as
 * at least a chance to match a feature (e.g., the rule selectors are not contractiding each other)
 * 
 * @author Andrea Aime - GeoSolutions
 * 
 */
public class RulePowerSetBuilder extends FilteredPowerSetBuilder<CssRule, CssRule> {
    List<CssRule> lastRuleSet;

    Selector lastCombinedSelector;

    public RulePowerSetBuilder(List<CssRule> domain) {
        super(domain);
    }

    @Override
    protected CssRule buildResult(List<CssRule> rules) {
        if (rules.size() == 1) {
            return rules.get(0);
        }
        Selector combinedSelector = combineSelectors(rules);

        // sort by specificity, from lowest to highest
        // TODO: check if this is really needed
        Collections.sort(rules, new CssRuleComparator());

        // apply cascading on properties
        Map<PseudoClass, Map<String, Property>> properties = new LinkedHashMap<>();
        for (CssRule cssRule : rules) {
            for (Map.Entry<PseudoClass, List<Property>> entry : cssRule.properties.entrySet()) {
                PseudoClass ps = entry.getKey();
                Map<String, Property> psProperties = properties.get(ps);
                if (psProperties == null) {
                    psProperties = new HashMap<String, Property>();
                    properties.put(ps, psProperties);
                }
                for (Property p : entry.getValue()) {
                    psProperties.put(p.name, p);
                }
                if (ps != PseudoClass.ROOT) {
                    // we also have to fill values for the pseudo classes owned by this one
                    for (PseudoClass containedClass : properties.keySet()) {
                        if (ps.contains(containedClass)) {
                            Map<String, Property> containedProperties = properties
                                    .get(containedClass);
                            for (Property p : entry.getValue()) {
                                containedProperties.put(p.name, p);
                            }
                        }
                    }
                }
            }
        }

        // build the new rule
        Map<PseudoClass, List<Property>> newProperties = new LinkedHashMap<>();
        for (Map.Entry<PseudoClass, Map<String, Property>> entry : properties.entrySet()) {
            newProperties.put(entry.getKey(), new ArrayList<Property>(entry.getValue().values()));
        }
        String comment = getCombinedComment(rules);
        CssRule result = new CssRule(combinedSelector, newProperties, comment);
        result.ancestry = rules;
        return result;
    }

    private String getCombinedComment(List<CssRule> rules) {
        StringBuilder sb = new StringBuilder();
        for (CssRule rule : rules) {
            if (rule.comment != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(rule.comment);
            }
        }

        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return null;
        }
    }

    @Override
    protected boolean accept(List<CssRule> rules) {
        return rules.get(0).selector != Selector.REJECT;
    }

    Selector combineSelectors(List<CssRule> rules) {
        if (rules == lastCombinedSelector) {
            return lastCombinedSelector;
        }
        Selector s;
        if (rules.size() == 1) {
            s = rules.get(0).selector;
        } else {
            s = rules.get(0).selector;
            for (int i = 1; i < rules.size() && s != Selector.REJECT; i++) {
                CssRule rule = rules.get(i);
                s = Selector.and(s, rule.selector);
            }
        }
        this.lastRuleSet = rules;
        this.lastCombinedSelector = s;
        return s;
    }

    @Override
    protected List<CssRule> postFilterResult(List<CssRule> result) {
        List<CssRule> filtered = new ArrayList<>();
        // first remove the rules that have no root pseudo selector, they are contributing nothing
        for (CssRule cssRule : result) {
            if (cssRule.properties.get(PseudoClass.ROOT) != null) {
                filtered.add(cssRule);
            }
        }
        if (filtered.size() <= 1) {
            return filtered;
        }

        // sort by selectivity
        Collections.sort(filtered, Collections.reverseOrder(new CssRuleComparator()));

        // we can have rules with the same selector, generated when combining a rule with *,
        // fold them if they are covering each other (and they should)
        List<CssRule> folded = new ArrayList<>();
        CssRule prev = filtered.get(0);
        for (int i = 1; i < filtered.size(); i++) {
            CssRule curr = filtered.get(i);
            if (curr.covers(prev)) {
                prev = curr;
            } else if (prev.covers(curr)) {
                // nothing to do, skip it
            } else {
                folded.add(prev);
                prev = curr;
            }
        }
        folded.add(prev);
        return folded;
    }

}
