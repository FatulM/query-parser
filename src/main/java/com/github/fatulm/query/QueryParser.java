package com.github.fatulm.query;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.fatulm.query.LambdaUtils.elvis;
import static com.github.fatulm.query.LambdaUtils.mapIf;
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

    private EnumSet<Flag> flags;

    /**
     * Instantiates QueryParser without any flags.
     */
    public QueryParser() {
        flags = EnumSet.noneOf(Flag.class);
    }

    /**
     * Checks encoded characters for bad structure
     *
     * @throws IllegalArgumentException if encoded characters have bad structure
     */
    private static void checkEncodedCharacters() {
        // TODO: not complete + not tested
    }

    /**
     * Checks query string structure.
     * Key and value can be empty.
     * And each key can contain multiple values.
     * But your string can not have key=value1=value2,
     * instead use key=value1&amp;key=value2
     *
     * @param query query string which is being checked
     * @throws IllegalArgumentException when query has invalid structure
     */
    private static void checkStructure(String query) {
        // for each part: "([^=&]*=?[^=&]*)" (matches empty)
        // structure: "(part)(&(part))*"

        if (!query.matches("([^=&]*=?[^=&]*)(&([^=&]*=?[^=&]*))*"))
            throw new IllegalArgumentException("query string has bad structure");
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
     * In addition to all alphanumerics and percent encoded characters,
     * a query can legally include the following unencoded characters:
     * / ? : @ - . _ ~ ! $ &amp; ' ( ) * + , ; =
     * This method lets having white space characters
     *
     * @param query query string which is being checked
     * @throws IllegalArgumentException when query has invalid characters
     */
    private static void checkCharactersGeneral(String query) {
        if (!query.matches("[\\w\\s.+*\\-%/?:@_~!$&(),;=']*"))
            throw new IllegalArgumentException("query string has invalid characters");

        // TODO: not complete + not tested completely
    }

    /**
     * This method ensures that a given query string does not have white space
     * When white space is not valid we use this method
     *
     * @param query query string which is being checked
     * @throws IllegalArgumentException when query has white space characters
     */
    private static void checkWhiteSpaceCharacters(String query) {
        if (!query.matches("[^\\s]*"))
            throw new IllegalArgumentException("query string contains unencoded white space");
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

    /**
     * @throws NullPointerException if query is null
     */
    private static void checkQueryNonNull(String query) {
        if (query == null)
            throw new NullPointerException("query string should not be null");
    }

    /**
     * @return unmodifiable map with non null keys
     */
    private static <K, V> Map<K, V> unmodifiableNonNullKeyMap(Map<K, V> map) {
        return Collections.unmodifiableMap(new HashMap<K, V>(map) {
            @Override
            public V get(Object key) {
                return super.get(requireKeyNonNull(key));
            }

            @Override
            public boolean containsKey(Object key) {
                return super.containsKey(requireKeyNonNull(key));
            }
        });
    }

    /**
     * @return itself
     * @throws NullPointerException if key is null
     */
    private static Object requireKeyNonNull(Object key) {
        if (key == null)
            throw new NullPointerException("key can not be null");
        return key;
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
        Map<String, List<String>> map;

        checkQueryNonNull(query);

        checkCharactersGeneral(query);
        if (!containsFlag(Flag.WHITE_SPACE_IS_VALID))
            checkWhiteSpaceCharacters(query);

        checkEncodedCharacters();
        checkStructure(query);

        if (containsFlag(Flag.IGNORE_WHITE_SPACE))
            query = ignoreWhiteSpaceEx(query);

        map = parseChecked(query);

        map = convertEncodedCharacters(map);

        if (containsFlag(Flag.HARD_IGNORE_WHITE_SPACE))
            map = ignoreWhiteSpace(map);

        if (containsFlag(Flag.CONVERT_TO_NULL))
            map = convertToNull(map);

        if (containsFlag(Flag.MERGE_VALUES))
            map = mergeValues(map);

        map = removeEmptyKeyToNullMaps(map);
        map = removeKeysWithEmptyValue(map);

        return unmodifiableNonNullKeyMap(map);
    }

    /**
     * Checks a specified flag state.
     *
     * @param flag flag which we want to check state
     * @return true if <tt>flag</tt> is added before
     * @throws NullPointerException if <tt>flag</tt> is null
     */
    public boolean containsFlag(Flag flag) {
        if (flag == null)
            throw new NullPointerException("flag should not be null");
        return flags.contains(flag);
    }

    /**
     * Adds all <tt>flags</tt>.
     * Should be used before {@link #parse}.
     *
     * @param flags the flags that you want to add
     * @return this
     * @throws NullPointerException  if <tt>flags</tt> are null
     * @throws IllegalStateException if query parser is not empty and this manipulation has effect
     */
    public QueryParser addFlags(Flag... flags) {
        if (flags == null)
            throw new NullPointerException("flag should not be null");
        for (Flag flag : flags)
            if (flag == null)
                throw new NullPointerException("flag should not be null");

        if (Arrays.asList(flags).contains(Flag.IGNORE_WHITE_SPACE))
            if (!Arrays.asList(flags).contains(Flag.WHITE_SPACE_IS_VALID))
                if (!containsFlag(Flag.WHITE_SPACE_IS_VALID))
                    throw new IllegalStateException
                            ("can not add IGNORE_WHITE_SPACE without WHITE_SPACE_IS_VALID");

        this.flags.addAll(Arrays.asList(flags));

        return this;
    }

    /**
     * Removes <tt>flags</tt>
     * Should be used before {@link #parse}.
     * If used without argument then removes all flags.
     *
     * @param flags the flags that you want to remove
     * @return this
     * @throws NullPointerException  if <tt>flags</tt> are null
     * @throws IllegalStateException if query parser is not empty and this manipulation has effect
     */
    public QueryParser removeFlags(Flag... flags) {
        if (flags == null)
            throw new NullPointerException("flag should not be null");
        for (Flag flag : flags)
            if (flag == null)
                throw new NullPointerException("flag should not be null");

        if (flags.length != 0)
            if (Arrays.asList(flags).contains(Flag.WHITE_SPACE_IS_VALID))
                if (!Arrays.asList(flags).contains(Flag.IGNORE_WHITE_SPACE))
                    if (containsFlag(Flag.WHITE_SPACE_IS_VALID))
                        if (containsFlag(Flag.IGNORE_WHITE_SPACE))
                            throw new RuntimeException
                                    ("Can not remove WHITE_SPACE_IS_VALID and having IGNORE_WHITE_SPACE");

        if (flags.length == 0)
            this.flags.clear();
        else
            this.flags.removeAll(Arrays.asList(flags));

        return this;
    }

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
    public enum Flag {
        IGNORE_WHITE_SPACE,
        CONVERT_TO_NULL,
        MERGE_VALUES,
        WHITE_SPACE_IS_VALID,
        HARD_IGNORE_WHITE_SPACE
    }
}
