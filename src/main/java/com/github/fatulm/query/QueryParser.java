package com.github.fatulm.query;

import java.net.URI;
import java.util.*;


/**
 * QueryParser is a Java API which can be used to parse query strings.
 * Query string can be obtained from URI by {@link URI#getQuery()}.
 * <br>
 * Order of the keys are not guarantied if you include some of the flags
 * and even some times which query string includes encoded characters.
 */
public class QueryParser {
    private EnumSet<Flag> flags;
    private HashMap<String, ArrayList<String>> map;

    /**
     * Instantiates QueryParser without any flags.
     */
    public QueryParser() {
        flags = EnumSet.noneOf(Flag.class);
        map = new HashMap<>();
    }

    /**
     * has some differences with {@link String#split(String)}
     *
     * @param str string which we want to split
     * @param c   splitter
     * @return list of  parts
     */
    static List<String> stringSplit(String str, char c) {
        ArrayList<String> output = new ArrayList<>();
        String splitter = new String(new char[]{c});

        stringSplit0(str, splitter, output);

        return output;
    }

    /**
     * @param str      string which we want to split
     * @param splitter splitter string including only one character
     * @param array    list of parts
     */
    private static void stringSplit0(String str, String splitter, ArrayList<String> array) {
        if (!str.contains(splitter)) {
            array.add(str);
            return;
        }

        int index = str.indexOf(splitter);
        String first = (index == 0) ? "" : str.substring(0, index);
        String second = (index == str.length() - 1) ? "" : str.substring(index + 1);

        array.add(first);
        stringSplit0(second, splitter, array);
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
     * instead use key=value1&key=value2
     *
     * @param query query string which is being checked
     * @throws IllegalArgumentException when query has invalid structure
     */
    private static void checkStructure(String query) {
        List<String> array = stringSplit(query, '&');
        for (String str : array)
            if (str.contains("="))
                if (str.indexOf('=') != str.lastIndexOf('='))
                    throw new IllegalArgumentException("query string has bad structure");
    }

    /**
     * Ignores white space for a list of strings.
     * Returns null for null.
     *
     * @param values list of strings
     * @return ignored list
     */
    private static ArrayList<String> ignoreWhiteSpace(List<String> values) {
        ArrayList<String> newValues = new ArrayList<>(values.size());
        for (String str : values)
            if (str == null)
                newValues.add(null);
            else
                newValues.add(ignoreWhiteSpace(str));
        return newValues;
    }

    /**
     * Ignores white space for a string.
     *
     * @param str input string should not be {@code null}
     * @return ignored string
     */
    private static String ignoreWhiteSpace(String str) {
        while (str.contains("\t"))
            str = str.replace('\t', ' ');

        while (str.contains("\n"))
            str = str.replace('\n', ' ');

        while (str.contains("  "))
            str = str.replace("  ", " ");
        return str.trim();
    }


    /**
     * Merges equal values for a list of value strings
     *
     * @param values input value list
     * @return output value list
     */
    private static ArrayList<String> mergeValues(ArrayList<String> values) {
        ArrayList<String> newValues = new ArrayList<>();

        for (String value : values)
            if (!newValues.contains(value))
                newValues.add(value);

        return newValues;
    }


    /**
     * Converts empty values to null for a value list
     *
     * @param values input value list
     * @return output value list
     */
    private static ArrayList<String> convertToNull(ArrayList<String> values) {
        ArrayList<String> newValues = new ArrayList<>(values.size());

        for (String value : values)
            if (value == null) {
                newValues.add(null);
            } else if (value.isEmpty()) {
                newValues.add(null);
            } else {
                newValues.add(value);
            }

        return newValues;
    }

    /**
     * Converts encoded characters to unencoded characters.
     * Input can not be {@code null}.
     *
     * @param str input string
     * @return output string
     */
    private static String convertEncodedCharacters(String str) {
        while (str.contains("%20"))
            str = str.replace("%20", " ");
        return str;

        // TODO: not complete + not tested completely
    }

    /**
     * Converts encoded characters to unencoded characters for a
     * list of value strings.
     *
     * @param values input values list
     * @return output values list
     */
    private static ArrayList<String> convertEncodedCharacters(ArrayList<String> values) {
        ArrayList<String> newValues = new ArrayList<>(values.size());

        for (String value : values)
            if (value == null) {
                newValues.add(null);
            } else {
                newValues.add(convertEncodedCharacters(value));
            }

        return newValues;
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
     * In addition to all alphanumerics and percent encoded characters,
     * a query can legally include the following unencoded characters:
     * / ? : @ - . _ ~ ! $ & ' ( ) * + , ; =
     *
     * @param query query string which is being checked
     * @throws IllegalArgumentException when query has invalid characters
     */
    private void checkCharacters(String query) {
        for (char c : query.toCharArray())
            if (!(
                    ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') ||
                            ('0' <= c && c <= '9') || c == '%' ||
                            c == '/' || c == '?' || c == ':' || c == '@' || c == '-' ||
                            c == '.' || c == '_' || c == '~' || c == '!' || c == '$' ||
                            c == '&' || c == '\'' || c == '(' || c == ')' || c == '*' ||
                            c == '+' || c == ',' || c == ';' || c == '=' ||
                            c == ' ' || c == '\n' || c == '\t'
            ))
                throw new IllegalArgumentException("query string has invalid characters");

        if (!containsFlag(Flag.WHITE_SPACE_IS_VALID))
            if (query.contains(" ") || query.contains("\n") || query.contains("\t"))
                throw new IllegalArgumentException("query string contains unencoded white space");

        // TODO: not complete + not tested completely
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
        checkCharacters(query);
        checkEncodedCharacters();
        checkStructure(query);

        parseChecked(query);

        if (containsFlag(Flag.IGNORE_WHITE_SPACE))
            ignoreWhiteSpace();

        convertEncodedCharacters();

        if (containsFlag(Flag.HARD_IGNORE_WHITE_SPACE))
            ignoreWhiteSpace();

        if (containsFlag(Flag.CONVERT_TO_NULL))
            convertToNull();

        if (containsFlag(Flag.MERGE_VALUES))
            mergeValues();

        removeEmptyKeyToNullMaps();
        removeEmptyKeySets();

        return this;
    }

    /**
     * Removes keys which have empty value list
     */
    private void removeEmptyKeySets() {
        HashMap<String, ArrayList<String>> newMap = new HashMap<>();
        for (String key : getKeySet())
            if (!getValues(key).isEmpty())
                newMap.put(key, (ArrayList<String>) getValues(key));
        map = newMap;
    }

    /**
     * Parses query string after checking all aspects
     *
     * @param query query string
     */
    private void parseChecked(String query) {
        List<String> array = stringSplit(query, '&');
        for (String str : array) {
            if (str.contains("=")) {
                List<String> keyValue = stringSplit(str, '=');
                if (!containsKey(keyValue.get(0))) {
                    map.put(keyValue.get(0), new ArrayList<String>());
                }
                getValues(keyValue.get(0)).add(keyValue.get(1));
            } else {
                if (!containsKey(str)) {
                    map.put(str, new ArrayList<String>());
                }
                getValues(str).add(null);
            }
        }
    }

    /**
     * Cleans QueryParser
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
     * Converts encoded characters with % to unencoded characters
     */
    private void convertEncodedCharacters() {
        HashMap<String, ArrayList<String>> newMap = new HashMap<>();

        for (String key : getKeySet()) {
            String newKey = convertEncodedCharacters(key);
            if (!newMap.containsKey(newKey))
                newMap.put(newKey, new ArrayList<String>());
            ArrayList<String> newValues = convertEncodedCharacters((ArrayList<String>) getValues(key));
            newMap.get(newKey).addAll(newValues);
        }

        map = newMap;
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
     * Ignores all white space around key and value items.
     * Converts fully white space keys and values to empty string.
     * keys are a set so if after this operation two keys become equal
     * then values are merged.
     * This Also makes Set of white Spaces between words to a single space.
     */
    private void ignoreWhiteSpace() {
        HashMap<String, ArrayList<String>> newMap = new HashMap<>();

        for (String key : getKeySet()) {
            String newKey = ignoreWhiteSpace(key);
            if (!newMap.containsKey(newKey))
                newMap.put(newKey, new ArrayList<String>());
            ArrayList<String> newValues = ignoreWhiteSpace(getValues(key));
            newMap.get(newKey).addAll(newValues);
        }

        map = newMap;
    }

    /**
     * Merges equal values.
     * Also note that: (null is equal to null) but ("" is not equal to null)
     */
    private void mergeValues() {
        HashMap<String, ArrayList<String>> newMap = new HashMap<>();
        for (String key : getKeySet())
            newMap.put(key, mergeValues((ArrayList<String>) getValues(key)));
        map = newMap;
    }

    /**
     * Converts empty values to null.
     * But does not manipulate keys.
     */
    private void convertToNull() {
        HashMap<String, ArrayList<String>> newMap = new HashMap<>();
        for (String key : getKeySet()) {
            ArrayList<String> values = (ArrayList<String>) getValues(key);
            ArrayList<String> newValues = convertToNull(values);
            newMap.put(key, newValues);
        }
        map = newMap;
    }

    /**
     * Removes empty string to null mapping
     * (for example in parse("") we have one)
     */
    private void removeEmptyKeyToNullMaps() {
        for (String key : getKeySet())
            if (key.isEmpty()) {
                List<String> values = getValues(key);
                while (values.contains(null))
                    values.remove(null);
                break;
            }
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
