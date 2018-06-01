package com.github.fatulm.query;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("RedundantThrows")
@RunWith(Theories.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryParserBuilderTest {
    @Rule
    public Timeout globalTimeout = new Timeout(1, TimeUnit.MINUTES);
    @Rule
    public ExpectedException ex = ExpectedException.none();

    @Test
    public void whenRemoveNullArrayArgumentThenThrowsNullPointerException() throws Exception {
        ex.expect(NullPointerException.class);
        ex.expectMessage("flag should not be null");

        QueryParser.builder()
                .removeFlags((QueryParserFlag[]) null);
    }

    @Test
    public void whenAddNullArrayArgumentThenThrowsNullPointerException() throws Exception {
        ex.expect(NullPointerException.class);
        ex.expectMessage("flag should not be null");

        QueryParser.builder()
                .addFlags((QueryParserFlag[]) null);
    }

    @Test
    public void whenAddNullFlagThenThrowsNullPointerException() throws Exception {
        ex.expect(NullPointerException.class);
        ex.expectMessage("flag should not be null");

        QueryParser.builder()
                .addFlags(QueryParserFlag.CONVERT_TO_NULL,
                        null,
                        QueryParserFlag.IGNORE_WHITE_SPACE,
                        QueryParserFlag.WHITE_SPACE_IS_VALID);
    }

    @Test
    public void whenRemoveNullFlagThenThrowsNullPointerException() throws Exception {
        ex.expect(NullPointerException.class);
        ex.expectMessage("flag should not be null");

        QueryParser.builder()
                .removeFlags(QueryParserFlag.MERGE_VALUES,
                        QueryParserFlag.HARD_IGNORE_WHITE_SPACE,
                        null);
    }

    @Test
    public void whenAddFlagsThenObjectContainsThem() throws Exception {
        QueryParserBuilder builder = QueryParser.builder()
                .addFlags(QueryParserFlag.MERGE_VALUES,
                        QueryParserFlag.CONVERT_TO_NULL);

        assertThat(builder.containsFlag(QueryParserFlag.CONVERT_TO_NULL), is(true));
        assertThat(builder.build().containsFlag(QueryParserFlag.CONVERT_TO_NULL), is(true));
    }

    @Test
    public void givenQueryParserWithSomeFlagsWhenRemovingSomeFlagsThenObjectDoesNotContainThem() throws Exception {
        assertThat(QueryParser.builder()
                .addFlags(QueryParserFlag.MERGE_VALUES,
                        QueryParserFlag.CONVERT_TO_NULL)
                .removeFlags(QueryParserFlag.IGNORE_WHITE_SPACE,
                        QueryParserFlag.CONVERT_TO_NULL)
                .containsFlag(QueryParserFlag.CONVERT_TO_NULL), is(false));
    }

    @Test
    public void givenQueryParserWithoutWhiteSpaceIsValidWhenAddingIgnoreWhiteSpaceFlagThenThrowsIllegalStateException()
            throws Exception {
        ex.expect(IllegalStateException.class);
        ex.expectMessage("can not add IGNORE_WHITE_SPACE without WHITE_SPACE_IS_VALID");

        QueryParser.builder()
                .addFlags(QueryParserFlag.IGNORE_WHITE_SPACE);
    }

    @Test
    public void givenQueryParserWithProperFlagsWhenRemovingOnlyWhiteSpaceValidThenThrows() throws Exception {
        QueryParserBuilder builder = QueryParser.builder()
                .addFlags(QueryParserFlag.WHITE_SPACE_IS_VALID,
                        QueryParserFlag.IGNORE_WHITE_SPACE);

        ex.expect(RuntimeException.class);
        ex.expectMessage("Can not remove WHITE_SPACE_IS_VALID and having IGNORE_WHITE_SPACE");

        builder.removeFlags(QueryParserFlag.WHITE_SPACE_IS_VALID);
    }

    @Test
    public void givenQueryParserWithProperFlagsWhenRemovingProperThenNothingIsThrown() throws Exception {
        QueryParser.builder()
                .addFlags(QueryParserFlag.WHITE_SPACE_IS_VALID,
                        QueryParserFlag.IGNORE_WHITE_SPACE)
                .removeFlags(QueryParserFlag.WHITE_SPACE_IS_VALID,
                        QueryParserFlag.IGNORE_WHITE_SPACE);
    }

    @Test
    public void whenAddingOnlyIgnoreWhiteSpaceFlagWithoutEffectThenNothingShouldBeThrown() throws Exception {
        QueryParser.builder()
                .addFlags(QueryParserFlag.WHITE_SPACE_IS_VALID)
                .addFlags(QueryParserFlag.IGNORE_WHITE_SPACE);
    }
}
