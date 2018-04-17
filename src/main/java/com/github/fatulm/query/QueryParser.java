package com.github.fatulm.query;

import java.net.URI;
import java.util.*;

import static com.github.fatulm.query.CollectionUtils.mapImpl;
import static com.github.fatulm.query.LambdaUtils.*;
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
    private Map<String, List<String>> map;

    /**
     * Instantiates QueryParser without any flags.
     */
    public QueryParser() {
        flags = EnumSet.noneOf(Flag.class);
        map = Collections.emptyMap();
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
                .map(elvis(mapIf(String::isEmpty, toNull())))
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
        Map<String, List<String>> newMap = mapImpl();
        map.forEach((k, vs) -> {
            if (!vs.isEmpty())
                newMap.put(k, vs);
        });
        return newMap;
    }

    /**
     * Converts encoded characters with % to unencoded characters
     *
     * @param map input map
     * @return processed map
     */
    private static Map<String, List<String>> convertEncodedCharacters(Map<String, List<String>> map) {
        Map<String, List<String>> newMap = mapImpl();
        map.forEach((k, vs) -> {
            String newKey = convertEncodedCharacters(k);
            newMap.putIfAbsent(newKey, CollectionUtils.listImpl());
            newMap.get(newKey).addAll(convertEncodedCharacters(vs));
        });
        return newMap;
    }

    /**
     * Ignores all white space around key and value items.
     * Converts fully white space keys and values to empty string.
     * keys are a set so if after this operation two keys become equal
     * then values are merged.
     * This Also makes Set of white Spaces between words to a single space.
     */
    private static Map<String, List<String>> ignoreWhiteSpace(Map<String, List<String>> map) {
        Map<String, List<String>> newMap = mapImpl();
        map.forEach((k, vs) -> {
            String newKey = ignoreWhiteSpace(k);
            newMap.putIfAbsent(newKey, CollectionUtils.listImpl());
            newMap.get(newKey).addAll(ignoreWhiteSpace(vs));
        });
        return newMap;
    }

    /**
     * Merges equal values.
     * Also note that: (null is equal to null) but ("" is not equal to null)
     */
    private static Map<String, List<String>> mergeValues(Map<String, List<String>> map) {
        Map<String, List<String>> newMap = mapImpl();
        map.forEach((k, vs) -> newMap.put(k, mergeValues(vs)));
        return newMap;
    }

    /**
     * Converts empty values to null.
     * But does not manipulate keys.
     */
    private static Map<String, List<String>> convertToNull(Map<String, List<String>> map) {
        Map<String, List<String>> newMap = mapImpl();
        map.forEach((k, vs) -> newMap.put(k, convertToNull(vs)));
        return newMap;
    }

    /**
     * Removes empty string to null mapping
     * (for example in parse("") we have one)
     */
    private static Map<String, List<String>> removeEmptyKeyToNullMaps(Map<String, List<String>> map) {
        Map<String, List<String>> newMap = mapImpl();
        map.forEach((k, vs) -> {
            if (!k.isEmpty())
                newMap.put(k, vs);
            else
                newMap.put(k,
                        vs.stream().filter(Objects::nonNull).collect(toList()));
        });
        return newMap;
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
     * Parses query strings.
     * You can get query String from URI by {@link URI#getQuery()}.
     * Also note that your string should not include "?"
     *
     * @param query query string
     * @return this
     */
    public QueryParser parse(String query) {
        if (!isEmpty())
            throw new IllegalStateException("query parser is not empty");

        if (query == null)
            throw new NullPointerException("query string should not be null");

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

        return this;
    }

    /**
     * Cleans QueryParser
     *
     * @return this
     */
    public QueryParser clear() {
        map.clear();
        return this;
    }

    /**
     * Checks if QueryParser is empty
     *
     * @return true if query parser is empty
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Checks if QueryParser contains a specified key
     *
     * @param key key that we want to check
     * @return if QueryParser contains the specified key
     * @throws NullPointerException if key is null
     */
    public boolean containsKey(String key) {
        if (key == null)
            throw new NullPointerException("key can not be null");
        return map.containsKey(key);
    }

    /**
     * Returns set of keys
     *
     * @return set of keys
     */
    public Set<String> getKeySet() {
        return map.keySet();
    }

    /**
     * Returns list of values for a specified key
     *
     * @param key the key that we want to get values for that key
     * @return list of values for a specified key
     * @throws NullPointerException if key is null
     */
    public List<String> getValues(String key) {
        if (key == null)
            throw new NullPointerException("key can not be null");
        return map.get(key);
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

        if (!isEmpty())
            if (flags.length > 0)
                for (Flag flag : flags)
                    if (!containsFlag(flag))
                        throw new IllegalStateException("query parser is not empty");

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

        if (!isEmpty()) {
            if (flags.length == 0) {
                if (!this.flags.isEmpty())
                    throw new IllegalStateException("query parser is not empty");
            } else {
                for (Flag flag : flags)
                    if (containsFlag(flag))
                        throw new IllegalStateException("query parser is not empty");
            }
        }

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
