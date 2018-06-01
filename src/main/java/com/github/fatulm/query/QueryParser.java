package com.github.fatulm.query;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.fatulm.query.LambdaUtils.elvis;
import static com.github.fatulm.query.LambdaUtils.mapIf;
import static com.github.fatulm.query.MapUtils.unmodifiableNonNullKeyMap;
import static com.github.fatulm.query.Preconditions.*;
import static com.github.fatulm.query.TextUtils.stringSplit;
import static java.util.stream.Collectors.*;


/**
 * QueryParser is a Java API which can be used to parse query strings.
 * Query string can be obtained from URI by {@link URI#getQuery()}.
 * <br>
 * Order of the keys are not guarantied if you include some of the flags
 * and even some times which query string includes encoded characters.
 */
public class QueryParser {
    static private final String SPACE = " ";

    private EnumSet<QueryParserFlag> flags;

    /**
     * Called from builder
     */
    QueryParser(EnumSet<QueryParserFlag> flags) {
        this.flags = flags;
    }

    /**
     * Ignores white space for a list of strings.
     * Returns null for null.
     *
     * @param values list of strings
     * @return ignored list
     */
    private static List<String> ignoreWhiteSpace(List<String> values) {
        return values.stream()
                .map(elvis(QueryParser::ignoreWhiteSpace))
                .collect(toList());
    }

    /**
     * Ignores white space for a string.
     *
     * @param str input string should not be {@code null}
     * @return ignored string
     */
    private static String ignoreWhiteSpace(String str) {
        return str.replaceAll("\\s+", SPACE).trim();
    }

    /**
     * Ignores white space for a string.
     * And also trims keys and values
     *
     * @param str input string should not be {@code null}
     * @return ignored string
     */
    private static String ignoreWhiteSpaceEx(String str) {
        return str.replaceAll("\\s+", SPACE)
                .replaceAll("\\s*=\\s*", "=")
                .replaceAll("\\s*&\\s*", "&")
                .trim();
    }


    /**
     * Merges equal values for a list of value strings
     *
     * @param values input value list
     * @return output value list
     */
    private static List<String> mergeValues(List<String> values) {
        return values.stream()
                .distinct()
                .collect(toList());
    }

    /**
     * Converts empty values to null for a value list
     *
     * @param values input value list
     * @return output value list
     */
    private static List<String> convertToNull(List<String> values) {
        return values.stream()
                .map(TextUtils::convertEmptyStringToNull)
                .collect(toList());
    }


    /**
     * Converts encoded characters to unencoded characters.
     * Input can not be {@code null}.
     *
     * @param str input string
     * @return output string
     */
    private static String convertEncodedCharacters(String str) {
        // TODO: not complete + not tested completely

        return str.replace("%20", SPACE);
    }

    /**
     * Converts encoded characters to unencoded characters for a
     * list of value strings.
     *
     * @param values input values list
     * @return output values list
     */
    private static List<String> convertEncodedCharacters(List<String> values) {
        return values.stream()
                .map(elvis(QueryParser::convertEncodedCharacters))
                .collect(toList());
    }

