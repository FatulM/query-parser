import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryParserTest {
    @DataPoints("QueryStringsWithIllegalCharacters")
    public static String[] QUERY_STRING_WITH_ILLEGAL_CHARACTERS = new String[]
            {"?key=value", "\\", "key=value#", "key=value\n"};
    @DataPoints("IllegalKeyStrings")
    public static String[] ILLEGAL_KEY_STRINGS = new String[]
            {"?key", "key&key", "key\n", "key\\"};
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
    public void GivenQueryParserWithSomeFlagsWhenRemovingSomeFlagsThenObjectDoesNotContainThem() throws Exception {
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
            (@FromDataPoints("QueryStringsWithIllegalCharacters") String str) throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("query string has invalid characters");
        qParser.parse(str);
    }

    @Test
    public void WhenParsingBadStructuredQueryStringThenThrowsIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("query string has bad structure");
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
        assertThat(qParser.getKeySet().isEmpty(), is(true));
    }

    @Test
    public void WhenParsingAQueryStringThenKeyCollectionIsCollectionOfKeysSpecified() throws Exception {
        qParser.parse("key1=value1&key1&key1=&key2=value2&&=");
        assertThat(qParser.getKeySet(), hasItems("key1", "key2", ""));
        assertThat(qParser.getKeySet(), hasItem(not("key3")));
        assertThat(qParser.getKeySet().size(), is(3));
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

    @Test
    public void WhenParsingAQueryStringWithEncodedSpaceThenSpaceIsConvertedForKey()
            throws Exception {
        qParser.parse("%20key%20=%20value%20");
        assertThat(qParser.containsKey(" key "), is(true));
        assertThat(qParser.getKeySet(), hasItem(" key "));
        assertThat(qParser.getValues(" key "), is(notNullValue()));
    }

    @Test
    public void WhenParsingAQueryStringWithEncodedSpaceThenSpaceIsConvertedForValue()
            throws Exception {
        qParser.parse("%20key%20=%20value%20");
        assertThat(qParser.getValues(" key "), hasItem(" value "));
    }

    @Test
    public void GivenNotEmptyQueryParserWhenWantToAddAFlagThenThrowsIllegalStateException() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("query parser is not empty");
        qParser.parse("key=value")
                .addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE);
    }

    @Test
    public void GivenNotEmptyQueryParserWhenWantToRemoveAFlagThenThrowsIllegalStateException() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("query parser is not empty");
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE)
                .parse("key=value")
                .removeFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE);
    }

    @Test
    public void GivenNotEmptyQueryParserWhenWantToRemoveAllFlagsThenThrowsIllegalStateException() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("query parser is not empty");
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE)
                .parse("key=value")
                .removeFlags();
    }

    @Test
    public void GivenNotEmptyQueryParserWhenWantToAddAFlagWithoutEffectThenNothingIsThrown() throws Exception {
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE)
                .parse("key=value")
                .addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE);
    }

    @Test
    public void GivenNotEmptyQueryParserWhenWantToRemoveAFlagWithoutEffectThenNothingIsThrown() throws Exception {
        qParser.parse("key=value")
                .removeFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE);
    }

    @Test
    public void GivenNotEmptyQueryParserWhenWantToRemoveAllFlagsWithoutEffectThenNothingIsThrown() throws Exception {
        qParser.parse("key=value")
                .removeFlags();
    }

    @Test
    public void GivenEmptyQueryParserThenItIsEmpty() throws Exception {
        assertThat(qParser.isEmpty(), is(true));
    }

    @Test
    public void GivenNotEmptyQueryParserThenItIsNotEmpty() throws Exception {
        assertThat(qParser.parse("key=value").isEmpty(), is(false));
    }

    @Test
    public void GivenNotEmptyQueryParserWhenParsingAnotherQueryThenItThrowsIllegalStateException() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("query parser is not empty");
        qParser.parse("key=value")
                .parse("key=value");
    }

    @Theory
    public void WhenCheckingIllegalKeyStringsThenItThrowsIllegalArgumentException
            (@FromDataPoints("IllegalKeyStrings") String key) throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("key string is illegal");
        qParser.containsKey(key);
    }

    @Theory
    public void WhenGettingValuesForIllegalKeyStringsThenItThrowsIllegalArgumentException
            (@FromDataPoints("Illegal Key Strings") String key) throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("key string is illegal");
        qParser.getValues(key);
    }

    @Test
    public void GivenAQueryParserWithIgnoreWhiteSpaceWhenParsingQueryWithSpaceThenSpaceIsHandledForKeys()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE)
                .parse("  key   key  =  value   value    ");
        assertThat(qParser.containsKey("key key"), is(true));
    }

    @Test
    public void GivenAQueryParserWithIgnoreWhiteSpaceWhenParsingQueryWithSpaceThenSpaceIsHandledForValues()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE)
                .parse("  key   key  =  value   value    ");
        assertThat(qParser.getValues("key key"), hasItem("value value"));
    }

    @Test
    public void GivenAEmptyQueryParserWhenQueryParserThrowsExceptionForFlagsThenItShouldBeEmpty()
            throws Exception {
        try {
            qParser.addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE);
        } catch (Exception ignored) {
        }

        assertThat(qParser.isEmpty(), is(true));
    }

    @Test
    public void GivenAQueryParserParsedAQueryWhenAddingNotProperFlagsThenQueryParserShouldNotChange()
            throws Exception {
        qParser.parse("key=value");
        try {
            qParser.addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE);
        } catch (Exception ignored) {
        }
        assertThat(qParser.getValues("key"), hasItem("value"));
    }

    @Test
    public void GivenANotEmptyQueryParserWhenAddingBadFlagsThenItShouldThrowExceptionForBadStateNotBadFlags()
            throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("query parser is not empty");
        qParser.parse("key=value");
        qParser.addFlags(QueryParser.Flag.CONVERT_TO_NULL);
    }

    @Test
    public void GivenANotEmptyQueryParserWhenQueryParserThrowsExceptionForFlagsThenItShouldBeEmpty()
            throws Exception {
        try {
            qParser.addFlags(QueryParser.Flag.CONVERT_TO_NULL);
        } catch (Exception ignored) {
        }
        assertThat(qParser.isEmpty(), is(true));
    }

    @Test
    public void GivenAQueryParserWithHardIgnoreWhiteSpaceWhenParsingAQueryStringThenEncodedSpaceIsHandledForKeys()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE)
                .parse("%20%20%20key%20%20%20key%20=%20value%20value%20%20");
        assertThat(qParser.getKeySet(), hasItem("key key"));
    }

    @Test
    public void GivenAQueryParserWithHardIgnoreWhiteSpaceWhenParsingAQueryStringThenEncodedSpaceIsHandledForValues()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE)
                .parse("%20%20%20key%20%20%20key%20=%20value%20value%20%20");
        assertThat(qParser.getValues("key key"), hasItem("value value"));
    }

    @Test
    public void WhenParsingAnEmptyQueryStringThenEmptyKeyToNullMustBeRemoved() throws Exception {
        qParser.parse("");
        assertThat(qParser.getKeySet().isEmpty(), is(true));
    }

    @Test
    public void WhenParsingAnFullySpacedQueryStringThenKeyShouldNotBeRemoved() throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID)
                .parse(" ");
        assertThat(qParser.getValues(" "), hasItem(nullValue()));
    }

    @Test
    public void WhenParsingAnEmptyQueryStringThenEmptyKeyToEmptyValueShouldNotBeRemoved() throws Exception {
        qParser.parse("=");
        assertThat(qParser.getValues(""), hasItem(""));
    }

    @Test
    public void GivenAQueryParserWithIgnoreWhiteSpaceWhenParsingAnFullySpacedQueryStringThenKeyShouldBeRemoved()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE, QueryParser.Flag.WHITE_SPACE_IS_VALID)
                .parse(" ");
        assertThat(qParser.getKeySet().isEmpty(), is(true));
    }

    @Test
    public void splitStringTest() throws Exception {
        List<String> list = QueryParser.stringSplit("a=b=c", '=');
        List<String> exp = Arrays.asList("a", "b", "c");
        assertThat(list, is(equalTo(exp)));

        list = QueryParser.stringSplit("", '=');
        exp = Collections.singletonList("");
        assertThat(list, is(equalTo(exp)));

        list = QueryParser.stringSplit("=", '=');
        exp = Arrays.asList("", "");
        assertThat(list, is(equalTo(exp)));

        list = QueryParser.stringSplit("a==", '=');
        exp = Arrays.asList("a", "", "");
        assertThat(list, is(equalTo(exp)));

        list = QueryParser.stringSplit(" =a=", '=');
        exp = Arrays.asList(" ", "a", "");
        assertThat(list, is(equalTo(exp)));
    }
}
