package com.github.fatulm.query;

/**
 * This enum includes flags which can be used in QueryParser.
 * <tt>IGNORE_WHITE_SPACE</tt> ignores all white spaces and converts fully
 * white space or empty strings to empty string (not null string).
 * This option does NOT guaranty order of the values.
 * <tt>MERGE_VALUES</tt> merges equal values
 * <tt>CONVERT_TO_NULL</tt> converts empty strings to null.
 * <tt>WHITE_SPACE_IS_VALID</tt> indicates that query string can have unencoded white space.
 * <tt>HARD_IGNORE_WHITE_SPACE</tt> ignores encoded white space too.
 * If you add all of them they will be execute in the order:
 * IGNORE_WHITE_SPACE then HARD_IGNORE_WHITE_SPACE then CONVERT_TO_NULL then MERGE_VALUES
 */
public enum QueryParserFlag {
    IGNORE_WHITE_SPACE,
    CONVERT_TO_NULL,
    MERGE_VALUES,
    WHITE_SPACE_IS_VALID,
    HARD_IGNORE_WHITE_SPACE
}
