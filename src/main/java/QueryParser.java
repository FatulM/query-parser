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
     * @throws NullPointerException if <tt>flags</tt> are null
     */
    public QueryParser addFlags(Flag... flags) {
        if (flags == null)
            throw new NullPointerException("flag should not be null");
        for (Flag flag : flags)
            if (flag == null)
                throw new NullPointerException("flag should not be null");

        this.flags.addAll(Arrays.asList(flags));

        return this;
    }

    /**
     * Removes <tt>flags</tt>
     * Should be used before {@link #parse}.
     *
     * @param flags the flags that you want to remove
     * @return this
     * @throws NullPointerException if <tt>flags</tt> are null
     */
    public QueryParser removeFlags(Flag... flags) {
        if (flags == null)
            throw new NullPointerException("flag should not be null");
        for (Flag flag : flags)
            if (flag == null)
                throw new NullPointerException("flag should not be null");

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
        if (query == null)
            throw new NullPointerException("query string should not be null");
        checkCharacters(query);
        checkStructure(query);
        // Parsing ...

        if (containsFlag(Flag.IGNORE_WHITE_SPACE))
            ignoreWhiteSpace();
        if (containsFlag(Flag.MERGE_VALUES))
            mergeValues();
        if (containsFlag(Flag.CONVERT_TO_NULL))
            convertToNull();

        return this;
    }

    public boolean containsKey(String key) {
        if (key == null)
            throw new NullPointerException("key can not be null");
        // Code ...
        return false;
    }

    public Set<String> getKeys() {
        // Code ...
        return null;
    }

    public List<String> getValues(String key) {
        if (key == null)
            throw new NullPointerException("key can not be null");
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
    }

    /**
     * Merges equal values.
     * Also note that: (null is equal to null) but ("" is not equal to null)
     */
    private void mergeValues() {
    }

    /**
     * Converts empty values to null.
     * But does not manipulate keys.
     */
    private void convertToNull() {
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
