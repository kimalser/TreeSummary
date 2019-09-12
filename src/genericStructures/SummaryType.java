package genericStructures;

// CascadingAnalystsLevelByLevel is the same as CascadingAnalysts but slightly
// more space efficient due to computing level by level and freeing memory used
// for the previous levels
public enum SummaryType {TreeSummary, CascadingAnalysts, CascadingAnalystsLevelByLevel, OverlappingRectangles, ExhaustiveTrees, Other}
