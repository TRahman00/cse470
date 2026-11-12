package com.bracu.fraudgraph.detector;

import com.bracu.fraudgraph.domain.GraphSnapshot;
import com.bracu.fraudgraph.domain.PatternMatch;

import java.util.List;

/** Abstract detector from Figure 3. */
public abstract class PatternDetector {
    public abstract List<PatternMatch> detect(GraphSnapshot graph);
}
