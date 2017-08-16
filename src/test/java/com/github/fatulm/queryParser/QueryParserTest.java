package com.github.fatulm.queryParser;

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
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

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
            (@FromDataPoints("Query Strings With Illegal Characters") String str) throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("query string has invalid characters");
        qParser.parse(str);
    }

    @Theory
    public void GivenQueryParserWithoutWhiteSpaceIsValidWhenParsingQueryWithWhiteSpaceThenThrowsIllegalArgumentException
            (@FromDataPoints("Query Strings With White Space") String str) throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("query string contains unencoded white space");
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
        qParser.parse("key1=value1&key1&key1=&key2=value2&=");
        assertThat(qParser.containsKey("key1"), is(true));
        assertThat(qParser.containsKey("key2"), is(true));
        assertThat(qParser.containsKey(""), is(true));
    }

    @Test
    public void WhenParsingAQueryWithEmptyKeyValuePairWithoutEqualSignThenItDoesNotContainsEmptyKey()
            throws Exception {
        qParser.parse("key=value&");
        assertThat(qParser.containsKey(""), is(false));
    }

    @Test
    public void WhenParsingAQueryWithEmptyKeyValuePairWithEqualSignThenItContainsEmptyKey() throws Exception {
        qParser.parse("key=value&=");
        assertThat(qParser.containsKey(""), is(true));
    }

    @Test
    public void WhenParsingAQueryWithAnSpecifiedKeyThenValueListForOtherKeyIsNull() throws Exception {
        qParser.parse("key1=value1");
        assertThat(qParser.getValues("key2"), is(nullValue()));
    }

    @Test
    public void WhenParsingAQueryWithEmptyValueForASpecifiedKeyThenItContainsEmptyValueForThatKey() throws Exception {
        qParser.parse("key=");
        assertThat(qParser.getValues("key"), hasItem(""));
        assertThat(qParser.getValues("key"), hasSize(1));
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
        assertThat(qParser.getKeySet(), hasSize(3));
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
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE);
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Can not remove WHITE_SPACE_IS_VALID and having IGNORE_WHITE_SPACE");
        qParser.removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID);
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
        qParser.parse("key=value");
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("query parser is not empty");
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE);
    }

    @Test
    public void GivenNotEmptyQueryParserWhenWantToRemoveAFlagThenThrowsIllegalStateException() throws Exception {
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE)
                .parse("key=value");
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("query parser is not empty");
        qParser.removeFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE);
    }

    @Test
    public void GivenNotEmptyQueryParserWhenWantToRemoveAllFlagsThenThrowsIllegalStateException() throws Exception {
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE)
                .parse("key=value");
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("query parser is not empty");
        qParser.removeFlags();
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
        qParser.parse("key=value");
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("query parser is not empty");
        qParser.parse("key=value");
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
        qParser.parse("key=value");
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("query parser is not empty");
        qParser.addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE);
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

    @Test
    public void WhenQueryParserIsClearedThenItIsEmpty() throws Exception {
        assertThat(qParser.parse("key=value").clear().isEmpty(), is(true));
    }

    @Test
    public void WhenParsingAQueryWithMultipleValuesForAKeyThenItIsHandledWell() throws Exception {
        qParser.parse("key=value1&key=value2&key=&key");
        List<String> list = Arrays.asList("value1", "value2", "", null);
        assertThat(qParser.getValues("key"), is(list));
    }

    @Test
    public void GivenAQueryParserWithMergeValuesWhenParsingAQueryWithMultipleValuesForAKeyThenItValuesAreMerged()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.MERGE_VALUES)
                .parse("key=value1&key=value1&key=&key&key=&key&key=value1&anotherKey=anotherValue");
        List<String> list = Arrays.asList("value1", "", null);
        assertThat(qParser.getValues("key"), is(list));
    }

    @Test
    public void GivenAQueryParserWithMergeValuesAndConvertToNullWhenParsingThenIgnoreWhiteSpaceIsFirst()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.MERGE_VALUES,
                QueryParser.Flag.WHITE_SPACE_IS_VALID,
                QueryParser.Flag.IGNORE_WHITE_SPACE)
                .parse(" key  =  value   1 &key  = value 2  & key=value 1 & key = value    3");
        assertThat(qParser.getValues("key"), hasItems("value 1", "value 2", "value 3"));
        assertThat(qParser.getValues("key"), hasSize(3));
    }

    @Test
    public void GivenAQueryParserWithIgnoreAndHardIgnoreWhiteSpaceWhenParsingAQueryThenWhiteSpaceIsHandledWell()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.HARD_IGNORE_WHITE_SPACE,
                QueryParser.Flag.WHITE_SPACE_IS_VALID,
                QueryParser.Flag.IGNORE_WHITE_SPACE)
                .parse("%20key%20%20=  value%20   1 &%20key%20  = %20value 2  & key=%20value 1 & %20%20key = value 3");
        assertThat(qParser.getValues("key"), hasItems("value 1", "value 2", "value 3"));
        assertThat(qParser.getValues("key"), hasSize(4));
    }

    @Test
    public void GivenAQueryParserWithConvertToNullWhenParsingAQueryThenEmptyStringIsConvertedToNull()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.CONVERT_TO_NULL)
                .parse("key=");
        assertThat(qParser.getValues("key"), hasItem(nullValue()));
        assertThat(qParser.getValues("key"), hasSize(1));
    }

    @Test
    public void GivenAQueryParserWithConvertToNullWhenParsingAQueryWithEmptyKeyAndValueThenItIsRemoved()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.CONVERT_TO_NULL)
                .parse("=");
        assertThat(qParser.isEmpty(), is(true));
    }

    @Test
    public void GivenAQueryParserWithConvertToNullAndMergeValuesWhenParsingAQueryThenConvertShouldBeFirst()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.CONVERT_TO_NULL, QueryParser.Flag.MERGE_VALUES)
                .parse("key=value&key=&key&key=value&key=&key");
        List<String> list = Arrays.asList("value", null);
        assertThat(qParser.getValues("key"), is(list));
    }

    @Test
    public void GivenAQueryParserWithIgnoreWhiteSpaceWhenParsingAQueryWithAllTypesOfWhiteSpaceThenItIsHandled()
            throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID, QueryParser.Flag.IGNORE_WHITE_SPACE)
                .parse(" key\n\n\t key \t \n=value\tvalue\n\n\t ");
        assertThat(qParser.getValues("key key"), hasItem("value value"));
    }

    @Test
    public void GivenEmptyFlagsWhenRemoveWhiteSpaceIsValidWithOutIgnoreWhiteSpaceThenNoExceptionIsThrown()
            throws Exception {
        qParser.removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID);
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID)
                .removeFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID);
    }

    @Test
    public void WhenParsingALegalStringThenNoExceptionShouldBeThrown() throws Exception {
        qParser.parse("abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789" + "/?:@-._~!$&'()*+,;=");
    }

    @Test
    public void GivenWhiteSpaceIsValidWhenHavingEncodedAndUnEncodedSpacesThenKeysCanBeMerged() throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID)
                .parse("key =value 1&key%20=value%202");
        assertThat(qParser.getValues("key "), hasItems("value 1", "value 2"));
    }

    @Test
    public void GivenNotEmptyQueryParserWhenAddingFlagsWithEmptyArgumentThenNothingHappens() throws Exception {
        qParser.parse("key=value")
                .addFlags();
    }

    @Test
    public void WhenAddingOnlyIgnoreWhiteSpaceFlagWithoutEffectThenNothingShouldBeThrown() throws Exception {
        qParser.addFlags(QueryParser.Flag.WHITE_SPACE_IS_VALID)
                .addFlags(QueryParser.Flag.IGNORE_WHITE_SPACE);
    }

    @Test
    public void QueryParserFlagEnumValueOfCheck() {
        assertThat(QueryParser.Flag.valueOf(QueryParser.Flag.IGNORE_WHITE_SPACE.name()),
                is(QueryParser.Flag.IGNORE_WHITE_SPACE));
        // only for suppressing code coverage tool
    }
}
