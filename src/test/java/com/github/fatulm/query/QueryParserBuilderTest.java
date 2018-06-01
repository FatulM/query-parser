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
                .removeFlags((QueryParser.Flag[]) null);
    }

    @Test
    public void whenAddNullArrayArgumentThenThrowsNullPointerException() throws Exception {
        ex.expect(NullPointerException.class);
        ex.expectMessage("flag should not be null");

        QueryParser.builder()
                .addFlags((QueryParser.Flag[]) null);
    }

    @Test
    public void whenAddNullFlagThenThrowsNullPointerException() throws Exception {
        ex.expect(NullPointerException.class);
        ex.expectMessage("flag should not be null");

        QueryParser.builder()
                .addFlags(QueryParser.Flag.CONVERT_TO_NULL,
                        null,
                        QueryParser.Flag.IGNORE_WHITE_SPACE,
                        QueryParser.Flag.WHITE_SPACE_IS_VALID);
    }

    @Test
    public void whenRemoveNullFlagThenThrowsNullPointerException() throws Exception {
        ex.expect(NullPointerException.class);
        ex.expectMessage("flag should not be null");

        QueryParser.builder()
                .removeFlags(QueryParser.Flag.MERGE_VALUES,
                        QueryParser.Flag.HARD_IGNORE_WHITE_SPACE,
                        null);
    }

    @Test
    public void whenAddFlagsThenObjectContainsThem() throws Exception {
        QueryParser.Builder builder = QueryParser.builder()
                .addFlags(QueryParser.Flag.MERGE_VALUES,
                        QueryParser.Flag.CONVERT_TO_NULL);

        assertThat(builder.containsFlag(QueryParser.Flag.CONVERT_TO_NULL), is(true));
        assertThat(builder.build().containsFlag(QueryParser.Flag.CONVERT_TO_NULL), is(true));
    }

    @Test
    public void givenQueryParserWithSomeFlagsWhenRemovingSomeFlagsThenObjectDoesNotContainThem() throws Exception {
        assertThat(QueryParser.builder()
                .addFlags(QueryParser.Flag.MERGE_VALUES,
                        QueryParser.Flag.CONVERT_TO_NULL)
                .removeFlags(QueryParser.Flag.IGNORE_WHITE_SPACE,
                        QueryParser.Flag.CONVERT_TO_NULL)
                .containsFlag(QueryParser.Flag.CONVERT_TO_NULL), is(false));
    }

    @Test
    public void givenQueryParserWithoutWhiteSpaceIsValidWhenAddingIgnoreWhiteSpaceFlagThenThrowsIllegalStateException()
            throws Exception {
        ex.expect(IllegalStateException.class);
        ex.expectMessage("can not add IGNORE_WHITE_SPACE without WHITE_SPACE_IS_VALID");

        QueryParser.builder()
                .addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE);
    }

    @Test
    public void givenQueryParserWithProperFlagsWhenRemovingOnlyWhiteSpaceValidThenThrows() throws Exception {
        QueryParser.Builder builder = QueryParser.builder()
                .addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID,
                        QueryParser.Flag.IGNORE_WHITE_SPACE);

        ex.expect(RuntimeException.class);
        ex.expectMessage("Can not remove WHITE_SPACE_IS_VALID and having IGNORE_WHITE_SPACE");

        builder.removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID);
    }

    @Test
    public void givenQueryParserWithProperFlagsWhenRemovingProperThenNothingIsThrown() throws Exception {
        QueryParser.builder()
                .addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID,
                        QueryParser.Flag.IGNORE_WHITE_SPACE)
                .removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID,
                        QueryParser.Flag.IGNORE_WHITE_SPACE);
    }

    @Test
    public void whenAddingOnlyIgnoreWhiteSpaceFlagWithoutEffectThenNothingShouldBeThrown() throws Exception {
        QueryParser.builder()
                .addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID)
                .addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE);
    }
}
