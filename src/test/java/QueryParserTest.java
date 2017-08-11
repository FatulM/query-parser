import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class QueryParserTest {
    @DataPoints("Query Strings With Illegal Characters")
    public static final List<String> QUERY_STRING_WITH_ILLEGAL_CHARACTERS = Arrays.asList(
            "?key=value", "\\", "key=value#", "key=value\n");
    @Rule
    public Timeout globalTimeout = new Timeout(1, TimeUnit.MINUTES);
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private QueryParser qParser;

    @Before
    public void setUp() throws Exception {
        qParser = new QueryParser();
    }

    @Test
    public void WhenRemoveNullArrayArgumentThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.removeFlags((QueryParser.Flag[]) null);
    }

    @Test
    public void WhenAddNullArrayArgumentThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.addFlags((QueryParser.Flag[]) null);
    }

    @Test
    public void WhenAddNullFlagThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.addFlags(QueryParser.Flag.CONVERT_TO_NULL, null,
                QueryParser.Flag.IGNORE_WHITE_SPACE, QueryParser.Flag.WHITE_SPACE_IS_VALID);
    }

    @Test
    public void WhenRemoveNullFlagThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.removeFlags(QueryParser.Flag.MERGE_VALUES, QueryParser.Flag.HARD_IGNORE_WHITE_SPACE, null);
    }

    @Test
    public void WhenAddFlagsThenObjectContainsThem() throws Exception {
        assertThat(qParser
                .addFlags(QueryParser.Flag.MERGE_VALUES, QueryParser.Flag.CONVERT_TO_NULL)
                .containsFlag(QueryParser.Flag.CONVERT_TO_NULL), is(true));
    }

    @Test
    public void WhenCheckingNullFlagThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.containsFlag(null);
    }

    @Test
    public void GivenObjectWithSomeFlagsWhenRemovingSomeFlagsThenObjectDoesNotContainThem() throws Exception {
        assertThat(qParser
                .addFlags(QueryParser.Flag.MERGE_VALUES, QueryParser.Flag.CONVERT_TO_NULL)
                .removeFlags(QueryParser.Flag.IGNORE_WHITE_SPACE, QueryParser.Flag.CONVERT_TO_NULL)
                .containsFlag(QueryParser.Flag.CONVERT_TO_NULL), is(false));
    }

    @Test
    public void WhenParsingNullQueryStringsThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("query string should not be null");
        qParser.parse(null);
    }

    @Theory
    public void WhenParsingQueryStringWithInvalidCharactersThenThrowsIllegalArgumentException
            (@FromDataPoints("Query Strings With Illegal Characters") String str) throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("query string has invalid characters");
        qParser.parse(str);
    }

    @Test
    public void WhenParsingBadStructuredQueryStringThenThrowsIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("query string is bad structured");
        qParser.parse("key1=value1&key2=value2=value3&key3=value4");
    }

    @Test
    public void WhenParsingASimpleQueryStringThenItContainsKey() throws Exception {
        assertThat(qParser.parse("key=value").containsKey("key"), is(true));
    }

    @Test
    public void WhenParsingAComplicatedQueryStringThenItContainsAllKeysSpecified() throws Exception {
        qParser.parse("key1=value1&key1&key1=&key2=value2");
        assertThat(qParser.containsKey("key1"), is(true));
        assertThat(qParser.containsKey("key2"), is(true));
        assertThat(qParser.containsKey(""), is(false));
        assertThat(qParser.containsKey(null), is(false));
    }

    @Test
    public void WhenParsingAQueryWithEmptyKeyValuePairThenItContainsEmptyKey() throws Exception {
        qParser.parse("key=value&");
        assertThat(qParser.containsKey(""), is(true));
    }

    @Test
    public void WhenParsingAQueryWithAnSpecifiedKeyThenValueListForOtherKeyIsNull() throws Exception {
        qParser.parse("key1=value1");
        assertThat(qParser.getValues("key2"), is(nullValue()));
    }

    @Test
    public void WhenParsingAQueryWithEmptyKeyValuePairThenItContainsNullValueForEmptyKey() throws Exception {
        qParser.parse("key=value&");
        assertThat(qParser.getValues(""), hasItem(nullValue()));
        assertThat(qParser.getValues("").size(), is(1));
    }

    @Test
    public void WhenParsingAQueryWithEmptyValueForASpecifiedKeyThenItContainsEmptyValueForThatKey() throws Exception {
        qParser.parse("key=");
        assertThat(qParser.getValues("key"), hasItem(""));
        assertThat(qParser.getValues("key").size(), is(1));
    }

    @Test
    public void WhenGettingCheckingIfItContainsNullKeyThenItThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("key can not be null");
        qParser.getValues(null);
    }

    @Test
    public void WhenGettingValuesForANullKeyThenItThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("key can not be null");
        qParser.containsKey(null);
    }

    @Test
    public void GivenAnEmptyQueryParserThenKeyCollectionIsEmpty() throws Exception {
        assertThat(qParser.getKeys().isEmpty(), is(true));
    }

    @Test
    public void WhenParsingAQueryStringThenKeyCollectionIsCollectionOfKeysSpecified() throws Exception {
        qParser.parse("key1=value1&key1&key1=&key2=value2&&=");
        assertThat(qParser.getKeys(), hasItems("key1", "key2", ""));
        assertThat(qParser.getKeys(), hasItem(not("key1 "))); // "key1" with space
        assertThat(qParser.getKeys().size(), is(3));
    }

    @Test
    public void GivenQueryParserWithoutWhiteSpaceIsValidWhenParsingQueryWithWhiteSpaceThenThrowsIllegalArgumentException
            () throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("query string contains unencoded white space");
        qParser.parse(" key=value");
    }

    @Test
    public void GivenQueryParserWithoutWhiteSpaceIsValidWhenAddingIgnoreWhiteSpaceFlagThenThrowsIllegalStateException()
            throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("can not add IGNORE_WHITE_SPACE without WHITE_SPACE_IS_VALID");
        qParser.addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE);
    }

    @Test
    public void GivenQueryParserWithProperFlagsWhenRemovingOnlyWhiteSpaceValidThenThrows() throws Exception {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Can not remove WHITE_SPACE_IS_VALID and having IGNORE_WHITE_SPACE");
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE)
                .removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID);
    }

    @Test
    public void GivenQueryParserWithProperFlagsWhenRemovingProperThenNothingIsThrown() throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE)
                .removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE);
    }
}
