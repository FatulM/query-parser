import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;

@SuppressWarnings("unused")
public class QueryParser {
    private EnumSet<Flag> flags;

    QueryParser() {
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
        return this;
    }

    public enum Flag {
        IGNORE_WHITE_SPACE,
        MERGE_KEYS,
        CONVERT_NULL
    }
}
