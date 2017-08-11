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

import static org.hamcrest.CoreMatchers.is;
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
        qParser.addFlags(QueryParser.Flag.CONVERT_NULL, null, QueryParser.Flag.IGNORE_WHITE_SPACE);
    }

    @Test
    public void WhenRemoveNullFlagThenThrowsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("flag should not be null");
        qParser.removeFlags(QueryParser.Flag.MERGE_KEYS, null);
    }

    @Test
    public void WhenAddFlagsThenObjectContainsThem() throws Exception {
        assertThat(qParser
                .addFlags(QueryParser.Flag.MERGE_KEYS, QueryParser.Flag.CONVERT_NULL)
                .containsFlag(QueryParser.Flag.CONVERT_NULL), is(true));
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
                .addFlags(QueryParser.Flag.MERGE_KEYS, QueryParser.Flag.CONVERT_NULL)
                .removeFlags(QueryParser.Flag.IGNORE_WHITE_SPACE, QueryParser.Flag.CONVERT_NULL)
                .containsFlag(QueryParser.Flag.CONVERT_NULL), is(false));
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
}
