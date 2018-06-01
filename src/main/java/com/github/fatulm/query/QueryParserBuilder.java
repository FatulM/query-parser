package com.github.fatulm.query;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Builder class for QueryParser
 */
public class QueryParserBuilder {
    private EnumSet<QueryParserFlag> flags;

    /**
     * Called from QueryParser.builder()
     */
    QueryParserBuilder() {
        flags = EnumSet.noneOf(QueryParserFlag.class);
    }

    /**
     * Adds all <tt>flags</tt>.
     *
     * @param flags the flags that you want to add
     * @return this
     * @throws NullPointerException if <tt>flags</tt> are null
     */
    public QueryParserBuilder addFlags(QueryParserFlag... flags) {
        if (flags == null)
            throw new NullPointerException("flag should not be null");
        for (QueryParserFlag flag : flags)
            if (flag == null)
                throw new NullPointerException("flag should not be null");

        if (Arrays.asList(flags).contains(QueryParserFlag.IGNORE_WHITE_SPACE))
            if (!Arrays.asList(flags).contains(QueryParserFlag.WHITE_SPACE_IS_VALID))
                if (!containsFlag(QueryParserFlag.WHITE_SPACE_IS_VALID))
                    throw new IllegalStateException
                            ("can not add IGNORE_WHITE_SPACE without WHITE_SPACE_IS_VALID");

        this.flags.addAll(Arrays.asList(flags));

        return this;
    }

    /**
     * Removes <tt>flags</tt>
     * If used without argument then removes all flags.
     *
     * @param flags the flags that you want to remove
     * @return this
     * @throws NullPointerException if <tt>flags</tt> are null
     */
    public QueryParserBuilder removeFlags(QueryParserFlag... flags) {
        if (flags == null)
            throw new NullPointerException("flag should not be null");
        for (QueryParserFlag flag : flags)
            if (flag == null)
                throw new NullPointerException("flag should not be null");

        if (flags.length != 0)
            if (Arrays.asList(flags).contains(QueryParserFlag.WHITE_SPACE_IS_VALID))
                if (!Arrays.asList(flags).contains(QueryParserFlag.IGNORE_WHITE_SPACE))
                    if (containsFlag(QueryParserFlag.WHITE_SPACE_IS_VALID))
                        if (containsFlag(QueryParserFlag.IGNORE_WHITE_SPACE))
                            throw new RuntimeException
                                    ("Can not remove WHITE_SPACE_IS_VALID and having IGNORE_WHITE_SPACE");

        if (flags.length == 0)
            this.flags.clear();
        else
            this.flags.removeAll(Arrays.asList(flags));

        return this;
    }

    /**
     * Checks a specified flag state.
     *
     * @param flag flag which we want to check state
     * @return true if <tt>flag</tt> is added before
     * @throws NullPointerException if <tt>flag</tt> is null
     */
    public boolean containsFlag(QueryParserFlag flag) {
        if (flag == null)
            throw new NullPointerException("flag should not be null");
        return flags.contains(flag);
    }

    /**
     * @return query parser with added flags
     */
    public QueryParser build() {
        return new QueryParser(flags);
    }
}
