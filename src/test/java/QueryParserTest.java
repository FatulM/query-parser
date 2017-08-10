import org.junit.Test;

public class QueryParserTest {
    @Test(expected = RuntimeException.class)
    public void methodTest() {
        QueryParser.method();
    }
}