    /**
     * Removes keys which have empty value collection
     *
     * @param map input map
     * @return processed map
     */
    private static Map<String, List<String>> removeKeysWithEmptyValue(Map<String, List<String>> map) {
        return map.entrySet()
                .stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Converts encoded characters with % to unencoded characters
     *
     * @param map input map
     * @return processed map
     */
    private static Map<String, List<String>> convertEncodedCharacters(Map<String, List<String>> map) {
        return map.entrySet()
                .stream()
                .map(e -> new Pair<>(convertEncodedCharacters(e.getKey()), convertEncodedCharacters(e.getValue())))
                .collect(toMap(Pair::getKey, Pair::getValue,
                        (list1, list2) -> Stream.concat(list1.stream(), list2.stream()).collect(toList())));
    }

    /**
     * Ignores all white space around key and value items.
     * Converts fully white space keys and values to empty string.
     * keys are a set so if after this operation two keys become equal
     * then values are merged.
     * This Also makes Set of white Spaces between words to a single space.
     */
    private static Map<String, List<String>> ignoreWhiteSpace(Map<String, List<String>> map) {
        return map.entrySet()
                .stream()
                .map(e -> new Pair<>(ignoreWhiteSpace(e.getKey()), ignoreWhiteSpace(e.getValue())))
                .collect(toMap(Pair::getKey, Pair::getValue,
                        (list1, list2) -> Stream.concat(list1.stream(), list2.stream()).collect(toList())));
    }

    /**
     * Merges equal values.
     * Also note that: (null is equal to null) but ("" is not equal to null)
     */
    private static Map<String, List<String>> mergeValues(Map<String, List<String>> map) {
        return map.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, e -> mergeValues(e.getValue())));
    }

    /**
     * Converts empty values to null.
     * But does not manipulate keys.
     */
    private static Map<String, List<String>> convertToNull(Map<String, List<String>> map) {
        return map.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, e -> convertToNull(e.getValue())));
    }

    /**
     * Removes empty string to null mapping
     * (for example in parse("") we have one)
     */
    private static Map<String, List<String>> removeEmptyKeyToNullMaps(Map<String, List<String>> map) {
        return map.entrySet()
                .stream()
                .map(Pair::new)
                .map(mapIf(e -> e.getKey().isEmpty(),
                        e -> new Pair<>(e.getKey(), filterNonNull(e.getValue()))))
                .collect(toMap(Pair::getKey, Pair::getValue));
    }

    /**
     * filters non null elements in a list
     *
     * @param list input list
     * @return filtered list
     */
    private static List<String> filterNonNull(List<String> list) {
        return list.stream()
                .filter(Objects::nonNull)
                .collect(toList());
    }

    /**
     * Parses query string after checking all aspects
     *
     * @param query query string
     */
    private static Map<String, List<String>> parseChecked(String query) {
        return stringSplit(query, '&').stream()
                .map(str -> stringSplit(str, '='))
                .map(QueryParser::keyValueOrNullPair)
                .collect(groupingBy(Pair::getKey, mapping(Pair::getValue, toList())));
    }


    /**
     * Maps a list of one item to list of that item and null
     * Maps a list of two items to itself
     *
     * @param list input list
     * @return output list of key and value
     */
    private static Pair<String, String> keyValueOrNullPair(List<String> list) {
        return new Pair<>(list.get(0), list.size() == 2 ? list.get(1) : null);
    }

    public static QueryParserBuilder builder() {
        return new QueryParserBuilder();
    }

    /**
     * Parses query strings.
     * You can get query String from URI by {@link URI#getQuery()}.
     * Also note that your string should not include "?"
     *
     * @param query query string
     * @return map of queries
     */
    public Map<String, List<String>> parse(String query) {
        checkPreconditions(query);

        if (containsFlag(QueryParserFlag.IGNORE_WHITE_SPACE))
            query = ignoreWhiteSpaceEx(query);

        Map<String, List<String>> map = parseChecked(query);

        map = convertEncodedCharacters(map);

        if (containsFlag(QueryParserFlag.HARD_IGNORE_WHITE_SPACE))
            map = ignoreWhiteSpace(map);

        if (containsFlag(QueryParserFlag.CONVERT_TO_NULL))
            map = convertToNull(map);

        if (containsFlag(QueryParserFlag.MERGE_VALUES))
            map = mergeValues(map);

        map = removeEmptyKeyToNullMaps(map);
        map = removeKeysWithEmptyValue(map);

        return unmodifiableNonNullKeyMap(map);
    }

    /**
     * checks preconditions
     *
     * @param query query
     */
    private void checkPreconditions(String query) {
        checkQueryNonNull(query);

        checkCharactersGeneral(query);
        if (!containsFlag(QueryParserFlag.WHITE_SPACE_IS_VALID))
            checkWhiteSpaceCharacters(query);

        checkEncodedCharacters();
        checkStructure(query);
    }

    /**
     * Checks a specified flag state.
     *
     * @param flag flag which we want to check state
     * @return true if <tt>flag</tt> is added in builder
     * @throws NullPointerException if <tt>flag</tt> is null
     */
    public boolean containsFlag(QueryParserFlag flag) {
        if (flag == null)
            throw new NullPointerException("flag should not be null");
        return flags.contains(flag);
    }
}
