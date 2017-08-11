import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class QueryParser {
    private EnumSet<Flag> flags;

    /**
     * Instantiates QueryParser without any flags.
     */
    public QueryParser() {
        flags = EnumSet.noneOf(Flag.class);
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

        if (isEmpty()) {
            if (flags.length == 0) {
                if (!this.flags.isEmpty())
                    throw new IllegalStateException("query parser is not empty");
            } else {
                for (Flag flag : flags)
                    if (containsFlag(flag))
                        throw new IllegalStateException("query parser is not empty");
            }
        }

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
        // code ...
        if (!containsFlag(Flag.WHITE_SPACE_IS_VALID))
            if (query.contains(" "))
                throw new IllegalArgumentException("query string contains unencoded white space");
    }

    /**
     * Checks encoded characters for bad structure
     *
     * @throws IllegalArgumentException if encoded characters have bad structure
     */
    private void checkEncodedCharacters() {
        // code ...
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
    private void checkStructure(String query) {
        // code ...
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

        // Parsing ...

        if (containsFlag(Flag.IGNORE_WHITE_SPACE))
            ignoreWhiteSpace();

        convertEncodedCharacters();

        if (containsFlag(Flag.HARD_IGNORE_WHITE_SPACE))
            ignoreWhiteSpace();

        if (containsFlag(Flag.MERGE_VALUES))
            mergeValues();

        if (containsFlag(Flag.CONVERT_TO_NULL))
            convertToNull();

        removeEmptyKeyToNullMaps();

        return this;
    }

    /**
     * Cleans QueryParse
     */
    public QueryParser clear() {
        // code ...
        return this;
    }

    public boolean isEmpty() {
        return true;
    }

    /**
     * Converts encoded characters with % to unencoded characters
     */
    private void convertEncodedCharacters() {

    }

    /**
     * Checks key for bad structure or illegal characters
     *
     * @param key key that we want to check
     * @throws NullPointerException     if key is null
     * @throws IllegalArgumentException if key has illegal characters
     */
    private void checkKey(String key) {
        if (key == null)
            throw new NullPointerException("key can not be null");
        if (!containsFlag(Flag.WHITE_SPACE_IS_VALID))
            if (key.contains(" "))
                throw new IllegalArgumentException("key can not include unencoded white space");
        // Code ...
    }

    /**
     * Checks if QueryParser contains a specified key
     *
     * @param key key that we want to check
     * @return if QueryParser contains the specified key
     */
    public boolean containsKey(String key) {
        checkKey(key);
        // Code ...
        return false;
    }

    /**
     * @return set of keys
     */
    public Set<String> getKeys() {
        // Code ...
        return null;
    }

    /**
     * @param key the key that we want to get values for that key
     * @return list of values for a specified key
     */
    public List<String> getValues(String key) {
        checkKey(key);
        // Code ...
        return null;
    }

    /**
     * Ignores all white space around key and value items.
     * Converts fully white space keys and values to empty string.
     * keys are a set so if after this operation two keys become equal
     * then values are merged.
     * This Also makes Set of white Spaces between words to a single space.
     */
    private void ignoreWhiteSpace() {
        // code ...
    }

    private String ignoreWhiteSpace(String str) {
        while (str.contains("  "))
            str = str.replace("  ", " ");
        return str.trim();
    }

    /**
     * Merges equal values.
     * Also note that: (null is equal to null) but ("" is not equal to null)
     */
    private void mergeValues() {
        // code ...
    }

    /**
     * Converts empty values to null.
     * But does not manipulate keys.
     */
    private void convertToNull() {
        // code ...
    }

    /**
     * Removes empty string to null mapping
     * (for example in parse("") we have one)
     */
    private void removeEmptyKeyToNullMaps() {
        //code ...
    }

    /**
     * This enum includes flags which can be used in QueryParser.
     * <tt>IGNORE_WHITE_SPACE</tt> ignores all white spaces and converts fully
     * white space or empty strings to empty string (not null string)
     * <tt>MERGE_VALUES</tt> merges equal values
     * <tt>CONVERT_TO_NULL</tt> converts empty strings to null.
     * <tt>WHITE_SPACE_IS_VALID</tt> indicates that query string can have unencoded white space.
     * <tt>HARD_IGNORE_WHITE_SPACE</tt> ignores encoded white space too.
     * If you add all of them they will be execute in the order:
     * IGNORE_WHITE_SPACE -> HARD_IGNORE_WHITE_SPACE -> CONVERT_TO_NULL -> MERGE_VALUES
     */
    public enum Flag {
        IGNORE_WHITE_SPACE,
        CONVERT_TO_NULL,
        MERGE_VALUES,
        WHITE_SPACE_IS_VALID,
        HARD_IGNORE_WHITE_SPACE
    }
}
