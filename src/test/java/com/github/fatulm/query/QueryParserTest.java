package com.github.fatulm.query;

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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@SuppressWarnings("RedundantThrows")
@RunWith(Theories.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryParserTest {
    @DataPoints("Query Strings With Illegal Characters")
    public static String[] QUERY_STRING_WITH_ILLEGAL_CHARACTERS = new String[]
            {"key=[value]", "\\", "key=value#", "key=value>", "{key}", "key=value\""};
    @DataPoints("Query Strings With White Space")
    public static String[] QUERY_STRING_WITH_WHITESPACE_CHARACTERS = new String[]
            {"key= value", " ", "\nkey=value", "k\tey=value", "key\n\t", "key\t= value"};
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
    public void whenRemoveNullArrayArgumentThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.removeFlags((QueryParser.Flag[]) null);
    }

    @Test
    public void whenAddNullArrayArgumentThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.addFlags((QueryParser.Flag[]) null);
    }

    @Test
    public void whenAddNullFlagThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.addFlags(QueryParser.Flag.CONVERT_TO_NULL, null,
                QueryParser.Flag.IGNORE_WHITE_SPACE, QueryParser.Flag.WHITE_SPACE_IS_VALID);
    }

    @Test
    public void whenRemoveNullFlagThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.removeFlags(QueryParser.Flag.MERGE_VALUES, QueryParser.Flag.HARD_IGNORE_WHITE_SPACE, null);
    }

    @Test
    public void whenAddFlagsThenObjectContainsThem() throws Exception {
        assertThat(qParser
                .addFlags(QueryParser.Flag.MERGE_VALUES, QueryParser.Flag.CONVERT_TO_NULL)
                .containsFlag(QueryParser.Flag.CONVERT_TO_NULL), is(true));
    }

    @Test
    public void whenCheckingNullFlagThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.containsFlag(null);
    }

    @Test
    public void givenQueryParserWithSomeFlagsWhenRemovingSomeFlagsThenObjectDoesNotContainThem() throws Exception {
        assertThat(qParser
                .addFlags(QueryParser.Flag.MERGE_VALUES, QueryParser.Flag.CONVERT_TO_NULL)
                .removeFlags(QueryParser.Flag.IGNORE_WHITE_SPACE, QueryParser.Flag.CONVERT_TO_NULL)
                .containsFlag(QueryParser.Flag.CONVERT_TO_NULL), is(false));
    }

    @Test
    public void whenParsingNullQueryStringsThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("query string should not be null");
        qParser.parse(null);
    }

    @Theory
    public void whenParsingQueryStringWithInvalidCharactersThenThrowsIllegalArgumentException
            (@FromDataPoints("Query Strings With Illegal Characters") String str) throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("query string has invalid characters");
        qParser.parse(str);
    }

    @Theory
    public void givenQueryParserWithoutWhiteSpaceIsValidWhenParsingQueryWithWhiteSpaceThenThrowsIllegalArgumentException
            (@FromDataPoints("Query Strings With White Space") String str) throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("query string contains unencoded white space");
        qParser.parse(str);
    }

    @Test
    public void whenParsingBadStructuredQueryStringThenThrowsIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("query string has bad structure");
        qParser.parse("key1=value1&key2=value2=value3&key3=value4");
    }

    @Test
    public void whenParsingASimpleQueryStringThenItContainsKey() throws Exception {
        assertThat(qParser.parse("key=value").containsKey("key"), is(true));
    }

    @Test
    public void whenParsingAComplicatedQueryStringThenItContainsAllKeysSpecified() throws Exception {
        Map<String, List<String>> map = qParser.parse("key1=value1&key1&key1=&key2=value2&=");
        assertThat(map.containsKey("key1"), is(true));
        assertThat(map.containsKey("key2"), is(true));
        assertThat(map.containsKey(""), is(true));
    }

    @Test
    public void whenParsingAQueryWithEmptyKeyValuePairWithoutEqualSignThenItDoesNotContainsEmptyKey() throws Exception {
        assertThat(qParser.parse("key=value&").containsKey(""), is(false));
    }

    @Test
    public void whenParsingAQueryWithEmptyKeyValuePairWithEqualSignThenItContainsEmptyKey() throws Exception {
        assertThat(qParser.parse("key=value&=").containsKey(""), is(true));
    }

    @Test
    public void whenParsingAQueryWithAnSpecifiedKeyThenValueListForOtherKeyIsNull() throws Exception {
        assertThat(qParser.parse("key1=value1").get("key2"), is(nullValue()));
    }

    @Test
    public void whenParsingAQueryWithEmptyValueForASpecifiedKeyThenItContainsEmptyValueForThatKey() throws Exception {
        Map<String, List<String>> map = qParser.parse("key=");
        assertThat(map.get("key"), hasItem(""));
        assertThat(map.get("key"), hasSize(1));
    }

    @Test
    public void whenGettingCheckingIfItContainsNullKeyThenItThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("key can not be null");
        //noinspection ResultOfMethodCallIgnored
        qParser.parse("key=value").get(null);
    }

    @Test
    public void whenGettingValuesForANullKeyThenItThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("key can not be null");
        //noinspection ResultOfMethodCallIgnored
        qParser.parse("key=value").containsKey(null);
    }

    @Test
    public void whenParsingAQueryStringThenKeyCollectionIsCollectionOfKeysSpecified() throws Exception {
        Map<String, List<String>> map = qParser.parse("key1=value1&key1&key1=&key2=value2&&=");
        assertThat(map.keySet(), hasItems("key1", "key2", ""));
        assertThat(map.keySet(), hasItem(not("key3")));
        assertThat(map.keySet(), hasSize(3));
    }

    @Test
    public void givenQueryParserWithoutWhiteSpaceIsValidWhenAddingIgnoreWhiteSpaceFlagThenThrowsIllegalStateException()
            throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("can not add IGNORE_WHITE_SPACE without WHITE_SPACE_IS_VALID");
        qParser.addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE);
    }

    @Test
    public void givenQueryParserWithProperFlagsWhenRemovingOnlyWhiteSpaceValidThenThrows() throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE);
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Can not remove WHITE_SPACE_IS_VALID and having IGNORE_WHITE_SPACE");
        qParser.removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID);
    }

    @Test
    public void givenQueryParserWithProperFlagsWhenRemovingProperThenNothingIsThrown() throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE)
                .removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE);
    }

    @Test
    public void whenParsingAQueryStringWithEncodedSpaceThenSpaceIsConvertedForKey() throws Exception {
        assertThat(qParser.parse("%20key%20=%20value%20").containsKey(" key "), is(true));
    }

    @Test
    public void whenParsingAQueryStringWithEncodedSpaceThenSpaceIsConvertedForValue() throws Exception {
        assertThat(qParser.parse("%20key%20=%20value%20").get(" key "), hasItem(" value "));
    }

    @Test
    public void givenNotEmptyQueryParserThenItIsNotEmpty() throws Exception {
        assertThat(qParser.parse("key=value").isEmpty(), is(false));
    }

    @Test
    public void givenAQueryParserWithIgnoreWhiteSpaceWhenParsingQueryWithSpaceThenSpaceIsHandledForKeys()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE);

        assertThat(qParser.parse("  key   key  =  value   value    ").containsKey("key key"),
                is(true));
    }

    @Test
    public void givenAQueryParserWithIgnoreWhiteSpaceWhenParsingQueryWithSpaceThenSpaceIsHandledForValues()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE);

        assertThat(qParser.parse("  key   key  =  value   value    ").get("key key"),
                hasItem("value value"));
    }

    @Test
    public void givenAQueryParserWithHardIgnoreWhiteSpaceWhenParsingAQueryStringThenEncodedSpaceIsHandledForKeys()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE);

        assertThat(qParser.parse("%20%20%20key%20%20%20key%20=%20value%20value%20%20").keySet(),
                hasItem("key key"));
    }

    @Test
    public void givenAQueryParserWithHardIgnoreWhiteSpaceWhenParsingAQueryStringThenEncodedSpaceIsHandledForValues()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE);

        assertThat(qParser.parse("%20%20%20key%20%20%20key%20=%20value%20value%20%20").get("key key"),
                hasItem("value value"));
    }

    @Test
    public void whenParsingAnEmptyQueryStringThenEmptyKeyToNullMustBeRemoved() throws Exception {
        assertThat(qParser.parse("").keySet().isEmpty(), is(true));
    }

    @Test
    public void whenParsingAnFullySpacedQueryStringThenKeyShouldNotBeRemoved() throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID);

        assertThat(qParser.parse(" ").get(" "), hasItem(nullValue()));
    }

    @Test
    public void whenParsingAnEmptyQueryStringThenEmptyKeyToEmptyValueShouldNotBeRemoved() throws Exception {
        assertThat(qParser.parse("=").get(""), hasItem(""));
    }

    @Test
    public void givenAQueryParserWithIgnoreWhiteSpaceWhenParsingAnFullySpacedQueryStringThenKeyShouldBeRemoved()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE, QueryParser.Flag.WHITE_SPACE_IS_VALID);

        assertThat(qParser.parse(" ").keySet().isEmpty(), is(true));
    }

    @Test
    public void splitStringTest() throws Exception {
        List<String> list = TextUtils.stringSplit("a=b=c", '=');
        List<String> exp = Arrays.asList("a", "b", "c");
        assertThat(list, is(exp));

        list = TextUtils.stringSplit("", '=');
        exp = Collections.singletonList("");
        assertThat(list, is(exp));

        list = TextUtils.stringSplit("=", '=');
        exp = Arrays.asList("", "");
        assertThat(list, is(exp));

        list = TextUtils.stringSplit("a==", '=');
        exp = Arrays.asList("a", "", "");
        assertThat(list, is(exp));

        list = TextUtils.stringSplit(" =a=", '=');
        exp = Arrays.asList(" ", "a", "");
        assertThat(list, is(exp));

        list = TextUtils.stringSplit("hello", '=');
        exp = Collections.singletonList("hello");
        assertThat(list, is(exp));

        list = TextUtils.stringSplit("", '=');
        exp = Collections.singletonList("");
        assertThat(list, is(exp));
    }

    @Test
    public void whenParsingAQueryWithMultipleValuesForAKeyThenItIsHandledWell() throws Exception {
        List<String> list = Arrays.asList("value1", "value2", "", null);
        assertThat(qParser.parse("key=value1&key=value2&key=&key").get("key"), is(list));
    }

    @Test
    public void givenAQueryParserWithMergeValuesWhenParsingAQueryWithMultipleValuesForAKeyThenItValuesAreMerged()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.MERGE_VALUES);

        Map<String, List<String>> map =
                qParser.parse("key=value1&key=value1&key=&key&key=&key&key=value1&anotherKey=anotherValue");
        assertThat(map.get("key"), containsInAnyOrder("value1", "", null));
        assertThat(map.get("key"), hasSize(3));

    }

    @Test
    public void givenAQueryParserWithMergeValuesAndConvertToNullWhenParsingThenIgnoreWhiteSpaceIsFirst()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.MERGE_VALUES,
                QueryParser.Flag.WHITE_SPACE_IS_VALID,
                QueryParser.Flag.IGNORE_WHITE_SPACE);

        Map<String, List<String>> map =
                qParser.parse(" key  =  value   1 &key  = value 2  & key=value 1 & key = value    3");
        assertThat(map.get("key"), hasItems("value 1", "value 2", "value 3"));
        assertThat(map.get("key"), hasSize(3));
    }

    @Test
    public void givenAQueryParserWithIgnoreAndHardIgnoreWhiteSpaceWhenParsingAQueryThenWhiteSpaceIsHandledWell()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE,
                QueryParser.Flag.WHITE_SPACE_IS_VALID,
                QueryParser.Flag.IGNORE_WHITE_SPACE);

        Map<String, List<String>> map =
                qParser.parse("%20key%20%20=  value%20   1 &%20key%20  = %20value 2  & key=%20value 1 & %20%20key = value 3");
        assertThat(map.get("key"), hasItems("value 1", "value 2", "value 3"));
        assertThat(map.get("key"), hasSize(4));
    }

    @Test
    public void givenAQueryParserWithConvertToNullWhenParsingAQueryThenEmptyStringIsConvertedToNull()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.CONVERT_TO_NULL);

        Map<String, List<String>> map = qParser.parse("key=");
        assertThat(map.get("key"), hasItem(nullValue()));
        assertThat(map.get("key"), hasSize(1));
    }

    @Test
    public void givenAQueryParserWithConvertToNullWhenParsingAQueryWithEmptyKeyAndValueThenItIsRemoved()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.CONVERT_TO_NULL);

        assertThat(qParser.parse("=").isEmpty(), is(true));
    }

    @Test
    public void givenAQueryParserWithConvertToNullAndMergeValuesWhenParsingAQueryThenConvertShouldBeFirst()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.CONVERT_TO_NULL, QueryParser.Flag.MERGE_VALUES);

        Map<String, List<String>> map = qParser.parse("key=value&key=&key&key=value&key=&key");
        assertThat(map.get("key"), containsInAnyOrder("value", null));
        assertThat(map.get("key"), hasSize(2));
    }

    @Test
    public void givenAQueryParserWithIgnoreWhiteSpaceWhenParsingAQueryWithAllTypesOfWhiteSpaceThenItIsHandled()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE);

        assertThat(qParser.parse(" key\n\n\t key \t \n=value\tvalue\n\n\t ").get("key key"),
                hasItem("value value"));
    }

    @Test
    public void givenEmptyFlagsWhenRemoveWhiteSpaceIsValidWithOutIgnoreWhiteSpaceThenNoExceptionIsThrown()
            throws Exception {
        qParser.removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID);
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID)
                .removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID);
    }

    @Test
    public void whenParsingALegalStringThenNoExceptionShouldBeThrown() throws Exception {
        qParser.parse("abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789" + "/?:@-._~!$&'()*+,;=");
    }

    @Test
    public void givenWhiteSpaceIsValidWhenHavingEncodedAndUnEncodedSpacesThenKeysCanBeMerged() throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID);
        assertThat(qParser.parse("key =value 1&key%20=value%202").get("key "), hasItems("value 1", "value 2"));
    }

    @Test
    public void whenAddingOnlyIgnoreWhiteSpaceFlagWithoutEffectThenNothingShouldBeThrown() throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID)
                .addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE);
    }

    @Test
    public void queryParserFlagEnumValueOfCheck() {
        assertThat(QueryParser.Flag.valueOf(QueryParser.Flag.IGNORE_WHITE_SPACE.name()),
                is(QueryParser.Flag.IGNORE_WHITE_SPACE));
        // only for suppressing code coverage tool
    }
}
