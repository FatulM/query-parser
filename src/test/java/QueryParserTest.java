import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class QueryParserTest {
    private QueryParser queryParser;

    @Before
    public void setUp() throws Exception {
        queryParser = new QueryParser();
    }

    @Test
    public void contains() throws Exception {
        assertThat(queryParser.parse("key=value").contains("key"), is(true));
    }

}
